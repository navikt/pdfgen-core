package no.nav.pdfgen.core

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.pdfgen.core.pdf.render
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MetricsTest {
    @Test
    fun `render should record to Micrometer when registry is supplied`() {
        val meterRegistry = SimpleMeterRegistry()
        PDFGenCore.init(Environment(), meterRegistry)

        render(
            directoryName = "dsopkontroll",
            template = "kontrollforespoersel",
            jsonNode = JsonNodeFactory.instance.objectNode(),
        )

        val timer = meterRegistry.find("pdfgen_core.handlebars.rendering").timer()
        assertNotNull(timer)
        assertEquals(1L, timer?.count())
    }
}
