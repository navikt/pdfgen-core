package no.nav.pdfgen.core.pdf

import no.nav.pdfgen.getResource
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CreatePdfKtTest {
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
