package no.nav.pdfgen.core

import java.util.concurrent.atomic.AtomicReference

val environment = AtomicReference(Environment())
class PDFGenCore {
    companion object {
        fun init(initialEnvironment: Environment) {
            environment.set(initialEnvironment)
        }

        fun reloadEnvironment(){
            environment.set(environment.get().copy())
        }
    }
}
