package no.nav.pdfgen.core.pdf

import com.fasterxml.jackson.databind.JsonNode
import com.openhtmltopdf.pdfboxout.PDFontSupplier
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Calendar
import javax.imageio.ImageIO
import no.nav.pdfgen.core.PDFGenCore
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

private val log = KotlinLogging.logger {}

fun createPDFA(template: String, directoryName: String, jsonPayload: JsonNode? = null): ByteArray? {
    val html =
        jsonPayload?.let { createHtml(template, directoryName, it) }
            ?: createHtmlFromTemplateData(template, directoryName)
    return html?.let { createPDFA(it) }
}

fun createPDFA(html: String): ByteArray {

    val pdf =
        ByteArrayOutputStream()
            .apply {
                PdfRendererBuilder()
                    .apply {
                        for (font in PDFGenCore.environment.fonts) {
                            val ttf =
                                TTFParser()
                                    .parse(
                                        RandomAccessReadBufferedFile(
                                            "${PDFGenCore.environment.fontsRoot.path}/${font.path}"
                                        )
                                    )
                                    .also { it.isEnableGsub = false }
                            useFont(
                                PDFontSupplier(PDType0Font.load(PDDocument(), ttf, font.subset)),
                                font.family,
                                font.weight,
                                font.style,
                                font.subset
                            )
                        }
                    }
                    .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_A)
                    .usePdfUaAccessbility(true)
                    .useColorProfile(PDFGenCore.environment.colorProfile)
                    .useSVGDrawer(BatikSVGDrawer())
                    .withHtmlContent(html, null)
                    .toStream(this)
                    .run()
            }
            .toByteArray()
    require(verifyCompliance(pdf)) { "Non-compliant PDF/A :(" }
    return pdf
}

fun createPDFA(imageStream: InputStream, outputStream: OutputStream) {
    PDDocument().use { document ->
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)
        val image = toPortait(ImageIO.read(imageStream))

        val quality = 1.0f

        val pdImage =
            try {
                JPEGFactory.createFromImage(document, image, quality)
            } catch (e: javax.imageio.IIOException) {
                // To avoid "javax.imageio.IIOException: Illegal band size: should be 0 < size <= 8"
                // for certain black/white pictures
                LosslessFactory.createFromImage(document, image)
            }

        val imageSize = scale(pdImage, page)

        PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false).use {
            it.drawImage(pdImage, Matrix(imageSize.width, 0f, 0f, imageSize.height, 0f, 0f))
        }

        val xmp = XMPMetadata.createXMPMetadata()
        val catalog = document.documentCatalog
        val cal = Calendar.getInstance()

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

        val intent = PDOutputIntent(document, PDFGenCore.environment.colorProfile.inputStream())
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

private fun verifyCompliance(
    input: ByteArray,
    flavour: PDFAFlavour = PDFAFlavour.PDFA_2_A,
): Boolean {
    val pdf = ByteArrayInputStream(input)
    val validator = Foundries.defaultInstance().createValidator(flavour, false)
    val result = Foundries.defaultInstance().createParser(pdf).use { validator.validate(it) }
    val failures = result.testAssertions.filter { it.status != TestAssertion.Status.PASSED }
    failures.forEach { test ->
        log.warn { test.message }
        log.warn { "Location ${test.location.context} ${test.location.level}" }
        log.warn { "Status ${test.status}" }
        log.warn { "Test number ${test.ruleId.testNumber}" }
    }
    return failures.isEmpty()
}
