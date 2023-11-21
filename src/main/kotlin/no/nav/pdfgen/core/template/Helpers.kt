package no.nav.pdfgen.core.template

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import no.nav.pdfgen.core.PDFGenCore
import no.nav.pdfgen.core.domain.Periode
import no.nav.pdfgen.core.domain.PeriodeMapper
import no.nav.pdfgen.core.objectMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
val yearMonthFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.yyyy")
val dateFormatLong: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d. MMMM yyyy").withLocale(Locale.of("no", "NO"))
val datetimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
val yearMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

fun formatDateTime(formatter: DateTimeFormatter, context: CharSequence): String =
    try {
        formatter.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parseBest(context))
    } catch (e: Exception) {
        formatter.format(DateTimeFormatter.ISO_DATE_TIME.parse(context))
    }

fun formatDate(formatter: DateTimeFormatter, context: CharSequence): String {
    val dateSplit = context.split("-")
    return try {
        if (dateSplit.size == 2) formatter.format(yearMonthFormatter.parse(context))
        else formatter.format(DateTimeFormatter.ISO_DATE.parse(context))
    } catch (e: Exception) {
        formatter.format(DateTimeFormatter.ISO_DATE.parse("$context-01"))
    }
}

fun registerNavHelpers(
    handlebars: Handlebars,
    additionalHelpers: Map<String, Helper<*>> = emptyMap()
) {
    handlebars.apply {
        registerHelper(
            "iso_to_nor_date",
            Helper<String> { context, _ ->
                if (context == null) return@Helper ""
                try {
                    formatDateTime(dateFormat, context)
                } catch (e: Exception) {
                    formatDate(dateFormat, context)
                }
            },
        )

        registerHelper(
            "iso_to_nor_datetime",
            Helper<String> { context, _ ->
                if (context == null) return@Helper ""
                formatDateTime(datetimeFormat, context)
            },
        )

        registerHelper(
            "iso_to_date",
            Helper<String> { context, _ ->
                if (context == null) return@Helper ""
                formatDate(dateFormat, context)
            },
        )

        registerHelper(
            "iso_to_year_month",
            Helper<String> { context, _ ->
                if (context == null) return@Helper ""
                formatDate(yearMonthFormat, context)
            },
        )

        registerHelper(
            "iso_to_long_date",
            Helper<String> { context, _ ->
                if (context == null) return@Helper ""
                try {
                    dateFormatLong.format(DateTimeFormatter.ISO_DATE_TIME.parse(context))
                } catch (e: Exception) {
                    dateFormatLong.format(DateTimeFormatter.ISO_DATE.parse(context))
                }
            },
        )

        registerHelper(
            "duration",
            Helper<String> { context, options ->
                ChronoUnit.DAYS.between(
                    LocalDate.from(DateTimeFormatter.ISO_DATE.parse(context)),
                    LocalDate.from(DateTimeFormatter.ISO_DATE.parse(options.param(0))),
                )
            },
        )

        // Expects json-objects of the form { "fom": "2018-05-20", "tom": "2018-05-29" }
        registerHelper(
            "json_to_period",
            Helper<Any> { context, _ ->
                val jsonString: String? = when (context) {
                    is String -> context
                    else -> context?.let { objectMapper.writeValueAsString(it) }
                }
                when (jsonString) {
                    null -> return@Helper ""
                    else -> {
                        val periode: Periode = PeriodeMapper.jsonTilPeriode(jsonString)
                        if (periode.fom == null) return@Helper ""
                        return@Helper periode.fom.format(dateFormat) +
                            " - " +
                            (periode.tom?.format(dateFormat)
                                ?: periode.til?.format(dateFormat) ?: "")
                    }
                }
            },
        )
        registerHelper(
            "insert_at",
            Helper<Any> { context, options ->
                if (context == null) return@Helper ""
                val divider = options.hash<String>("divider", " ")
                options.params
                    .map { it as Int }
                    .fold(context.toString()) { v, idx ->
                        v.substring(0, idx) + divider + v.substring(idx, v.length)
                    }
            },
        )

        registerHelper(
            "eq",
            Helper<Any?> { context, options ->
                if (context?.toString() == options.param<Any?>(0)?.toString()) {
                    options.fn()
                } else {
                    options.inverse()
                }
            },
        )

        registerHelper(
            "not_eq",
            Helper<Any?> { context, options ->
                if (context?.toString() != options.param<Any?>(0)?.toString()) {
                    options.fn()
                } else {
                    options.inverse()
                }
            },
        )

        registerHelper(
            "gt",
            Helper<Comparable<Any>> { context, options ->
                val param = options.param(0) as Comparable<Any>
                if (context > param) options.fn() else options.inverse()
            },
        )

        registerHelper(
            "lt",
            Helper<Comparable<Any>> { context, options ->
                val param = options.param(0) as Comparable<Any>
                if (context < param) options.fn() else options.inverse()
            },
        )

        registerHelper(
            "safe",
            Helper<String> { context, _ ->
                if (context == null) "" else Handlebars.SafeString(context)
            },
        )

        registerHelper(
            "image",
            Helper<String> { context, _ ->
                if (context == null) "" else PDFGenCore.environment.images[context]
            },
        )

        registerHelper(
            "resource",
            Helper<String> { context, _ ->
                PDFGenCore.environment.resources[context]?.toString(Charsets.UTF_8) ?: ""
            },
        )

        registerHelper(
            "capitalize",
            Helper<String> { context, _ ->
                context?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""
            },
        )

        registerHelper(
            "capitalize_names",
            Helper<String> { context, _ ->
                if (context == null) {
                    ""
                } else {
                    Handlebars.SafeString(
                        context
                            .trim()
                            .replace("\\s+".toRegex(), " ")
                            .lowercase()
                            .capitalizeWords(" ")
                            .capitalizeWords("-")
                            .capitalizeWords("'"),
                    )
                }
            },
        )

        registerHelper(
            "uppercase",
            Helper<String> { context, _ -> context?.uppercase() ?: "" },
        )

        registerHelper(
            "inc",
            Helper<Int> { context, _ -> context + 1 },
        )

        registerHelper(
            "formatComma",
            Helper<Any> { context, _ -> context?.toString()?.replace(".", ",") ?: "" },
        )

        registerHelper(
            "any",
            Helper<Any> { first, options ->
                if ((listOf(first) + options.params).all { options.isFalsy(it) }) {
                    options.inverse()
                } else {
                    options.fn()
                }
            },
        )

        registerHelper(
            "contains_field",
            Helper<Iterable<Any>?> { list, options ->
                val checkfor = options.param(0, null as String?)

                val contains =
                    list
                        ?.map { Context.newContext(options.context, it) }
                        ?.any { ctx -> !options.isFalsy(ctx.get(checkfor)) }
                        ?: false

                if (contains) {
                    options.fn()
                } else {
                    options.inverse()
                }
            },
        )

        registerHelper(
            "contains_all",
            Helper<ArrayNode> { list, options ->
                val textValues = list.map { it.textValue() }

                val params = options.params.toList()
                val contains = if (params.isEmpty()) false else textValues.containsAll(params)

                if (contains) {
                    options.fn()
                } else {
                    options.inverse()
                }
            },
        )

        registerHelper(
            "currency_no",
            Helper<Any> { context, options ->
                if (context == null) return@Helper ""
                val withoutDecimals = options.param(0, false)

                val splitNumber = context.toString().split(".")

                // we're joining with a non-breaking space since currency values should not be split
                // across several lines
                val formattedNumber =
                    splitNumber.first().reversed().chunked(3).joinToString("\u00A0").reversed()
                if (withoutDecimals) {
                    formattedNumber
                } else {
                    val decimals =
                        splitNumber.drop(1).firstOrNull()?.let { (it + "0").substring(0, 2) }
                            ?: "00"
                    "$formattedNumber,$decimals"
                }
            },
        )

        registerHelper(
            "int_as_currency_no",
            Helper<Int> { context, _ ->
                val kr = context / 100
                val øre = context % 100

                // using .format(locale = Locale("nb")) should also do the trick, but it appears
                // this no longer works
                // so we just reuse the string-based code from above to get the format we want :)
                val formattedKr =
                    kr.toString().reversed().chunked(3).joinToString("\u00A0").reversed()
                "$formattedKr,%02d".format(øre)
            },
        )

        registerHelper(
            "string_as_currency_no",
            Helper<String> { context, _ ->
                val value = context.filter { c -> c.isDigit() }.toInt()
                val kr = value / 100
                val øre = value % 100

                val formattedKr =
                    kr.toString().reversed().chunked(3).joinToString("\u00A0").reversed()
                "$formattedKr,%02d".format(øre)
            },
        )

        registerHelper(
            "is_defined",
            Helper<Any> { context, options ->
                if (context != null) options.fn() else options.inverse()
            },
        )

        registerHelper(
            "breaklines",
            Helper<String> { context, _ ->
                if (context == null) {
                    ""
                } else {
                    val santizedText = Handlebars.Utils.escapeExpression(context)
                    val withLineBreak =
                        santizedText
                            .toString()
                            .replace("\\r\\n", "<br/>")
                            .replace("\\n", "<br/>")
                            .replace("\r\n", "<br/>")
                            .replace("\n", "<br/>")
                    Handlebars.SafeString(withLineBreak)
                }
            },
        )

        registerHelper(
            "filter",
            Helper<Iterable<Any>> { list, options ->
                val filterByField = options.param(0, null as String?)
                val filterByValue = options.param(1, null as String?)
                val buffer = options.buffer()
                if (list is JsonNode) {
                    val filteredList =
                        list.filter { getParameterValue(it, filterByField) == filterByValue }
                    filteredList.forEachIndexed { _, it -> buffer.append(options.fn(it)) }
                }

                buffer
            },
        )
        registerHelper(
            "concat",
            Helper<String> { firstValue, options ->
                val values = listOf(firstValue) + options.params
                values.joinToString(" ")
            },
        )
        registerHelper(
            "stringify",
            Helper<Any> { value, _ -> objectMapper.writeValueAsString(value) },
        )

        registerHelper(
            "now",
            Helper<Any> { _, _ -> LocalDateTime.now().toString()}
        )

        registerHelper(
            "now_date",
            Helper<Any> { _, _ -> LocalDate.now().toString()}
        )

        additionalHelpers.forEach { (t, u) -> registerHelper(t, u) }
    }
}

private fun getParameterValue(jsonNode: JsonNode, parameterPathString: String?): String {
    val parameterPath = parameterPathString?.split(".") ?: emptyList()
    var tempJsonNode = jsonNode
    parameterPath.forEach { parameter -> tempJsonNode = tempJsonNode.get(parameter) }
    return tempJsonNode.asText()
}

private fun String.capitalizeWords(wordSplitter: String) =
    this.split(wordSplitter).joinToString(wordSplitter) {
        it.trim().replaceFirstChar { it.uppercase() }
    }
