package no.nav.pdfgen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InequalityHelperTest : HelperTestSupport() {
    @Test
    fun `not_eq should not match identical values`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("an_int", 1337)
                put("a_double", 1337.67)
                put("string_with_int", "1337")
                put("string_with_double", "1337.67")
                put("a_string", "imma string")
            },
        )

        // Act
        val actual1 = handlebars
            .compileInline("{{#not_eq an_int an_int }}TRUE{{else}}FALSE{{/not_eq}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual1)

        // Act
        val actual2 = handlebars
            .compileInline(
                "{{#not_eq string_with_int string_with_int }}TRUE{{else}}FALSE{{/not_eq}}",
            ).apply(context)

        // Assert
        assertEquals("FALSE", actual2)

        // Act
        val actual3 = handlebars
            .compileInline(
                "{{#not_eq string_with_double string_with_double }}TRUE{{else}}FALSE{{/not_eq}}",
            ).apply(context)

        // Assert
        assertEquals("FALSE", actual3)

        // Act
        val actual4 = handlebars
            .compileInline("{{#not_eq a_string a_string }}TRUE{{else}}FALSE{{/not_eq}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual4)

        // Act
        val actual5 = handlebars
            .compileInline("{{#not_eq a_double a_double }}TRUE{{else}}FALSE{{/not_eq}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual5)
    }

    @Test
    fun `not_eq should match different content`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("an_int", 1337)
                put("a_double", 1337.67)
                put("string_with_int", "1337")
                put("string_with_double", "1337.67")
                put("a_string", "imma string")
            },
        )

        // Act
        val actual = handlebars.compileInline("{{#not_eq an_int \"1338\" }}TRUE{{/not_eq}}").apply(context)

        // Assert
        assertEquals("TRUE", actual)
    }

    @Test
    fun `not_eq should match different content in reverse order`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("an_int", 1337)
                put("a_double", 1337.67)
                put("string_with_int", "1337")
                put("string_with_double", "1337.67")
                put("a_string", "imma string")
            },
        )

        // Act
        val actual = handlebars.compileInline("{{#not_eq \"1338\" an_int }}TRUE{{/not_eq}}").apply(context)

        // Assert
        assertEquals("TRUE", actual)
    }

    @Test
    fun `not_eq should match different values`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("an_int", 1337)
                put("a_double", 1337.67)
                put("string_with_int", "1337")
                put("string_with_double", "1337.67")
                put("a_string", "imma string")
            },
        )

        // Act
        val actual = handlebars
            .compileInline("{{#not_eq an_int a_double}}TRUE{{else}}FALSE{{/not_eq}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual)
    }

    @Test
    fun `not_eq should match missing or null values`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("an_int", 1337)
                put("a_double", 1337.67)
                put("string_with_int", "1337")
                put("string_with_double", "1337.67")
                put("a_string", "imma string")
            },
        )

        // Act
        val actual1 = handlebars
            .compileInline("{{#not_eq an_int doesnt_exist}}TRUE{{else}}FALSE{{/not_eq}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual1)

        // Act
        val actual2 = handlebars
            .compileInline("{{#not_eq an_int null}}TRUE{{else}}FALSE{{/not_eq}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual2)
    }
}
