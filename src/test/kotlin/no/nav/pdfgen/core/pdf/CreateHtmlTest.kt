package no.nav.pdfgen.core.pdf

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.pdfgen.core.objectMapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class CreateHtmlTest {
    @Test
    fun `should render HTML without logging payloads or HTML`() {
        val logger = LoggerFactory.getLogger("no.nav.pdfgen.core.pdf.CreateHtml") as Logger
        val originalLevel = logger.level
        val appender =
            ListAppender<ILoggingEvent>().apply {
                context = logger.loggerContext
                start()
            }
        logger.level = Level.DEBUG
        logger.addAppender(appender)

        try {
            val payload =
                objectMapper.readTree(
                    """
                    {
                      "brevSporring": {
                        "customerId": "synthetic-payload-marker",
                        "fornavn": "synthetic-first",
                        "etternavn": "synthetic-last",
                        "fraDato": "01-01-2024",
                        "tilDato": "02-01-2024",
                        "dagensDato": "03-01-2024"
                      }
                    }
                    """.trimIndent(),
                )

            val htmlFromPayload = createHtml("kontrollforespoersel", "dsopkontroll", payload)
            assertTrue(htmlFromPayload?.contains("synthetic-payload-marker") == true)

            val htmlFromTemplateData = createHtmlFromTemplateData("kontrollforespoersel", "dsopkontroll")
            assertTrue(htmlFromTemplateData?.contains("fornavn etternavn") == true)

            val renderedHtml = render("dsopkontroll", "kontrollforespoersel", payload)
            assertTrue(renderedHtml?.contains("synthetic-payload-marker") == true)
            assertNull(render("dsopkontroll", "unknown-template", payload))

            val debugMessages =
                appender.list
                    .filter { it.level == Level.DEBUG }
                    .map { it.formattedMessage }
            assertFalse(
                debugMessages.any {
                    it.contains("JSON") ||
                        it.contains("Generated HTML") ||
                        it.contains("synthetic-payload-marker") ||
                        it.contains("<html>")
                },
                "Payload or rendered HTML was logged at DEBUG",
            )
        } finally {
            logger.detachAppender(appender)
            logger.level = originalLevel
            appender.stop()
        }
    }
}
