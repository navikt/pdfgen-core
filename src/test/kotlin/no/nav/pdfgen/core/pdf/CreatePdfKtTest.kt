package no.nav.pdfgen.core.pdf

import com.sun.management.UnixOperatingSystemMXBean
import no.nav.pdfgen.core.Environment
import no.nav.pdfgen.core.PDFGenCore
import no.nav.pdfgen.core.PDFGenResource
import no.nav.pdfgen.core.objectMapper
import no.nav.pdfgen.getResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Path

class CreatePdfKtTest {
    @Suppress("DEPRECATION")
    private fun initializePdfGeneration(fontsRoot: Path = Path.of(requireNotNull(javaClass.getResource("/fonts")).toURI())) {
        // The renderer reads fonts by file path, so point it at the packaged font files.
        PDFGenCore.init(Environment(fontsRoot = PDFGenResource(fontsRoot)))
        // PDF/A validation needs VeraPDF's provider registered.
        VeraGreenfieldFoundryProvider.initialise()
    }

    private fun fileDescriptorBeanOrSkip(): UnixOperatingSystemMXBean {
        val fdBean = ManagementFactory.getOperatingSystemMXBean() as? UnixOperatingSystemMXBean
        assumeTrue(fdBean != null, "Open file descriptor count is only available on Unix-like JVMs")
        return requireNotNull(fdBean)
    }

    private fun testPayload() = requireNotNull(javaClass.getResourceAsStream("/data/dsopkontroll/kontrollforespoersel.json"))
        .use { objectMapper.readTree(it) }

    private companion object {
        const val RENDER_COUNT = 10
        const val MAX_DESCRIPTOR_INCREASE = 5L
    }

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

        @Test
        fun `does not leak file descriptors after repeated successful renders`() {
            // Arrange
            val fdBean = fileDescriptorBeanOrSkip()
            initializePdfGeneration()
            val html = "<html><body>test</body></html>"
            createPDFA(html)
            val baseline = fdBean.openFileDescriptorCount

            // Act
            repeat(RENDER_COUNT) {
                createPDFA(html)
            }
            val increase = fdBean.openFileDescriptorCount - baseline

            // Assert
            assertTrue(
                increase <= MAX_DESCRIPTOR_INCREASE,
                "Expected at most $MAX_DESCRIPTOR_INCREASE additional file descriptors after " +
                    "$RENDER_COUNT successful renders, but observed an increase of $increase",
            )
        }

        @Test
        fun `closes font resources when registration fails after opening a font`(
            @TempDir fontsRoot: Path,
        ) {
            // Arrange
            val fdBean = fileDescriptorBeanOrSkip()
            Files.write(
                fontsRoot.resolve("SourceSansPro-Regular.ttf"),
                getResource<ByteArray>("/fonts/SourceSansPro-Regular.ttf"),
            )
            Files.writeString(
                fontsRoot.resolve("config.json"),
                """
                [
                  {
                    "family": "Source Sans Pro",
                    "path": "SourceSansPro-Regular.ttf",
                    "weight": 400,
                    "style": "NORMAL",
                    "subset": false
                  },
                  {
                    "family": "Missing",
                    "path": "missing.ttf",
                    "weight": 400,
                    "style": "NORMAL",
                    "subset": false
                  }
                ]
                """.trimIndent(),
            )
            initializePdfGeneration(fontsRoot)
            val html = "<html><body>test</body></html>"

            // Warm up file loading before measuring descriptor growth.
            assertThrows<IOException> { createPDFA(html) }
            val baseline = fdBean.openFileDescriptorCount

            // Act
            repeat(RENDER_COUNT) {
                assertThrows<IOException> { createPDFA(html) }
            }
            val increase = fdBean.openFileDescriptorCount - baseline

            // Assert
            assertTrue(
                increase <= MAX_DESCRIPTOR_INCREASE,
                "Expected at most $MAX_DESCRIPTOR_INCREASE additional file descriptors after " +
                    "$RENDER_COUNT partial font registrations, but observed an increase of $increase",
            )
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
            assertThrows<IOException> {
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
