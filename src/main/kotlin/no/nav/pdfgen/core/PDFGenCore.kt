package no.nav.pdfgen.core

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.atomic.AtomicReference

private val coreEnvironment: AtomicReference<Environment?> = AtomicReference<Environment?>(null)
private val log = KotlinLogging.logger {}

class PDFGenCore {
    companion object {
        fun init(initialEnvironment: Environment) {
            coreEnvironment.set(initialEnvironment)
        }

        val environment: Environment
            get() =
                coreEnvironment.updateAndGet { current ->
                    current ?: Environment().also { log.debug { "Creating default Environment" } }
                }!!

        fun reloadEnvironment() {
            log.debug { "Reloading environment" }
            coreEnvironment.updateAndGet { currentEnv -> currentEnv?.copy() ?: Environment() }
        }
    }
}
