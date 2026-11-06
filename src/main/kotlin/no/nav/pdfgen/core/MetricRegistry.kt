package no.nav.pdfgen.core

import io.micrometer.core.instrument.Timer
import io.prometheus.client.Summary

val HANDLEBARS_RENDERING_SUMMARY: Summary = Summary
    .Builder()
    .name("pdfgen_core_handlebars_rendering")
    .help("Time it takes for handlebars to render the template")
    .register()

internal fun <T> recordHandlebarsRendering(block: () -> T): T {
    val meterRegistry = PDFGenCore.meterRegistry
        ?: return HANDLEBARS_RENDERING_SUMMARY
            .startTimer()
            .use { block() }

    val timer = Timer
        .builder("pdfgen_core.handlebars.rendering")
        .description("Time it takes for handlebars to render the template")
        .publishPercentileHistogram()
        .register(meterRegistry)

    val sample = Timer.start(meterRegistry)

    return try {
        block()
    } finally {
        sample.stop(timer)
    }
}
