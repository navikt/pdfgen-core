package no.nav.pdfgen

import com.github.jknack.handlebars.HandlebarsException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PresenceAndCollectionHelpersTest : HelperTestSupport() {
    @Test
    fun `is_defined should render its body when a field exists`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply { put("someProperty", false) },
        )

        // Act
        val actual = handlebars
            .compileInline("{{#is_defined someProperty }}IS DEFINED{{/is_defined }}")
            .apply(context)

        // Assert
        assertEquals("IS DEFINED", actual)
    }

    @Test
    fun `is_defined should render no output when a field is missing`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply { put("someProperty", false) },
        )

        // Act
        val actual = handlebars
            .compileInline("{{#is_defined someOtherProperty }}IS DEFINED{{/is_defined }}")
            .apply(context)

        // Assert
        assertEquals("", actual)
    }

    @Test
    fun `contains_all should match a single requested value`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                putArray("myList").add("FIRST_VAL").add("SECOND_VAL").add("THIRD_VAL")
                putArray("emptyList")
            },
        )

        // Act
        val actual = handlebars
            .compileInline(
                "{{#contains_all myList \"FIRST_VAL\"}}FOUND!{{else}}NOTHING!{{/contains_all }}",
            ).apply(context)

        // Assert
        assertEquals("FOUND!", actual)
    }

    @Test
    fun `contains_all should match all requested values regardless of order`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                putArray("myList").add("FIRST_VAL").add("SECOND_VAL").add("THIRD_VAL")
                putArray("emptyList")
            },
        )

        // Act
        val actual = handlebars
            .compileInline(
                """{{#contains_all myList "FIRST_VAL" "THIRD_VAL" "SECOND_VAL"}}""" +
                    """FOUND!{{else}}NOTHING!{{/contains_all }}""",
            ).apply(context)

        // Assert
        assertEquals("FOUND!", actual)
    }

    @Test
    fun `contains_all should not match when a value is absent`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                putArray("myList").add("FIRST_VAL").add("SECOND_VAL").add("THIRD_VAL")
                putArray("emptyList")
            },
        )

        // Act
        val actual = handlebars
            .compileInline(
                """{{#contains_all myList "FIRST_VAL" "THIRD_VAL" "UNKNOWN"}}""" +
                    """FOUND!{{else}}NOTHING!{{/contains_all }}""",
            ).apply(context)

        // Assert
        assertEquals("NOTHING!", actual)
    }

    @Test
    fun `contains_all should not match an empty parameter`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                putArray("myList").add("FIRST_VAL").add("SECOND_VAL").add("THIRD_VAL")
                putArray("emptyList")
            },
        )

        // Act
        val actual = handlebars
            .compileInline(
                """{{#contains_all myList ""}}FOUND!{{else}}NOTHING!{{/contains_all }}""",
            ).apply(context)

        // Assert
        assertEquals("NOTHING!", actual)
    }

    @Test
    fun `contains_all should not match when no values are requested`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                putArray("myList").add("FIRST_VAL").add("SECOND_VAL").add("THIRD_VAL")
                putArray("emptyList")
            },
        )

        // Act
        val actual = handlebars
            .compileInline(
                """{{#contains_all myList}}FOUND!{{else}}NOTHING!{{/contains_all }}""",
            ).apply(context)

        // Assert
        assertEquals("NOTHING!", actual)
    }

    @Test
    fun `contains_all should handle an empty list`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                putArray("myList").add("FIRST_VAL").add("SECOND_VAL").add("THIRD_VAL")
                putArray("emptyList")
            },
        )

        // Act
        val actual = handlebars
            .compileInline(
                """{{#contains_all emptyList "UNKNOWN"}}FOUND!{{else}}NOTHING!{{/contains_all }}""",
            ).apply(context)

        // Assert
        assertEquals("NOTHING!", actual)
    }

    @Test
    fun `contains_all should reject an unknown list`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                putArray("myList").add("FIRST_VAL").add("SECOND_VAL").add("THIRD_VAL")
                putArray("emptyList")
            },
        )

        // Act and assert
        assertThrows<HandlebarsException> {
            handlebars
                .compileInline(
                    """{{#contains_all dontexist "UNKNOWN"}}FOUND!{{else}}NOTHING!{{/contains_all }}""",
                ).apply(context)
        }
    }
}
