package no.nav.pdfgen

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.JsonNodeValueResolver
import com.github.jknack.handlebars.context.MapValueResolver
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.pdfgen.core.Environment
import no.nav.pdfgen.core.PDFGenCore
import no.nav.pdfgen.core.objectMapper
import no.nav.pdfgen.core.template.registerNavHelpers
import org.junit.jupiter.api.BeforeEach

fun String.toJson(): JsonNode = objectMapper.readTree(this)

abstract class HelperTestSupport {
    protected val jsonNodeFactory: JsonNodeFactory = JsonNodeFactory.instance
    protected val handlebars = Handlebars(ClassPathTemplateLoader())
        .apply { registerNavHelpers(this) }

    @BeforeEach
    fun init() {
        PDFGenCore.init(
            initialEnvironment = Environment(),
            meterRegistry = SimpleMeterRegistry(),
        )
    }

    protected fun jsonContext(jsonNode: JsonNode): Context {
        println(ObjectMapper().writeValueAsString(jsonNode))
        return Context
            .newBuilder(jsonNode)
            .resolver(
                JsonNodeValueResolver.INSTANCE,
                MapValueResolver.INSTANCE,
            ).build()
    }
}
