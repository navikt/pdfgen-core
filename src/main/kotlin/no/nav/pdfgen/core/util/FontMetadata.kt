package no.nav.pdfgen.core.util

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import no.nav.pdfgen.core.fontsRoot

data class FontMetadata(
    val family: String,
    val path: String,
    val weight: Int,
    val style: BaseRendererBuilder.FontStyle,
    val subset: Boolean,
) {
    val bytes: ByteArray = fontsRoot.readAllBytes(path)
}
