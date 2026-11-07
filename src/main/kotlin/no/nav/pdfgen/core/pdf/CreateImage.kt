package no.nav.pdfgen.core.pdf

import java.io.ByteArrayOutputStream
import java.io.InputStream

@Suppress("unused")
fun createPdfFromImage(inputStream: InputStream): ByteArray = ByteArrayOutputStream().use { outputStream ->
    createPDFA(
        imageStream = inputStream,
        outputStream = outputStream,
    )
    outputStream.toByteArray()
}
