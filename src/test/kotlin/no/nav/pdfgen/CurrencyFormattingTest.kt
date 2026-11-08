package no.nav.pdfgen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CurrencyFormattingTest : HelperTestSupport() {
    @Test
    fun `currency_no should format decimal amounts`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("beløp", 1337.69)
                put("beløp_single_decimal", 1337.6)
                put("beløp_integer", 9001)
                put("beløp_stort", 1337420.69)
                put("beløp_ganske_liten_integer", 1)
                put("beløp_kjempeliten_integer", 0)
                put("beløp_liten_integer", 10)
                put("beløp_stor_integer", 1000001)
                put("beløp_kjempestor_integer", Int.MAX_VALUE)
                put("beløp_liten_string", "0")
                put("beløp_stor_string", "1000001")
            },
        )

        // Act
        val actual1 = handlebars.compileInline("{{ currency_no beløp }}").apply(context)

        // Assert
        assertEquals("1 337,69", actual1)

        // Act
        val actual2 = handlebars.compileInline("{{ currency_no beløp_single_decimal }}").apply(context)

        // Assert
        assertEquals("1 337,60", actual2)

        // Act
        val actual3 = handlebars.compileInline("{{ currency_no beløp_integer }}").apply(context)

        // Assert
        assertEquals("9 001,00", actual3)

        // Act
        val actual4 = handlebars.compileInline("{{ currency_no beløp_stort }}").apply(context)

        // Assert
        assertEquals("1 337 420,69", actual4)
    }

    @Test
    fun `currency_no should format amounts without decimals`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("beløp", 1337.69)
                put("beløp_single_decimal", 1337.6)
                put("beløp_integer", 9001)
                put("beløp_stort", 1337420.69)
                put("beløp_ganske_liten_integer", 1)
                put("beløp_kjempeliten_integer", 0)
                put("beløp_liten_integer", 10)
                put("beløp_stor_integer", 1000001)
                put("beløp_kjempestor_integer", Int.MAX_VALUE)
                put("beløp_liten_string", "0")
                put("beløp_stor_string", "1000001")
            },
        )

        // Act
        val actual1 = handlebars.compileInline("{{ currency_no beløp true }}").apply(context)

        // Assert
        assertEquals("1 337", actual1)

        // Act
        val actual2 = handlebars.compileInline("{{ currency_no beløp_stort true }}").apply(context)

        // Assert
        assertEquals("1 337 420", actual2)
    }

    @Test
    fun `int_as_currency_no should format integer values`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("beløp", 1337.69)
                put("beløp_single_decimal", 1337.6)
                put("beløp_integer", 9001)
                put("beløp_stort", 1337420.69)
                put("beløp_ganske_liten_integer", 1)
                put("beløp_kjempeliten_integer", 0)
                put("beløp_liten_integer", 10)
                put("beløp_stor_integer", 1000001)
                put("beløp_kjempestor_integer", Int.MAX_VALUE)
                put("beløp_liten_string", "0")
                put("beløp_stor_string", "1000001")
            },
        )

        // Act
        val actual1 = handlebars.compileInline("{{ int_as_currency_no beløp_integer }}").apply(context)

        // Assert
        assertEquals("90,01", actual1)

        // Act
        val actual2 = handlebars.compileInline("{{ int_as_currency_no beløp_liten_integer }}").apply(context)

        // Assert
        assertEquals("0,10", actual2)

        // Act
        val actual3 = handlebars
            .compileInline("{{ int_as_currency_no beløp_kjempeliten_integer }}")
            .apply(context)

        // Assert
        assertEquals("0,00", actual3)

        // Act
        val actual4 = handlebars
            .compileInline("{{ int_as_currency_no beløp_ganske_liten_integer }}")
            .apply(context)

        // Assert
        assertEquals("0,01", actual4)

        // Act
        val actual5 = handlebars.compileInline("{{ int_as_currency_no beløp_stor_integer }}").apply(context)

        // Assert
        assertEquals("10 000,01", actual5)

        // Act
        val actual6 = handlebars
            .compileInline("{{ int_as_currency_no beløp_kjempestor_integer }}")
            .apply(context)

        // Assert
        assertEquals("21 474 836,47", actual6)
    }

    @Test
    fun `string_as_currency_no should format numeric strings`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("beløp", 1337.69)
                put("beløp_single_decimal", 1337.6)
                put("beløp_integer", 9001)
                put("beløp_stort", 1337420.69)
                put("beløp_ganske_liten_integer", 1)
                put("beløp_kjempeliten_integer", 0)
                put("beløp_liten_integer", 10)
                put("beløp_stor_integer", 1000001)
                put("beløp_kjempestor_integer", Int.MAX_VALUE)
                put("beløp_liten_string", "0")
                put("beløp_stor_string", "1000001")
            },
        )

        // Act
        val actual1 = handlebars
            .compileInline("{{ string_as_currency_no beløp_liten_string }}")
            .apply(context)

        // Assert
        assertEquals("0,00", actual1)

        // Act
        val actual2 = handlebars
            .compileInline("{{ string_as_currency_no beløp_stor_string }}")
            .apply(context)

        // Assert
        assertEquals("10 000,01", actual2)
    }

    @Test
    fun `currency_no should format negative numeric values`() {
        // Arrange
        val values = jsonNodeFactory.objectNode().apply {
            put("negative_integer", -123)
            put("negative_integer_without_decimals", -123)
            put("negative_grouped_integer", -123456)
            put("negative_decimal", -1234.56)
            put("negative_small_decimal", -0.5)
            put("negative_truncated_decimal", -1234.567)
        }
        val context = jsonContext(values)

        listOf(
            "negative_integer" to "-123,00",
            "negative_integer_without_decimals" to "-123",
            "negative_grouped_integer" to "-123\u00A0456,00",
            "negative_decimal" to "-1\u00A0234,56",
            "negative_small_decimal" to "-0,50",
            "negative_truncated_decimal" to "-1\u00A0234,56",
        ).forEach { (field, expected) ->
            val template = if (field == "negative_integer_without_decimals") {
                "{{ currency_no $field true }}"
            } else {
                "{{ currency_no $field }}"
            }

            // Act
            val actual = handlebars.compileInline(template).apply(context)

            // Assert
            assertEquals(expected, actual, "currency_no $field")
        }
    }

    @Test
    fun `int_as_currency_no should format negative values and integer limits`() {
        // Arrange
        val values = jsonNodeFactory.objectNode().apply {
            put("negative_one_fifty", -150)
            put("negative_fifty", -50)
            put("negative_one", -1)
            put("negative_one_hundred", -100)
            put("negative_twelve_thousand_three_hundred", -12300)
            put("negative_grouped", -123456789)
            put("minimum_integer", Int.MIN_VALUE)
            put("zero", 0)
            put("maximum_integer", Int.MAX_VALUE)
        }
        val context = jsonContext(values)

        listOf(
            "negative_one_fifty" to "-1,50",
            "negative_fifty" to "-0,50",
            "negative_one" to "-0,01",
            "negative_one_hundred" to "-1,00",
            "negative_twelve_thousand_three_hundred" to "-123,00",
            "negative_grouped" to "-1\u00A0234\u00A0567,89",
            "minimum_integer" to "-21\u00A0474\u00A0836,48",
            "zero" to "0,00",
            "maximum_integer" to "21\u00A0474\u00A0836,47",
        ).forEach { (field, expected) ->
            // Act
            val actual = handlebars.compileInline("{{ int_as_currency_no $field }}").apply(context)

            // Assert
            assertEquals(expected, actual, "int_as_currency_no $field")
        }
    }

    @Test
    fun `string_as_currency_no should format negative and grouped values`() {
        // Arrange
        val values = jsonNodeFactory.objectNode().apply {
            put("negative_one_fifty", "-150")
            put("negative_fifty", "-50")
            put("negative_one", "-1")
            put("negative_one_hundred", "-100")
            put("negative_twelve_thousand_three_hundred", "-12300")
            put("negative_grouped", "-123456789")
            put("minimum_integer", "-2147483648")
            put("maximum_integer", "2147483647")
            put("negative_zero", "-0")
            put("grouped_digits_only", "1 234,50")
            put("whitespace_around_negative", " \t-150\n")
        }
        val context = jsonContext(values)

        listOf(
            "negative_one_fifty" to "-1,50",
            "negative_fifty" to "-0,50",
            "negative_one" to "-0,01",
            "negative_one_hundred" to "-1,00",
            "negative_twelve_thousand_three_hundred" to "-123,00",
            "negative_grouped" to "-1\u00A0234\u00A0567,89",
            "minimum_integer" to "-21\u00A0474\u00A0836,48",
            "maximum_integer" to "21\u00A0474\u00A0836,47",
            "negative_zero" to "0,00",
            "grouped_digits_only" to "1\u00A0234,50",
            "whitespace_around_negative" to "-1,50",
        ).forEach { (field, expected) ->
            // Act
            val actual = handlebars.compileInline("{{ string_as_currency_no $field }}").apply(context)

            // Assert
            assertEquals(expected, actual, "string_as_currency_no $field")
        }
    }

    @Test
    fun `string_as_currency_no should reject values outside the integer range`() {
        // Arrange
        val values = jsonNodeFactory.objectNode().apply {
            put("too_large", "2147483648")
            put("too_small", "-2147483649")
        }
        val context = jsonContext(values)

        // Act and assert
        assertThrows<Exception> {
            handlebars.compileInline("{{ string_as_currency_no too_large }}").apply(context)
        }
        assertThrows<Exception> {
            handlebars.compileInline("{{ string_as_currency_no too_small }}").apply(context)
        }
    }
}
