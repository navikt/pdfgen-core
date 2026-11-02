package no.nav.pdfgen.core.pdf

import no.nav.pdfgen.getResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayOutputStream

class CreatePdfKtTest {
    @Test
    fun `empty image throws unsupported format`() {
        val outputStream = ByteArrayOutputStream()

        val exception = assertThrows<IllegalArgumentException> {
            createPDFA(imageStream = ByteArray(0).inputStream(), outputStream = outputStream)
        }

        assertEquals("Unsupported image format", exception.message)
        assertEquals(0, outputStream.size())
    }

    @Test
    fun `invalid image throws unsupported format`() {
        val outputStream = ByteArrayOutputStream()

        val exception = assertThrows<IllegalArgumentException> {
            createPDFA(
                imageStream = "synthetic invalid image".toByteArray(Charsets.UTF_8).inputStream(),
                outputStream = outputStream,
            )
        }

        assertEquals("Unsupported image format", exception.message)
        assertEquals(0, outputStream.size())
    }

    @Test
    fun `truncated png throws io exception`() {
        val byteArray = getResource<ByteArray>("/image/test.png")
        val outputStream = ByteArrayOutputStream()

        assertThrows<java.io.IOException> {
            createPDFA(imageStream = byteArray.copyOf(20).inputStream(), outputStream = outputStream)
        }
    }

    @Test
    fun `can convert png to pdf without errors`() {
        val byteArray = getResource<ByteArray>("/image/test.png")

        val outputStream = ByteArrayOutputStream()

        // create pdf
        createPDFA(imageStream = byteArray.inputStream(), outputStream = outputStream)
        val generatedPdfBytes = outputStream.toByteArray()

        assertNotNull(generatedPdfBytes)
        assertTrue(generatedPdfBytes.isNotEmpty(), "PDF generation failed, output is empty")
    }

    @Test
    fun `funky grey scale should not crash`() {
        val byteArray = getResource<ByteArray>("/image/16bitgreyscale.png")

        val outputStream = ByteArrayOutputStream()
        createPDFA(imageStream = byteArray.inputStream(), outputStream = outputStream)
        val generatedPdfBytes = outputStream.toByteArray()

        assertNotNull(generatedPdfBytes)
        assertTrue(generatedPdfBytes.isNotEmpty(), "PDF generation failed, output is empty")
    }
}
