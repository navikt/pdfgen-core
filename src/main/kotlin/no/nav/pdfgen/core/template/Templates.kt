package no.nav.pdfgen.core.template

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.FileTemplateLoader
import com.github.jknack.handlebars.io.StringTemplateSource
import no.nav.pdfgen.core.PDFGenResource
import java.nio.file.Files
import kotlin.io.path.extension

typealias TemplateMap = Map<Pair<String, String>, Template>

// Close both directory levels, including when reading or compiling a template fails.
fun loadTemplates(
    templateRoot: PDFGenResource,
    additionalHandlebarHelpers: Map<String, Helper<*>>,
): TemplateMap = Files
    .list(templateRoot.getPath())
    .use { directories ->
        directories
            .filter { !Files.isHidden(it) && Files.isDirectory(it) }
            .toList()
            .flatMap { applicationDir ->
                val applicationName = applicationDir.fileName.toString()
                Files.list(applicationDir).use { templateFiles ->
                    // Collect the compiled templates here so no lazy stream escapes this use block.
                    templateFiles
                        .filter { it.fileName.extension == "hbs" }
                        .map {
                            val fileName = it.fileName.toString()
                            val templateName = fileName.substring(0..fileName.length - 5)
                            val templateBytes = Files.readAllBytes(it).toString(Charsets.UTF_8)
                            val xhtml =
                                setupHandlebars(templateRoot, additionalHandlebarHelpers)
                                    .compile(StringTemplateSource(fileName, templateBytes))
                            (applicationName to templateName) to xhtml
                        }.toList()
                }
            }.toMap()
    }

private fun setupHandlebars(
    templateRoot: PDFGenResource,
    additionalHandlebarHelpers: Map<String, Helper<*>>,
) = Handlebars(FileTemplateLoader(templateRoot.toFile())).apply {
    registerNavHelpers(this, additionalHandlebarHelpers)
    infiniteLoops(true)
}
