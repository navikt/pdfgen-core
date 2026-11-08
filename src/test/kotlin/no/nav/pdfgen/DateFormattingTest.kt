package no.nav.pdfgen

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DateFormattingTest : HelperTestSupport() {
    @Nested
    inner class PeriodFormatting {
        @Test
        fun `json_to_period should convert to date with fom and tom`() {
            // Arrange
            @Language("Json")
            val jsonString =
                """
                {
                    "periode": {
                      "fom": "2020-03-20",
                      "tom": "2021-09-23"
                    }
                }
                """.trimIndent()

            val template = handlebars.compileInline(
                """
                {{json_to_period periode}}
                """.trimIndent(),
            )

            // Act
            val result = template.apply(jsonContext(jsonString.toJson()))

            // Assert
            assertEquals("20.03.2020 - 23.09.2021", result)
        }

        @Test
        fun `json_to_period should convert to date with fom and til`() {
            // Arrange
            @Language("Json")
            val jsonString =
                """
                {
                    "periode": {
                      "fom": "2020-03-20",
                      "til": "2021-09-23"
                    }
                }
                """.trimIndent()

            val template = handlebars.compileInline(
                """
                {{json_to_period periode}}
                """.trimIndent(),
            )

            // Act
            val result = template.apply(jsonContext(jsonString.toJson()))

            // Assert
            assertEquals("20.03.2020 - 23.09.2021", result)
        }

        @Test
        fun `json_to_period should convert to date when the period is a JSON string`() {
            // Arrange
            @Language("Json")
            val jsonString =
                """
                {
                    "periode": "{\"fom\": \"2019-08-09\", \"tom\": \"2019-08-10\"}"
                }
                """.trimIndent()

            val template = handlebars.compileInline(
                """
                {{json_to_period periode}}
                """.trimIndent(),
            )

            // Act
            val result = template.apply(jsonContext(jsonString.toJson()))

            // Assert
            assertEquals("09.08.2019 - 10.08.2019", result)
        }

        @Test
        fun `json_to_period should leave the end date empty when tom is missing`() {
            // Arrange
            @Language("Json")
            val jsonString =
                """
                {
                    "periode": {
                      "fom": "2020-03-20"
                    }
                }
                """.trimIndent()

            val template = handlebars.compileInline(
                """
                {{json_to_period periode}}
                """.trimIndent(),
            )

            // Act
            val result = template.apply(jsonContext(jsonString.toJson()))

            // Assert
            assertEquals("20.03.2020 - ", result)
        }
    }

    @Nested
    inner class DateFormatting {
        @Test
        fun `iso_to_nor_datetime_seconds should format a timestamp with seconds`() {
            // Arrange
            val context = jsonContext(
                jsonNodeFactory.objectNode().apply {
                    put("timestamp", "2020-03-03T10:15:30")
                    put("timestampLong", "2020-10-03T10:15:30")
                    put("date", "2020-02-12")
                },
            )

            // Act
            val actual = handlebars.compileInline("{{ iso_to_nor_datetime_seconds timestamp }}").apply(context)

            // Assert
            assertEquals("03.03.2020 10:15:30", actual)
        }

        @Test
        fun `iso_to_nor_datetime should format a timestamp with hours and minutes`() {
            // Arrange
            val context = jsonContext(
                jsonNodeFactory.objectNode().apply {
                    put("timestamp", "2020-03-03T10:15:30")
                    put("timestampLong", "2020-10-03T10:15:30")
                    put("date", "2020-02-12")
                },
            )

            // Act
            val actual = handlebars.compileInline("{{ iso_to_nor_datetime timestamp }}").apply(context)

            // Assert
            assertEquals("03.03.2020 10:15", actual)
        }

        @Test
        fun `iso_to_nor_date should format a timestamp as a short date`() {
            // Arrange
            val context = jsonContext(
                jsonNodeFactory.objectNode().apply {
                    put("timestamp", "2020-03-03T10:15:30")
                    put("timestampLong", "2020-10-03T10:15:30")
                    put("date", "2020-02-12")
                },
            )

            // Act
            val actual = handlebars.compileInline("{{ iso_to_nor_date timestamp }}").apply(context)

            // Assert
            assertEquals("03.03.2020", actual)
        }

        @Test
        fun `iso_to_nor_date should format a date without a time component`() {
            // Arrange
            val context = jsonContext(
                jsonNodeFactory.objectNode().apply {
                    put("date", "2020-03-03")
                    put("dateWithoutDay", "2020-03")
                },
            )

            // Act
            val actual1 = handlebars.compileInline("{{ iso_to_nor_date date }}").apply(context)

            // Assert
            assertEquals("03.03.2020", actual1)

            // Act
            val actual2 = handlebars.compileInline("{{ iso_to_nor_date dateWithoutDay }}").apply(context)

            // Assert
            assertEquals("01.03.2020", actual2)
        }

        @Test
        fun `iso_to_year_month should format a date with or without a day`() {
            // Arrange
            val context = jsonContext(
                jsonNodeFactory.objectNode().apply {
                    put("date", "2020-03")
                    put("dateWithDay", "2020-03-09")
                },
            )

            // Act
            val actual1 = handlebars.compileInline("{{ iso_to_year_month date }}").apply(context)

            // Assert
            assertEquals("03.2020", actual1)

            // Act
            val actual2 = handlebars.compileInline("{{ iso_to_year_month dateWithDay }}").apply(context)

            // Assert
            assertEquals("03.2020", actual2)
        }

        @Test
        fun `iso_to_long_date should format a timestamp as a long date`() {
            // Arrange
            val context = jsonContext(
                jsonNodeFactory.objectNode().apply {
                    put("timestamp", "2020-03-03T10:15:30")
                    put("timestampLong", "2020-10-03T10:15:30")
                    put("date", "2020-02-12")
                },
            )

            // Act
            val actual = handlebars.compileInline("{{ iso_to_long_date timestampLong }}").apply(context)

            // Assert
            assertEquals("3. oktober 2020", actual)
        }

        @Test
        fun `iso_to_long_date should format a date as a long date`() {
            // Arrange
            val context = jsonContext(
                jsonNodeFactory.objectNode().apply {
                    put("timestamp", "2020-03-03T10:15:30")
                    put("timestampLong", "2020-10-03T10:15:30")
                    put("date", "2020-02-12")
                },
            )

            // Act
            val actual = handlebars.compileInline("{{ iso_to_long_date date }}").apply(context)

            // Assert
            assertEquals("12. februar 2020", actual)
        }
    }
}
