package no.nav.pdfgen.core

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import java.util.concurrent.atomic.AtomicReference

private val coreEnvironment: AtomicReference<Environment?> = AtomicReference<Environment?>(null)
private val coreMeterRegistry: AtomicReference<MeterRegistry?> = AtomicReference<MeterRegistry?>(null)
private val log = KotlinLogging.logger {}

class PDFGenCore {
    companion object {
        @Deprecated(
            message = "Pass a Micrometer MeterRegistry to init(); Simpleclient fallback will be removed in 2.0.0.",
        )
        fun init(initialEnvironment: Environment) {
            coreEnvironment.set(initialEnvironment)
            coreMeterRegistry.set(null)
        }

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
}
