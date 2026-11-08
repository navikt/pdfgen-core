package no.nav.pdfgen.core.pdf

import com.fasterxml.jackson.databind.JsonNode
import com.openhtmltopdf.pdfboxout.PDFontSupplier
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.pdfgen.core.PDFGenCore
import no.nav.pdfgen.core.util.FontMetadata
import no.nav.pdfgen.core.util.scale
import no.nav.pdfgen.core.util.toPortait
import org.apache.fontbox.ttf.TTFParser
import org.apache.pdfbox.io.RandomAccessReadBufferedFile
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDMetadata
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences
import org.apache.pdfbox.util.Matrix
import org.apache.xmpbox.XMPMetadata
import org.apache.xmpbox.type.BadFieldValueException
import org.apache.xmpbox.xml.XmpSerializer
import org.verapdf.pdfa.Foundries
import org.verapdf.pdfa.flavours.PDFAFlavour
import org.verapdf.pdfa.results.TestAssertion
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.util.Calendar
import javax.imageio.ImageIO

private val log = KotlinLogging.logger {}

/**
 * Renders a template as a PDF/A document.
 *
 * Uses [jsonPayload] when provided. Otherwise, loads the template's default data.
 *
 * @param template template name without the `.hbs` extension
 * @param directoryName application directory containing the template
 * @param jsonPayload data for the template, or null to load its default data
 * @return the generated PDF, or null if template rendering returns no HTML
 */
fun createPDFA(
    template: String,
    directoryName: String,
    jsonPayload: JsonNode? = null,
): ByteArray? {
    // Use request data when present; otherwise load the template's default data.
    val html = jsonPayload?.let {
        createHtml(
            template = template,
            directoryName = directoryName,
            jsonPayload = it,
        )
    } ?: createHtmlFromTemplateData(
        template = template,
        directoryName = directoryName,
    )

    return html?.let { createPDFA(it) }
}

/**
 * Renders HTML as PDF/A-2A and verifies the generated document.
 *
 * @param html HTML markup to render
 * @return the generated PDF bytes
 * @throws IllegalArgumentException if the generated PDF fails compliance validation
 */
fun createPDFA(html: String): ByteArray {
    val environment = PDFGenCore.environment
    val pdf = ByteArrayOutputStream().use { outputStream ->
        val builder = PdfRendererBuilder()
        // The builder uses the registered fonts during run(), so keep their backing resources open.
        withLoadedFonts(
            builder = builder,
            fonts = environment.fonts,
            fontsRoot = environment.fontsRoot.path,
        ) {
            builder
                .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_A)
                .usePdfUaAccessibility(true)
                .useColorProfile(environment.colorProfile)
                .useSVGDrawer(BatikSVGDrawer())
                .withHtmlContent(html, null)
                .toStream(outputStream)
                .run()
        }
        outputStream.toByteArray()
    }
    // Validate the serialized bytes so non-compliant output is never returned.
    require(verifyCompliance(pdf)) { "Non-compliant PDF/A :(" }
    return pdf
}

/**
 * Registers fonts and keeps their resources open until [render] completes.
 *
 * @param builder renderer that receives the font registrations
 * @param fonts font definitions to register
 * @param fontsRoot directory containing the font files
 * @param render rendering operation to run after all fonts are registered
 */
private fun withLoadedFonts(
    builder: PdfRendererBuilder,
    fonts: List<FontMetadata>,
    fontsRoot: Path,
    render: () -> Unit,
) {
    /**
     * Registers the font at [index], then advances to the next font.
     *
     * Runs [render] after registering the last font.
     *
     * @param index zero-based index of the next font to register
     */
    fun registerFont(index: Int) {
        if (index == fonts.size) {
            // All fonts are registered; render before the nested use blocks close them.
            render()
            return
        }

        val font = fonts[index]
        // Keep the font file and parsed font alive while PDFBox embeds glyphs during rendering.
        RandomAccessReadBufferedFile(
            fontsRoot
                .resolve(font.path)
                .toString(),
        ).use { fontFile ->
            TTFParser().parse(fontFile).use { trueTypeFont ->
                trueTypeFont.isEnableGsub = false

                // PDType0Font is attached to this document, which must outlive the render.
                PDDocument().use { fontDocument ->
                    builder.useFont(
                        PDFontSupplier(
                            PDType0Font.load(
                                fontDocument,
                                trueTypeFont,
                                font.subset,
                            ),
                        ),
                        font.family,
                        font.weight,
                        font.style,
                        font.subset,
                    )
                    registerFont(index + 1)
                }
            }
        }
    }

    // Start at the first zero-indexed font; an empty list renders immediately.
    registerFont(0)
}

/**
 * Converts an image stream to PDF/A and writes the result to [outputStream].
 *
 * The caller retains ownership of both streams.
 *
 * @param imageStream image data supported by [ImageIO]
 * @param outputStream destination for the generated PDF bytes
 * @throws IllegalArgumentException if the image data is unsupported
 */
fun createPDFA(
    imageStream: InputStream,
    outputStream: OutputStream,
) {
    PDDocument().use { document ->
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)
        // Decode once; null means ImageIO did not recognize the input format.
        val image = toPortait(requireNotNull(ImageIO.read(imageStream)) { "Unsupported image format" })

        val quality = 1.0f

        val pdImage = try {
            JPEGFactory.createFromImage(
                document,
                image,
                quality,
            )
        } catch (_: javax.imageio.IIOException) {
            // The ImageIO JPEG writer rejects some grayscale sample layouts; keep those images lossless.
            LosslessFactory.createFromImage(document, image)
        }

        val imageSize = scale(pdImage, page)

        PDPageContentStream(
            document,
            page,
            PDPageContentStream.AppendMode.APPEND,
            false,
        ).use {
            it.drawImage(
                pdImage,
                Matrix(
                    imageSize.width,
                    0f,
                    0f,
                    imageSize.height,
                    0f,
                    0f,
                ),
            )
        }

        val xmp = XMPMetadata.createXMPMetadata()
        val catalog = document.documentCatalog
        val cal = Calendar.getInstance()

        // Add PDF/A identification, language, accessibility, and color-profile metadata before saving.
        try {
            val dc = xmp.createAndAddDublinCoreSchema()
            dc.addCreator("pdfgen-coree")
            dc.addDate(cal)

            val id = xmp.createAndAddPDFAIdentificationSchema()
            id.part = 2
            id.conformance = "U"

            val serializer = XmpSerializer()
            val baos = ByteArrayOutputStream()
            serializer.serialize(xmp, baos, true)

            val metadata = PDMetadata(document)
            metadata.importXMPMetadata(baos.toByteArray())
            catalog.metadata = metadata
        } catch (e: BadFieldValueException) {
            throw IllegalArgumentException(e)
        }

        // Closing this in-memory stream is not required; use is kept for clarity.
        // PDOutputIntent copies the ICC profile during construction.
        val intent = PDFGenCore.environment.colorProfile.inputStream().use { profile ->
            PDOutputIntent(document, profile)
        }

        intent.info = "sRGB IEC61966-2.1"
        intent.outputCondition = "sRGB IEC61966-2.1"
        intent.outputConditionIdentifier = "sRGB IEC61966-2.1"
        intent.registryName = "http://www.color.org"
        catalog.addOutputIntent(intent)
        catalog.language = "nb-NO"

        val pdViewer = PDViewerPreferences(page.cosObject)
        pdViewer.setDisplayDocTitle(true)
        catalog.viewerPreferences = pdViewer

        catalog.markInfo = PDMarkInfo(page.cosObject)
        catalog.structureTreeRoot = PDStructureTreeRoot()
        catalog.markInfo.isMarked = true

        document.save(outputStream)
        document.close()
    }
}

/**
 * Validates PDF bytes against [flavour] and logs failed assertions.
 *
 * @param input generated PDF bytes
 * @param flavour PDF/A profile to validate against
 * @return true if every validation assertion passes
 */
private fun verifyCompliance(
    input: ByteArray,
    flavour: PDFAFlavour = PDFAFlavour.PDFA_2_A,
): Boolean {
    val foundry = Foundries.defaultInstance()
    return ByteArrayInputStream(input).use { pdf ->
        // The parser and validator only serve this PDF; use closes them on success or failure.
        foundry
            .createValidator(
                flavour,
                false,
            ).use { validator ->
                val validationResult = foundry
                    .createParser(pdf)
                    .use { parser -> validator.validate(parser) }

                // VeraPDF includes passed assertions too; only failures need to be logged.
                val failures = validationResult.testAssertions
                    .filter { it.status != TestAssertion.Status.PASSED }

                failures.forEach { assertion ->
                    log.atWarn {
                        message = "{} | Location: {}, level={}, status={}, testNumber={}"
                        arguments = arrayOf(
                            assertion.message,
                            assertion.location.context,
                            assertion.location.level,
                            assertion.status,
                            assertion.ruleId.testNumber,
                        )
                    }
                }
                failures.isEmpty()
            }
    }
}
