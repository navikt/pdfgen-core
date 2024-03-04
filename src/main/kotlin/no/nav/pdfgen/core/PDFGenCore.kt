package no.nav.pdfgen.core

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.atomic.AtomicReference

private val coreEnvironment = AtomicReference(Environment())
private val log = KotlinLogging.logger {}

class PDFGenCore {
    companion object {
        fun init(initialEnvironment: Environment) {
            coreEnvironment.set(initialEnvironment)
        }

        val environment: Environment
            get() = coreEnvironment.get()

        fun reloadEnvironment() {
            log.debug { "Reloading environment" }
            coreEnvironment.set(coreEnvironment.get().copy())
        }
    }
}
