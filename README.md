# pdfgen-core
Repository for `pdfgen-core`, an application written in Kotlin used to create PDFs and HTMLs

## Technologies & Tools
* Kotlin
* Gradle
* Ktor
* Junit
* Handlebars
* VeraPDF-validation
* JDK 21

## Getting started

pdfgen-core is used with templates, fonts, additional resources, and potential test data to verify that valid PDFs get produced by the templates.

Check GitHub releases to find the latest `release` version 
Check [Github releases](https://github.com/navikt/pdfgen-core/releases) to find the latest `release` version

In your own repository create folders `templates`, `resources` and `data` on root of you repository
* `templates`
    Create subfolder inside `templates` folder
    ```bash
    mkdir {templates}/directory_name # directory_name can be anything, but it'll be a necessary part of the API later
    ````
  * `templates/directory_name/` should then be populated with your .hbs-templates. the names of these templates will also decide parts of the API paths
    
* `resources` should contain additional resources (ex images) which can be referred in your .hbs-templates
* `data` should contain test JSON data that can be used to verify that valid PDFs get produced by templates. `data` folder should have same subdirectory structure as `templates`. [Example](#generating-HTML-from-predefined-JSON-data-in-data-folder) how you can produce HTML and PDF with test data


### Initialize pdfgen-core
#### Initialize with custom handlebar helpers
You can initialize pdfgen-core with additional handlebar helpers:
```kotlin
PDFGenCore.init(Environment(additionalHandlebarHelpers = mapOf(
    "enum_to_readable" to Helper<String> { context, _ ->
        when(context){
            "SOME_ENUM_VALUE" -> "Human readable text"
            else -> ""
        }
    },
)))
```
#### Initialize with alternative path to `templates`, `resources` folder
```kotlin
PDFGenCore.init(
Environment(
    additionalHandlebarHelpers = mapOf(
        "enum_to_readable" to Helper<String> { context, _ ->
            when (context) {
                "BIDRAGSMOTTAKER" -> "Bidragsmottaker"
                else -> ""
            }
        },
    ),
    templateRoot = PDFGenResource("/path/to/templates"),
    resourcesRoot = PDFGenResource("/path/to/resources"),
),
)
```

#### Reload templates and resources from disk
You can reload the resources and templates from disk using the following method.
This will be especially useful for when developing templates and should be executed before generating HTML or PDF such that the updated templates and data are loaded from disk
```kotlin
PDFGenCore.reloadEnvironment()
```

### Example usage
#### Generating HTML from predefined JSON data in data-folder
```kotlin

val html: String = createHtmlFromTemplateData(template, directoryName)
```

#### Generating PDF from predefined JSON data in data-folder
```kotlin
val html: String = createHtmlFromTemplateData(template, directoryName)
val pdfBytes: ByteArray = createPDFA(html)
```

#### Generating from JSON input data
```kotlin
val html: String = createHtmlFromTemplateData(template, directoryName, jsonNode)
val pdfBytes: ByteArray = createPDFA(html)

// Or directly
val pdfBytes: ByteArray = createPDFA(template, directoryName, jsonNode)
```

## Handlerbars helpers
Example of usage of handlebar helpers defined in this library

### {{filter}}

Filter array of objects by fieldvalue

**Example data**

```json
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
```

**Params**

* `json` **{List}** List of JSON values
* `fieldname` **{String}** Fieldname used for filtering
* `value` **{String}** Value to filter by
* `returns` **{List}** Filtered list

**Example**

```handlebars
{{#filter roller "type" "BARN" as |barn|}}
   <div>{{ barn.navn }}</div>
{{/filter}}

```
## Developing pdfgen-core

### Build and run tests
`./gradlew build`

### Publish to local maven repository
Run the following command
`./gradlew publishToMavenLocal`

This will publish `pdfgen-core` to local maven repository with version `local-build`

You can then import `pdfgen-core` to your gradle project with

`implementation("no.nav.pdfgen:pdfgen-core:local-build")`
### Upgrading the gradle wrapper
Find the newest version of gradle here: https://gradle.org/releases/ Then run this command:

```./gradlew wrapper --gradle-version $gradleVersjon```


## 👥 Contact

This project is currently maintained by the organisation [@navikt](https://github.com/navikt).

If you need to raise an issue or question about this library, please create an issue here and tag it with the appropriate label.

For contact requests within the [@navikt](https://github.com/navikt) org, you can use the slack channel #pdfgen

If you need to contact anyone directly, please see contributors.

## ✏️ Contributing

To get started, please fork the repo and checkout a new branch. You can then build the library with the Gradle wrapper

```shell script
./gradlew shadowJar
```

See more info in [CONTRIBUTING.md](CONTRIBUTING.md)
