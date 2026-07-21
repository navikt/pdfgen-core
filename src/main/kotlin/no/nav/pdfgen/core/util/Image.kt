package no.nav.pdfgen.core.util

import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

data class ImageSize(val width: Float, val height: Float)

fun toPortait(image: BufferedImage): BufferedImage {
    // TYPE_CUSTOM (0) er ikke støttet av AffineTransformOp — normaliser til ARGB
    val normalized =
        if (image.type == BufferedImage.TYPE_CUSTOM) {
            BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB).also {
                val g = it.createGraphics()
                g.drawImage(image, 0, 0, null)
                g.dispose()
            }
        } else {
            image
        }

    if (normalized.height >= normalized.width) {
        return normalized
    }

    val rotateTransform =
        AffineTransform.getRotateInstance(
            Math.toRadians(90.0),
            (normalized.height / 2f).toDouble(),
            (normalized.height / 2f).toDouble(),
        )

    return AffineTransformOp(rotateTransform, AffineTransformOp.TYPE_BILINEAR)
        .filter(normalized, BufferedImage(normalized.height, normalized.width, normalized.type))
}

fun scale(image: PDImageXObject, page: PDPage): ImageSize {
    var width = image.width.toFloat()
    var height = image.height.toFloat()

    if (width > page.cropBox.width) {
        width = page.cropBox.width
        height = width * image.height.toFloat() / image.width.toFloat()
    }

    if (height > page.cropBox.height) {
        height = page.cropBox.height
        width = height * image.width.toFloat() / image.height.toFloat()
    }

    return ImageSize(width, height)
}
