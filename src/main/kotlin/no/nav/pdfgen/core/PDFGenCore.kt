package no.nav.pdfgen.core

import io.micrometer.core.instrument.MeterRegistry
import java.util.concurrent.atomic.AtomicReference

private val coreEnvironment: AtomicReference<Environment> = AtomicReference(null)
private val coreMeterRegistry: AtomicReference<MeterRegistry> = AtomicReference(null)

/**
 * Holds process-wide environment and metrics configuration for pdfgen-core.
 *
 * Call [init] to provide application-specific settings. Without initialization, the first
 * environment access creates a default environment, and metrics are skipped until a registry is set.
 */
object PDFGenCore {
    /**
     * Sets the environment and metrics registry used by pdfgen-core.
     *
     * Replaces a default environment created by [environment], if one exists.
     *
     * @param initialEnvironment environment configuration to use
     * @param meterRegistry registry for pdfgen-core metrics
     */
    @JvmStatic
    fun init(
        initialEnvironment: Environment,
        meterRegistry: MeterRegistry,
    ) {
        coreEnvironment.set(initialEnvironment)
        coreMeterRegistry.set(meterRegistry)
    }

    /**
     * Reloads templates and resources using the current environment's configured paths.
     *
     * Creates a default environment if [init] has not run.
     */
    @Suppress("unused")
    @JvmStatic
    fun reloadEnvironment() {
        coreEnvironment.updateAndGet { currentEnv ->
            currentEnv?.copy() ?: Environment()
        }
    }

    /**
     * Returns the configured environment, creating a default on first access if [init] has not run.
     * A later call to [init] replaces that default.
     */
    internal val environment: Environment
        get() = coreEnvironment.updateAndGet { current ->
            current ?: Environment()
        }

    /**
     * Returns the registry provided to [init], or `null` until initialization.
     * Callers skip metrics when no registry is available.
     */
    internal val meterRegistry: MeterRegistry?
        get() = coreMeterRegistry.get()
}
