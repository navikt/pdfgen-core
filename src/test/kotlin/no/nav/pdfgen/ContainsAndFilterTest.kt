package no.nav.pdfgen

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContainsAndFilterTest : HelperTestSupport() {
    @Test
    fun `contains should match an item with the requested field`() {
        // Arrange
        val template = handlebars.compile("helper_templates/contains")

        val jsonNode = jsonNodeFactory
            .objectNode()
            .apply {
                putArray("list")
                    .addObject()
                    .put("fish", "test")
            }

        // Act
        val actual = template.apply(jsonContext(jsonNode)).trim()

        // Assert
        assertEquals("IT_CONTAINS", actual)
    }

    @Test
    fun `contains should not match a false field value`() {
        // Arrange
        val template = handlebars.compile("helper_templates/contains")

        val jsonNode = jsonNodeFactory
            .objectNode()
            .apply {
                putArray("list")
                    .addObject()
                    .put("fish", false)
            }

        // Act
        val actual = template.apply(jsonContext(jsonNode)).trim()

        // Assert
        assertEquals("NO_CONTAINS", actual)
    }

    @Test
    fun `contains should not match an empty array`() {
        // Arrange
        val template = handlebars.compile("helper_templates/contains")

        val jsonNode = jsonNodeFactory.objectNode().apply { putArray("list") }

        // Act
        val actual = template.apply(jsonContext(jsonNode)).trim()

        // Assert
        assertEquals("NO_CONTAINS", actual)
    }

    @Test
    fun `contains should not match when the field is absent`() {
        // Arrange
        val template = handlebars.compile("helper_templates/contains")

        val jsonNode = jsonNodeFactory.objectNode().apply {
            putArray("list")
                .addObject()
                .put("shark", "something")
        }

        // Act
        val actual = template.apply(jsonContext(jsonNode)).trim()

        // Assert
        assertEquals("NO_CONTAINS", actual)
    }

    @Test
    fun `contains should not match a null field value`() {
        // Arrange
        val template = handlebars.compile("helper_templates/contains")

        val jsonNode = jsonNodeFactory.objectNode().apply {
            putArray("list").apply { addObject().putNull("fish") }
        }

        // Act
        val actual = template.apply(jsonContext(jsonNode)).trim()

        // Assert
        assertEquals("NO_CONTAINS", actual)
    }

    @Test
    fun `contains should match an item after a nonmatching item`() {
        // Arrange
        val template = handlebars.compile("helper_templates/contains")

        val jsonNode = jsonNodeFactory.objectNode().apply {
            putArray("list").apply {
                addObject().put("shark", "something")
                addObject().put("fish", "test")
            }
        }

        // Act
        val actual = template.apply(jsonContext(jsonNode)).trim()

        // Assert
        assertEquals("IT_CONTAINS", actual)
    }

    @Test
    fun `contains should match a later item after a null field`() {
        // Arrange
        val template = handlebars.compile("helper_templates/contains")

        val jsonNode = jsonNodeFactory.objectNode().apply {
            putArray("list").apply {
                addObject().putNull("fish")
                addObject().put("fish", "test")
            }
        }

        // Act
        val actual = template.apply(jsonContext(jsonNode)).trim()

        // Assert
        assertEquals("IT_CONTAINS", actual)
    }

    @Test
    fun `filter should select objects matching a field value`() {
        // Arrange
        @Language("Json")
        val jsonString =
            """
            {
                "roller": [
                  {
                    "type": "BARN",
                    "navn": "Barn1 Etternavn"
                  },
                  {
                    "type": "BARN",
                    "navn": "Barn2 Etternavn"
                  },
                  {
                    "type": "FORELDRE",
                    "navn": "Mor Etternavn"
                  }
                ]
            }
            """.trimIndent()

        val template = handlebars.compileInline(
            """
            {{#filter roller "type" "BARN" as |barn|}}
                  {{barn.navn}}
             {{/filter}}
            """.trimIndent(),
        )

        // Act
        val result = template.apply(jsonContext(jsonString.toJson()))

        // Act
        val actual1 = result.contains("Barn1 Etternavn")

        // Assert
        assertEquals(true, actual1)

        // Act
        val actual2 = result.contains("Barn2 Etternavn")

        // Assert
        assertEquals(true, actual2)

        // Act
        val actual3 = result.contains("Mor Etternavn")

        // Assert
        assertEquals(false, actual3)
    }

    @Test
    fun `filter should preserve order and skip objects with a missing field`() {
        // Arrange
        val template = handlebars.compileInline(
            """{{#filter rows "type" "KEEP" as |row|}}{{row.name}}|{{/filter}}""",
        )
        val context = jsonContext(
            """
                {
                  "rows": [
                    {"type": "KEEP", "name": "first"},
                    {"type": "SKIP", "name": "not-a-match"},
                    {"name": "missing"},
                    {"type": "KEEP", "name": "last"}
                  ]
                }
                """.toJson(),
        )

        // Act
        val actual = template.apply(context)

        // Assert
        assertEquals("first|last|", actual)
    }

    @Test
    fun `filter should handle nested fields and invalid path segments`() {
        // Arrange
        val template = handlebars.compileInline(
            """{{#filter rows "meta.type" "KEEP" as |row|}}{{row.name}}|{{/filter}}""",
        )
        val context = jsonContext(
            """
                {
                  "rows": [
                    {"meta": {"type": "KEEP"}, "name": "first"},
                    {"meta": {"type": "SKIP"}, "name": "not-a-match"},
                    {"name": "missing-parent"},
                    {"meta": {}, "name": "empty-parent"},
                    {"meta": null, "name": "null-parent"},
                    {"meta": "scalar", "name": "scalar-parent"},
                    {"meta": {"other": "value"}, "name": "missing-leaf"},
                    {"meta": {"type": "KEEP"}, "name": "last"}
                  ]
                }
                """.toJson(),
        )

        // Act
        val actual = template.apply(context)

        // Assert
        assertEquals("first|last|", actual)
    }

    @Test
    fun `filter should match an empty string but not missing or null fields`() {
        // Arrange
        val template = handlebars.compileInline(
            """{{#filter rows "value" "" as |row|}}{{row.name}}|{{/filter}}""",
        )
        val context = jsonContext(
            """
                {
                  "rows": [
                    {"value": "", "name": "empty"},
                    {"name": "missing"},
                    {"value": null, "name": "json-null"},
                    {"value": "text", "name": "text"}
                  ]
                }
                """.toJson(),
        )

        // Act
        val actual = template.apply(context)

        // Assert
        assertEquals("empty|", actual)
    }

    @Test
    fun `filter should match null without matching missing fields`() {
        // Arrange
        val template = handlebars.compileInline(
            """{{#filter rows "value" "null" as |row|}}{{row.name}}|{{/filter}}""",
        )
        val context = jsonContext(
            """
                {
                  "rows": [
                    {"value": null, "name": "json-null"},
                    {"name": "missing"},
                    {"value": "null", "name": "text-null"}
                  ]
                }
                """.toJson(),
        )

        // Act
        val actual1 = template.apply(context)

        // Assert
        assertEquals("json-null|text-null|", actual1)

        val nestedTemplate = handlebars.compileInline(
            """{{#filter rows "meta.value" "null" as |row|}}{{row.name}}|{{/filter}}""",
        )
        val nestedContext = jsonContext(
            """
                {
                  "rows": [
                    {"meta": {"value": null}, "name": "nested-json-null"},
                    {"name": "missing-parent"},
                    {"meta": {}, "name": "missing-leaf"}
                  ]
                }
                """.toJson(),
        )

        // Act
        val actual2 = nestedTemplate.apply(nestedContext)

        // Assert
        assertEquals("nested-json-null|", actual2)
    }

    @Test
    fun `filter should return no rows when the filter value is omitted`() {
        // Arrange
        val template = handlebars.compileInline(
            """{{#filter rows "value" as |row|}}{{row.name}}|{{/filter}}""",
        )
        val context = jsonContext(
            """
                {
                  "rows": [
                    {"name": "missing"}
                  ]
                }
                """.toJson(),
        )

        // Act
        val actual = template.apply(context)

        // Assert
        assertEquals("", actual)
    }
}
