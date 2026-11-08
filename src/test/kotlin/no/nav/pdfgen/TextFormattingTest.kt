package no.nav.pdfgen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TextFormattingTest : HelperTestSupport() {
    @Test
    fun `capitalize_names should capitalize uppercase names`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{capitalize_names \"BRAGE BRUKER OLSEN\"}}").apply(context)

        // Assert
        assertEquals("Brage Bruker Olsen", actual)
    }

    @Test
    fun `capitalize_names should capitalize lowercase names`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{capitalize_names \"brage bruker olsen\"}}").apply(context)

        // Assert
        assertEquals("Brage Bruker Olsen", actual)
    }

    @Test
    fun `capitalize_names should normalize mixed-case names`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{capitalize_names \"BRage BRUker OLSEn\"}}").apply(context)

        // Assert
        assertEquals("Brage Bruker Olsen", actual)
    }

    @Test
    fun `capitalize_names should normalize alternating case`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{capitalize_names \"brAGE bruKer oLsEn\"}}").apply(context)

        // Assert
        assertEquals("Brage Bruker Olsen", actual)
    }

    @Test
    fun `capitalize_names should normalize repeated whitespace`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars
            .compileInline("{{capitalize_names \"   BRAGE   BRUKER   OLSEN    \"}}")
            .apply(context)

        // Assert
        assertEquals("Brage Bruker Olsen", actual)
    }

    @Test
    fun `capitalize_names should capitalize hyphenated names`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars
            .compileInline("{{capitalize_names \" BRAGE-BRUKER OLSEN \"}}")
            .apply(context)

        // Assert
        assertEquals("Brage-Bruker Olsen", actual)
    }

    @Test
    fun `capitalize_names should handle spaces around hyphens`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars
            .compileInline("{{capitalize_names \" BRAGE - BRUKER OLSEN \"}}")
            .apply(context)

        // Assert
        assertEquals("Brage-Bruker Olsen", actual)
    }

    @Test
    fun `capitalize_names should capitalize names containing apostrophes`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{capitalize_names \" O'SHEA OLSEN \"}}").apply(context)

        // Assert
        assertEquals("O'Shea Olsen", actual)
    }

    @Test
    fun `capitalize_names should preserve already capitalized names`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{capitalize_names \"Brage Bruker Olsen\"}}").apply(context)

        // Assert
        assertEquals("Brage Bruker Olsen", actual)
    }

    @Test
    fun `capitalize_names should preserve an already capitalized single word`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{capitalize_names \"Brage\"}}").apply(context)

        // Assert
        assertEquals("Brage", actual)
    }

    @Test
    fun `uppercase should convert lowercase text to uppercase`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{uppercase \"brage bruker olsen\"}}").apply(context)

        // Assert
        assertEquals("BRAGE BRUKER OLSEN", actual)
    }

    @Test
    fun `uppercase should convert mixed-case text to uppercase`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{uppercase \"Brage Bruker Olsen\"}}").apply(context)

        // Assert
        assertEquals("BRAGE BRUKER OLSEN", actual)
    }

    @Test
    fun `uppercase should preserve numbers while converting text`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{uppercase \"0553 Oslo\"}}").apply(context)

        // Assert
        assertEquals("0553 OSLO", actual)
    }

    @Test
    fun `lowercase should convert uppercase text to lowercase`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{lowercase \"BRAGE BRUKER OLSEN\"}}").apply(context)

        // Assert
        assertEquals("brage bruker olsen", actual)
    }

    @Test
    fun `lowercase should convert mixed-case text to lowercase`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{lowercase \"Brage Bruker Olsen\"}}").apply(context)

        // Assert
        assertEquals("brage bruker olsen", actual)
    }

    @Test
    fun `lowercase should preserve numbers while converting text`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars.compileInline("{{lowercase \"0553 Oslo\"}}").apply(context)

        // Assert
        assertEquals("0553 oslo", actual)
    }

    @Test
    fun `breaklines should convert carriage returns and newlines to HTML breaks`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars
            .compileInline("{{breaklines \"I pitty the fool \\r\\n Who doesn't br\"}}")
            .apply(context)

        // Assert
        assertEquals("I pitty the fool <br/> Who doesn&#x27;t br", actual)
    }

    @Test
    fun `breaklines should convert newlines to HTML breaks`() {
        // Arrange
        val context = jsonContext(jsonNodeFactory.objectNode())

        // Act
        val actual = handlebars
            .compileInline("{{breaklines \"I pitty the fool \\n Who doesn't br\"}}")
            .apply(context)

        // Assert
        assertEquals("I pitty the fool <br/> Who doesn&#x27;t br", actual)
    }

    @Test
    fun `concat should join parameters into one string`() {
        // Arrange
        val context = jsonContext(
            jsonNodeFactory.objectNode().apply {
                put("value1", "Value1")
                put("value2", 123123)
                put("value3", "value3")
            },
        )

        // Act
        val actual = handlebars.compileInline("{{concat value1 value2 value3 }}").apply(context)

        // Assert
        assertEquals("Value1 123123 value3", actual)
    }
}
