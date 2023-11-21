package no.nav.pdfgen.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.jknack.handlebars.Helper
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.pathString
import kotlin.io.path.readBytes
import no.nav.pdfgen.core.template.loadTemplates
import no.nav.pdfgen.core.util.FontMetadata
import org.apache.pdfbox.io.IOUtils

private val log = KotlinLogging.logger {}
val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules().registerKotlinModule()

class Environment(
    val additionalHandlebarHelpers: Map<String, Helper<*>> = emptyMap(),
    val templateRoot: PDFGenResource = PDFGenResource("templates/"),
    val resourcesRoot: PDFGenResource = PDFGenResource("resources/"),
    val fontsRoot: PDFGenResource = PDFGenResource("fonts/"),
    val dataRoot: PDFGenResource = PDFGenResource("data/"),
) {
    val colorProfile: ByteArray =
        IOUtils.toByteArray(Environment::class.java.getResourceAsStream("/sRGB2014.icc"))
    val images: Map<String, String> = loadImages(resourcesRoot)
    val resources: Map<String, ByteArray> = loadResources(resourcesRoot)
    val fonts: List<FontMetadata> = objectMapper.readValue(fontsRoot.readAllBytes("config.json"))
    val templates = loadTemplates(templateRoot, additionalHandlebarHelpers)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Environment

        return colorProfile.contentEquals(other.colorProfile)
    }

    override fun hashCode(): Int {
        return colorProfile.contentHashCode()
    }

    fun copy(): Environment {
        return Environment(
            additionalHandlebarHelpers = additionalHandlebarHelpers,
            templateRoot = templateRoot,
            resourcesRoot = resourcesRoot,
            fontsRoot = fontsRoot,
            dataRoot = dataRoot,
        )
    }
}

data class PDFGenResource(val path: String) {

    private val _path: Path = Paths.get(path)

    fun readAllBytes(filename: String? = null): ByteArray {
        val filePath = filename?.let { _path.resolve(it) } ?: _path
        return if (filePath.exists()) filePath.readBytes()
        else
            Environment::class
                .java
                .classLoader
                .getResourceAsStream(filePath.pathString)!!
                .readAllBytes()
    }

    fun toFile(filename: String? = null): File = getPath(filename).toFile()

    fun getPath(filename: String? = null): Path {
        val filePath = filename?.let { _path.resolve(it) } ?: _path
        log.trace { "Reading file from path $filePath. File exists on path = ${filePath.exists()}" }
        return if (filePath.exists()) filePath
        else Path.of(Environment::class.java.classLoader.getResource(filePath.pathString)!!.toURI())
    }
}

private fun loadImages(imagesRoot: PDFGenResource) =
    Files.list(imagesRoot.getPath())
        .filter {
            val validExtensions = setOf("jpg", "jpeg", "png", "bmp", "svg")
            !Files.isHidden(it) && it.fileName.extension in validExtensions
        }
        .map {
            val fileName = it.fileName.toString()
            val extension =
                when (it.fileName.extension) {
                    "jpg" -> "jpeg" // jpg is not a valid mime-type
                    "svg" -> "svg+xml"
                    else -> it.fileName.extension
                }
            val base64string = Base64.getEncoder().encodeToString(Files.readAllBytes(it))
            val base64 = "data:image/$extension;base64,$base64string"
            fileName to base64
        }
        .toList()
        .toMap()

private fun loadResources(imagesRoot: PDFGenResource) =
    Files.list(imagesRoot.getPath())
        .filter {
            val validExtensions = setOf("svg")
            !Files.isHidden(it) && it.fileName.extension in validExtensions
        }
        .map { it.fileName.toString() to Files.readAllBytes(it) }
        .toList()
        .toMap()
