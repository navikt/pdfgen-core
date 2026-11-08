package no.nav.pdfgen.core

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import java.util.concurrent.atomic.AtomicReference

private val coreEnvironment: AtomicReference<Environment> = AtomicReference(null)
private val coreMeterRegistry: AtomicReference<MeterRegistry> = AtomicReference(null)

private val log = KotlinLogging.logger {}

object PDFGenCore {
    fun init(
        initialEnvironment: Environment,
        meterRegistry: MeterRegistry,
    ) {
        coreEnvironment.set(initialEnvironment)
        coreMeterRegistry.set(meterRegistry)
    }

    val environment: Environment
        get() = coreEnvironment.updateAndGet { current ->
            current ?: Environment().also {
                log.debug { "Creating default Environment" }
            }
        }!!

    internal val meterRegistry: MeterRegistry?
        get() = coreMeterRegistry.get()

    @Suppress("unused")
    fun reloadEnvironment() {
        log.debug { "Reloading environment" }
        coreEnvironment.updateAndGet { currentEnv ->
            currentEnv?.copy() ?: Environment()
        }
    }
}
