package no.nav.pdfgen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GreaterThanHelperTest : HelperTestSupport() {
    @Test
    fun `gt should not match equal values`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("small_int", -1)
                put("large_int", 1337)
                put("small_double", -1.67)
                put("large_double", 1337.67)
                put("a_string", "Adam")
                put("z_string", "Zorro")
            },
        )

        // Act
        val actual1 = handlebars
            .compileInline("{{#gt small_int small_int }}TRUE{{else}}FALSE{{/gt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual1)

        // Act
        val actual2 = handlebars
            .compileInline("{{#gt small_double small_double }}TRUE{{else}}FALSE{{/gt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual2)

        // Act
        val actual3 = handlebars
            .compileInline("{{#gt a_string a_string }}TRUE{{else}}FALSE{{/gt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual3)
    }

    @Test
    fun `gt should match when the first value is greater`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("small_int", -1)
                put("large_int", 1337)
                put("small_double", -1.67)
                put("large_double", 1337.67)
                put("a_string", "Adam")
                put("z_string", "Zorro")
            },
        )

        // Act
        val actual1 = handlebars
            .compileInline("{{#gt large_int small_int }}TRUE{{else}}FALSE{{/gt}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual1)

        // Act
        val actual2 = handlebars
            .compileInline("{{#gt large_double small_double }}TRUE{{else}}FALSE{{/gt}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual2)

        // Act
        val actual3 = handlebars
            .compileInline("{{#gt z_string a_string }}TRUE{{else}}FALSE{{/gt}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual3)
    }

    @Test
    fun `gt should not match when the first value is smaller`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("small_int", -1)
                put("large_int", 1337)
                put("small_double", -1.67)
                put("large_double", 1337.67)
                put("a_string", "Adam")
                put("z_string", "Zorro")
            },
        )

        // Act
        val actual1 = handlebars
            .compileInline("{{#gt small_int large_int }}TRUE{{else}}FALSE{{/gt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual1)

        // Act
        val actual2 = handlebars
            .compileInline("{{#gt small_double large_double }}TRUE{{else}}FALSE{{/gt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual2)

        // Act
        val actual3 = handlebars
            .compileInline("{{#gt a_string z_string }}TRUE{{else}}FALSE{{/gt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual3)
    }

    @Test
    fun `gt should reject values with different types`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("small_int", -1)
                put("large_int", 1337)
                put("small_double", -1.67)
                put("large_double", 1337.67)
                put("a_string", "Adam")
                put("z_string", "Zorro")
            },
        )

        // Act and assert
        assertThrows<Exception> {
            handlebars
                .compileInline("{{#gt int_string large_int }}TRUE{{else}}FALSE{{/gt}}")
                .apply(context)
        }
        assertThrows<Exception> {
            handlebars
                .compileInline("{{#gt small_double a_string }}TRUE{{else}}FALSE{{/gt}}")
                .apply(context)
        }
        assertThrows<Exception> {
            handlebars
                .compileInline("{{#gt small_double small_int }}TRUE{{else}}FALSE{{/gt}}")
                .apply(context)
        }
    }

    @Test
    fun `gt should reject missing or null values`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("small_int", -1)
                put("large_int", 1337)
                put("small_double", -1.67)
                put("large_double", 1337.67)
                put("a_string", "Adam")
                put("z_string", "Zorro")
            },
        )

        // Act and assert
        assertThrows<Exception> {
            handlebars
                .compileInline("{{#gt int_string noexists }}TRUE{{else}}FALSE{{/gt}}")
                .apply(context)
        }
        assertThrows<Exception> {
            handlebars
                .compileInline("{{#gt small_double null }}TRUE{{else}}FALSE{{/gt}}")
                .apply(context)
        }
        assertThrows<Exception> {
            handlebars.compileInline("{{#gt small_double }}TRUE{{else}}FALSE{{/gt}}").apply(context)
        }
    }
}
