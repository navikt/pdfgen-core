package no.nav.pdfgen.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.jknack.handlebars.Helper
import no.nav.pdfgen.core.template.loadTemplates
import no.nav.pdfgen.core.util.FontMetadata
import org.apache.pdfbox.io.IOUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.pathString
import kotlin.io.path.readBytes

val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules().registerKotlinModule()

class Environment(
    val additionalHandlebarHelpers: Map<String, Helper<*>> = emptyMap(),
    val templateRoot: PDFGenResource = PDFGenResource("templates/"),
    val resourcesRoot: PDFGenResource = PDFGenResource("resources/"),
    val fontsRoot: PDFGenResource = PDFGenResource("fonts/"),
    val dataRoot: PDFGenResource = PDFGenResource("data/"),
) {
    // Reloading opens a new resource stream; close it after reading the profile.
    val colorProfile: ByteArray = Environment::class.java.getResourceAsStream("/sRGB2014.icc")?.use {
        IOUtils.toByteArray(it)
    } ?: throw RuntimeException("Could not find ICC color profile")
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

    override fun hashCode(): Int = colorProfile.contentHashCode()

    fun copy() = Environment(
        additionalHandlebarHelpers = additionalHandlebarHelpers,
        templateRoot = templateRoot,
        resourcesRoot = resourcesRoot,
        fontsRoot = fontsRoot,
        dataRoot = dataRoot,
    )
}

data class PDFGenResource(
    val path: Path,
) {
    constructor(path: String) : this(Paths.get(path))

    fun readAllBytes(filename: String? = null): ByteArray {
        val filePath = filename?.let { path.resolve(it) } ?: path
        return if (filePath.exists()) {
            filePath.readBytes()
        } else {
            // Classpath streams need explicit closing, unlike the disk-path readBytes helper.
            Environment::class
                .java
                .classLoader
                .getResourceAsStream(filePath.pathString)
                ?.use { it.readAllBytes() }
                ?: throw RuntimeException("Could not find file at path $filePath")
        }
    }

    fun toFile(filename: String? = null): File = getPath(filename).toFile()

    fun getPath(filename: String? = null): Path {
        val filePath = filename?.let { path.resolve(it) } ?: path
        return if (filePath.exists()) {
            filePath
        } else {
            Environment::class.java.classLoader.getResource(filePath.pathString)?.let {
                Path.of(it.toURI())
            }
                ?: throw RuntimeException("Could not find file at path $filePath")
        }
    }
}

// Terminal operations such as toList do not close Files.list; use prevents leaks on reload.
private fun loadImages(imagesRoot: PDFGenResource) = Files.list(imagesRoot.getPath()).use { files ->
    files
        .filter {
            val validExtensions = setOf("jpg", "jpeg", "png", "bmp", "svg")
            !Files.isHidden(it) && it.fileName.extension in validExtensions
        }.map {
            val fileName = it.fileName.toString()
            val extension = when (it.fileName.extension) {
                "jpg" -> "jpeg" // jpg is not a valid mime-type
                "svg" -> "svg+xml"
                else -> it.fileName.extension
            }
            val base64string = Base64.getEncoder().encodeToString(Files.readAllBytes(it))
            val base64 = "data:image/$extension;base64,$base64string"
            fileName to base64
        }.toList()
        .toMap()
}

private fun loadResources(imagesRoot: PDFGenResource) = Files.list(imagesRoot.getPath()).use { files ->
    // Read and collect the resources before use closes the directory stream.
    files
        .filter {
            val validExtensions = setOf("svg")
            !Files.isHidden(it) && it.fileName.extension in validExtensions
        }.map { it.fileName.toString() to Files.readAllBytes(it) }
        .toList()
        .toMap()
}
