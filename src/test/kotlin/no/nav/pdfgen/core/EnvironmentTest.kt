package no.nav.pdfgen.core

import com.sun.management.UnixOperatingSystemMXBean
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.lang.management.ManagementFactory
import java.util.Base64

class EnvironmentTest {
    @Test
    fun `copy should not leak file descriptors when loading classpath resources repeatedly`() {
        val fdBean = ManagementFactory.getOperatingSystemMXBean() as? UnixOperatingSystemMXBean
        assumeTrue(fdBean != null, "Open file descriptor count is only available on Unix-like JVMs")
        if (fdBean == null) return

        val warmup = Environment()
        assertResourcesLoaded(warmup)

        val baseline = fdBean.openFileDescriptorCount
        repeat(COPY_COUNT) {
            assertResourcesLoaded(warmup.copy())
        }
        val measured = fdBean.openFileDescriptorCount
        val increase = measured - baseline

        assertTrue(
            increase <= MAX_DESCRIPTOR_INCREASE,
            "Expected at most $MAX_DESCRIPTOR_INCREASE additional open file descriptors after " +
                "$COPY_COUNT Environment.copy() calls, but observed an increase of $increase " +
                "(baseline=$baseline, measured=$measured)",
        )
    }

    private fun assertResourcesLoaded(environment: Environment) {
        assertTrue(environment.colorProfile.size > 1_000, "ICC color profile should be loaded")
        assertArrayEquals(
            "acsp".toByteArray(Charsets.US_ASCII),
            environment.colorProfile.copyOfRange(36, 40),
        )

        assertTrue(environment.images.keys.containsAll(setOf("Personikon.png", "Navlogo.png")))
        val image = checkNotNull(environment.images["Personikon.png"])
        assertTrue(image.startsWith("data:image/png;base64,"))
        val imageBytes = Base64.getDecoder().decode(image.substringAfter(','))
        assertArrayEquals(
            byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a),
            imageBytes.copyOfRange(0, 8),
        )

        val svg = checkNotNull(environment.resources["checkbox.svg"])
        assertTrue(svg.toString(Charsets.UTF_8).contains("<svg"))

        val regularFont = environment.fonts.single { it.path == "SourceSansPro-Regular.ttf" }
        val fontBytes = environment.fontsRoot.readAllBytes(regularFont.path)
        assertTrue(fontBytes.size > 1_000, "Configured font should be readable")
        assertArrayEquals(
            byteArrayOf(0x00, 0x01, 0x00, 0x00),
            fontBytes.copyOfRange(0, 4),
        )

        assertTrue(
            environment.templates.containsKey("syfosoknader" to "arbeidstakere"),
            "Expected the arbeidstakere template to be compiled",
        )
    }

    private companion object {
        const val COPY_COUNT = 20
        const val MAX_DESCRIPTOR_INCREASE = 30L
    }
}
