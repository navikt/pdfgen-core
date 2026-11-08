package no.nav.pdfgen.core.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.Raster

class ImageKtTest {
    @Test
    fun `toPortait returnerer stående bilde uendret`() {
        val portrait = BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB)
        val result = toPortait(portrait)
        assertEquals(portrait, result)
    }

    @Test
    fun `toPortait roterer liggende bilde til stående`() {
        val landscape = BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB)
        val result = toPortait(landscape)
        assertTrue(result.height >= result.width, "Bilde skal være stående etter rotasjon")
    }

    @Test
    fun `toPortait preserves all pixels when rotating landscape image`() {
        // Arrange
        val landscape = BufferedImage(4, 2, BufferedImage.TYPE_INT_RGB)
        val colors = listOf(
            listOf(Color.RED.rgb, Color.GREEN.rgb, Color.BLUE.rgb, Color.YELLOW.rgb),
            listOf(Color.CYAN.rgb, Color.MAGENTA.rgb, Color.ORANGE.rgb, Color.PINK.rgb),
        )
        colors.forEachIndexed { y, row ->
            row.forEachIndexed { x, color -> landscape.setRGB(x, y, color) }
        }

        // Act
        val result = toPortait(landscape)

        // Assert
        assertEquals(2, result.width)
        assertEquals(4, result.height)
        colors.forEachIndexed { y, row ->
            row.forEachIndexed { x, color ->
                assertEquals(color, result.getRGB(1 - y, x), "Pixel ($x, $y) should be preserved")
            }
        }
    }

    @Test
    fun `toPortait håndterer TYPE_CUSTOM uten å kaste IllegalArgumentException`() {
        // TYPE_CUSTOM (0) oppstår med visse bildeformater med tilpasset ColorModel
        val customColorModel = ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            intArrayOf(8, 8, 8, 8),
            true,
            false,
            Transparency.TRANSLUCENT,
            DataBuffer.TYPE_BYTE,
        )
        val sampleModel = customColorModel.createCompatibleSampleModel(200, 100)
        val dataBuffer = sampleModel.createDataBuffer()
        val raster = Raster.createWritableRaster(sampleModel, dataBuffer, null)
        val customTypeImage = BufferedImage(customColorModel, raster, false, null)

        assertEquals(BufferedImage.TYPE_CUSTOM, customTypeImage.type)

        val result = toPortait(customTypeImage)

        assertTrue(result.height >= result.width, "Bilde skal være stående etter rotasjon")
    }
}
