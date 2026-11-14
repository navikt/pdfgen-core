package no.nav.pdfgen.core

import io.micrometer.core.instrument.Timer

internal fun <T> recordHandlebarsRendering(block: () -> T): T {
    val meterRegistry = PDFGenCore.meterRegistry ?: return block()

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
