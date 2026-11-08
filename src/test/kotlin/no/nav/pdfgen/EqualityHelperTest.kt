package no.nav.pdfgen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EqualityHelperTest : HelperTestSupport() {
    @Test
    fun `eq should match identical values`() {
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
        val actual1 = handlebars.compileInline("{{#eq an_int an_int }}TRUE{{/eq}}").apply(context)

        // Assert
        assertEquals("TRUE", actual1)

        // Act
        val actual2 = handlebars
            .compileInline("{{#eq string_with_int string_with_int }}TRUE{{/eq}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual2)

        // Act
        val actual3 = handlebars
            .compileInline("{{#eq string_with_double string_with_double }}TRUE{{/eq}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual3)

        // Act
        val actual4 = handlebars.compileInline("{{#eq a_string a_string }}TRUE{{/eq}}").apply(context)

        // Assert
        assertEquals("TRUE", actual4)

        // Act
        val actual5 = handlebars.compileInline("{{#eq a_double a_double }}TRUE{{/eq}}").apply(context)

        // Assert
        assertEquals("TRUE", actual5)
    }

    @Test
    fun `eq should match a number and its string representation`() {
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
        val actual = handlebars.compileInline("{{#eq an_int \"1337\" }}TRUE{{/eq}}").apply(context)

        // Assert
        assertEquals("TRUE", actual)
    }

    @Test
    fun `eq should match a string and a number in reverse order`() {
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
        val actual = handlebars.compileInline("{{#eq \"1337\" an_int }}TRUE{{/eq}}").apply(context)

        // Assert
        assertEquals("TRUE", actual)
    }

    @Test
    fun `eq should match an integer and a string field`() {
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
        val actual = handlebars.compileInline("{{#eq an_int string_with_int }}TRUE{{/eq}}").apply(context)

        // Assert
        assertEquals("TRUE", actual)
    }

    @Test
    fun `eq should match a string field and an integer in reverse order`() {
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
        val actual = handlebars.compileInline("{{#eq string_with_int an_int }}TRUE{{/eq}}").apply(context)

        // Assert
        assertEquals("TRUE", actual)
    }

    @Test
    fun `eq should match a double and a string field`() {
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
            .compileInline("{{#eq a_double string_with_double }}TRUE{{/eq}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual)
    }

    @Test
    fun `eq should match a string field and a double in reverse order`() {
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
            .compileInline("{{#eq string_with_double a_double}}TRUE{{/eq}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual)
    }

    @Test
    fun `eq should not match different values`() {
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
            .compileInline("{{#eq an_int a_double}}TRUE{{else}}FALSE{{/eq}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual)
    }

    @Test
    fun `eq should not match missing or null values`() {
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
            .compileInline("{{#eq an_int doesnt_exist}}TRUE{{else}}FALSE{{/eq}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual1)

        // Act
        val actual2 = handlebars.compileInline("{{#eq an_int null}}TRUE{{else}}FALSE{{/eq}}").apply(context)

        // Assert
        assertEquals("FALSE", actual2)
    }
}
