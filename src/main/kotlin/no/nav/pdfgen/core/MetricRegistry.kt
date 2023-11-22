package no.nav.pdfgen.core

import io.prometheus.client.Summary

val HANDLEBARS_RENDERING_SUMMARY: Summary =
    Summary.Builder()
        .name("pdfgen_core_handlebars_rendering")
        .help("Time it takes for handlebars to render the template")
        .register()
val OPENHTMLTOPDF_RENDERING_SUMMARY: Summary =
    Summary.Builder()
        .name("pdfgen_core_openhtmltopdf_rendering_summary")
        .help("Time it takes to render a PDF")
        .labelNames("application_name", "template_type")
        .register()
