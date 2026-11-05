package no.nav.pdfgen.core.pdf

import no.nav.pdfgen.core.Environment
import no.nav.pdfgen.core.PDFGenCore
import no.nav.pdfgen.core.PDFGenResource
import no.nav.pdfgen.core.objectMapper
import no.nav.pdfgen.getResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider
import java.io.ByteArrayOutputStream
import java.nio.file.Path

class CreatePdfKtTest {
    private fun initializePdfGeneration() {
        // The renderer reads fonts by file path, so point it at the packaged font files.
        val fontsRoot = Path.of(requireNotNull(javaClass.getResource("/fonts")).toURI())
        PDFGenCore.init(Environment(fontsRoot = PDFGenResource(fontsRoot)))
        // PDF/A validation needs VeraPDF's provider registered.
        VeraGreenfieldFoundryProvider.initialise()
    }

    private fun testPayload() = requireNotNull(javaClass.getResourceAsStream("/data/dsopkontroll/kontrollforespoersel.json"))
        .use { objectMapper.readTree(it) }

    @Nested
    inner class HtmlInput {
        @Test
        fun `renders html to pdf with configured fonts`() {
            // Arrange
            initializePdfGeneration()
            val html = "<html><body>test</body></html>"

            // Act
            val generatedPdf = createPDFA(html)

            // Assert
            assertTrue(generatedPdf.isNotEmpty())
        }
    }

    @Nested
    inner class TemplateInput {
        @Test
        fun `creates pdf from supplied json payload`() {
            // Arrange
            initializePdfGeneration()
            val payload = testPayload()

            // Act
            val generatedPdf = createPDFA(
                template = "kontrollforespoersel",
                directoryName = "dsopkontroll",
                jsonPayload = payload,
            )

            // Assert
            assertTrue(requireNotNull(generatedPdf).isNotEmpty())
        }

        @Test
        fun `creates pdf from template data when payload is omitted`() {
            // Arrange
            initializePdfGeneration()
            val template = "kontrollforespoersel"
            val directory = "dsopkontroll"

            // Act
            val generatedPdf = createPDFA(template = template, directoryName = directory)

            // Assert
            assertTrue(requireNotNull(generatedPdf).isNotEmpty())
        }

        @Test
        fun `returns null when template is not available`() {
            // Arrange
            initializePdfGeneration()

            // Act
            val generatedPdf = createPDFA(template = "behandlingsdager", directoryName = "syfosoknader")

            // Assert
            assertNull(generatedPdf)
        }
    }

    @Nested
    inner class ImageInput {
        @Test
        fun `empty image throws unsupported format`() {
            // Arrange
            val outputStream = ByteArrayOutputStream()
            val imageStream = ByteArray(0).inputStream()

            // Act
            val exception = assertThrows<IllegalArgumentException> {
                createPDFA(imageStream = imageStream, outputStream = outputStream)
            }

            // Assert
            assertEquals("Unsupported image format", exception.message)
            assertEquals(0, outputStream.size())
        }

        @Test
        fun `invalid image throws unsupported format`() {
            // Arrange
            val outputStream = ByteArrayOutputStream()
            val imageStream = "synthetic invalid image".toByteArray(Charsets.UTF_8).inputStream()

            // Act
            val exception = assertThrows<IllegalArgumentException> {
                createPDFA(imageStream = imageStream, outputStream = outputStream)
            }

            // Assert
            assertEquals("Unsupported image format", exception.message)
            assertEquals(0, outputStream.size())
        }

        @Test
        fun `truncated png throws io exception`() {
            // Arrange
            val image = getResource<ByteArray>("/image/test.png").copyOf(20)
            val outputStream = ByteArrayOutputStream()

            // Act
            assertThrows<java.io.IOException> {
                createPDFA(imageStream = image.inputStream(), outputStream = outputStream)
            }

            // Assert
            assertEquals(0, outputStream.size())
        }

        @Test
        fun `converts png to pdf`() {
            // Arrange
            initializePdfGeneration()
            val image = getResource<ByteArray>("/image/test.png")
            val outputStream = ByteArrayOutputStream()

            // Act
            createPDFA(imageStream = image.inputStream(), outputStream = outputStream)

            // Assert
            assertTrue(outputStream.toByteArray().isNotEmpty())
        }

        @Test
        fun `converts 16 bit grayscale png to pdf`() {
            // Arrange
            initializePdfGeneration()
            val image = getResource<ByteArray>("/image/16bitgreyscale.png")
            val outputStream = ByteArrayOutputStream()

            // Act
            createPDFA(imageStream = image.inputStream(), outputStream = outputStream)

            // Assert
            assertTrue(outputStream.toByteArray().isNotEmpty())
        }
    }
}
