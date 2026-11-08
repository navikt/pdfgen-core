package no.nav.pdfgen

import com.github.jknack.handlebars.HandlebarsException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LessThanHelperTest : HelperTestSupport() {
    @Test
    fun `lt should not match equal values`() {
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
            .compileInline("{{#lt small_int small_int }}TRUE{{else}}FALSE{{/lt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual1)

        // Act
        val actual2 = handlebars
            .compileInline("{{#lt small_double small_double }}TRUE{{else}}FALSE{{/lt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual2)

        // Act
        val actual3 = handlebars
            .compileInline("{{#lt a_string a_string }}TRUE{{else}}FALSE{{/lt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual3)
    }

    @Test
    fun `lt should not match when the first value is greater`() {
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
            .compileInline("{{#lt large_int small_int }}TRUE{{else}}FALSE{{/lt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual1)

        // Act
        val actual2 = handlebars
            .compileInline("{{#lt large_double small_double }}TRUE{{else}}FALSE{{/lt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual2)

        // Act
        val actual3 = handlebars
            .compileInline("{{#lt z_string a_string }}TRUE{{else}}FALSE{{/lt}}")
            .apply(context)

        // Assert
        assertEquals("FALSE", actual3)
    }

    @Test
    fun `lt should match when the first value is smaller`() {
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
            .compileInline("{{#lt small_int large_int }}TRUE{{else}}FALSE{{/lt}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual1)

        // Act
        val actual2 = handlebars
            .compileInline("{{#lt small_double large_double }}TRUE{{else}}FALSE{{/lt}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual2)

        // Act
        val actual3 = handlebars
            .compileInline("{{#lt a_string z_string }}TRUE{{else}}FALSE{{/lt}}")
            .apply(context)

        // Assert
        assertEquals("TRUE", actual3)
    }

    @Test
    fun `lt should reject values with different types`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("int_string", "1337")
                put("small_int", -1)
                put("large_int", 1337)
                put("small_double", -1.67)
                put("large_double", 1337.67)
                put("a_string", "Adam")
                put("z_string", "Zorro")
            },
        )

        // Act and assert
        assertThrows<HandlebarsException> {
            handlebars
                .compileInline("{{#lt int_string large_int }}TRUE{{else}}FALSE{{/lt}}")
                .apply(context)
        }
        assertThrows<HandlebarsException> {
            handlebars
                .compileInline("{{#lt small_double a_string }}TRUE{{else}}FALSE{{/lt}}")
                .apply(context)
        }
        assertThrows<HandlebarsException> {
            handlebars
                .compileInline("{{#lt small_double small_int }}TRUE{{else}}FALSE{{/lt}}")
                .apply(context)
        }
    }

    @Test
    fun `lt should reject missing or null values`() {
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
        assertThrows<HandlebarsException> {
            handlebars
                .compileInline("{{#lt int_string noexists }}TRUE{{else}}FALSE{{/lt}}")
                .apply(context)
        }
        assertThrows<HandlebarsException> {
            handlebars
                .compileInline("{{#lt small_double null }}TRUE{{else}}FALSE{{/lt}}")
                .apply(context)
        }
        assertThrows<HandlebarsException> {
            handlebars.compileInline("{{#lt small_double }}TRUE{{else}}FALSE{{/lt}}").apply(context)
        }
    }
}
