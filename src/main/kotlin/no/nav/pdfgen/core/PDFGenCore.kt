package no.nav.pdfgen.core

import io.micrometer.core.instrument.MeterRegistry
import java.util.concurrent.atomic.AtomicReference

private val coreEnvironment: AtomicReference<Environment> = AtomicReference(null)
private val coreMeterRegistry: AtomicReference<MeterRegistry> = AtomicReference(null)

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
                current ?: Environment()
            }!!

        internal val meterRegistry: MeterRegistry?
            get() = coreMeterRegistry.get()

        @Suppress("unused")
        fun reloadEnvironment() {
            coreEnvironment.updateAndGet { currentEnv ->
                currentEnv?.copy() ?: Environment()
            }
        }
    }
}
