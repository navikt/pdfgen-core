package no.nav.pdfgen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DurationHelpersTest : HelperTestSupport() {
    @Test
    fun `inc should increment a duration by one day`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars
            .compileInline("{{inc (duration \"2023-01-01\" \"2023-01-01\")}}")
            .apply(context)

        // Assert
        assertEquals("1", actual)
    }
}
