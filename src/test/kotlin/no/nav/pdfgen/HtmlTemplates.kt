package no.nav.pdfgen

/*
val testTemplateIncludedFonts: String = getResource("/html/test_template_included_fonts.html")
val testTemplateInvalidFonts: String = getResource("/html/test_template_invalid_fonts.html")

val testJpg: ByteArray = getResource("/image/test.jpg")
val testPng: ByteArray = getResource("/image/test.png")
*/

inline fun <reified T> getResource(path: String): T {
    val stream = checkNotNull(HelperTest::class.java.getResourceAsStream(path)) {
        "Test resource not found: $path"
    }

    return stream.use { input ->
        when (T::class) {
            String::class -> input.bufferedReader(Charsets.UTF_8).use { it.readText() } as T
            ByteArray::class -> input.readBytes() as T
            else -> throw UnsupportedOperationException()
        }
    }
}
