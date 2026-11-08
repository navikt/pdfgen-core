package no.nav.pdfgen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnyOperatorTest : HelperTestSupport() {
    @Test
    fun `any should return empty output when a single field is missing`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("a", "a")
                put("b", "b")
                put("c", "c")
            },
        )

        // Act
        val actual = handlebars
            .compileInline("{{#any d}}YES{{/any}}")
            .apply(context)

        // Assert
        assertEquals("", actual)
    }

    @Test
    fun `any should render its body when a single field exists`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("a", "a")
                put("b", "b")
                put("c", "c")
            },
        )

        // Act
        val actual = handlebars
            .compileInline("{{#any a}}YES{{/any}}")
            .apply(context)

        // Assert
        assertEquals("YES", actual)
    }

    @Test
    fun `any should render its body when one of several fields exists`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("a", "a")
                put("b", "b")
                put("c", "c")
            },
        )

        // Act
        val actual = handlebars
            .compileInline("{{#any d e f a}}YES{{/any}}")
            .apply(context)

        // Assert
        assertEquals("YES", actual)
    }

    @Test
    fun `any should render its body when the first field exists`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("a", "a")
                put("b", "b")
                put("c", "c")
            },
        )

        // Act
        val actual = handlebars
            .compileInline("{{#any a d e f}}YES{{/any}}")
            .apply(context)

        // Assert
        assertEquals("YES", actual)
    }

    @Test
    fun `any should return empty output when all fields are missing`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("a", "a")
                put("b", "b")
                put("c", "c")
            },
        )

        // Act
        val actual = handlebars
            .compileInline("{{#any d e f g}}YES{{/any}}")
            .apply(context)

        // Assert
        assertEquals("", actual)
    }
}
