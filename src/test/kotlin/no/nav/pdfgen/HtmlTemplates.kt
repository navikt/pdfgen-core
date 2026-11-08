package no.nav.pdfgen

inline fun <reified T> getResource(path: String): T {
    val stream = checkNotNull(HelperTestSupport::class.java.getResourceAsStream(path)) {
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
