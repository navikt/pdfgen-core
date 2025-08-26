# pdfgen-core
Repository for `pdfgen-core`, an application written in Kotlin used to create PDFs and HTMLs

![GitHub Release](https://img.shields.io/github/v/release/navikt/pdfgen-core)


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
Check [GitHub releases](https://github.com/navikt/pdfgen-core/releases) to find the latest `release` version

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

### {{json_to_period}}

Format json with parameters `fom`, `tom`/`til` as period string

**Example data**

```json
{
  "periode": {
    "fom": "2020-03-20",
    "tom": "2021-09-23"
  }
}
```

**Params**

* `json` **{Periode}** Object with fields `fom` and `tom` or`til`
* `returns` **{List}** Formatted date (ex `20.03.2020 - 23.09.2021`)

**Example**

```handlebars
{{json_to_period periode}}
```
## Developing pdfgen-core

### Prerequisites
Make sure you have the Java JDK 21 installed
You can check which version you have installed using this command:
``` bash script
java -version
```

### Build and run tests
`./gradlew build`

### Publish to local maven repository
Run the following command
`./gradlew publishToMavenLocal` (or `./gradlew -t publishToMavenLocal` if you want to enable autobuild on changes)

This will publish `pdfgen-core` to local maven repository with version `local-build`

You can then import `pdfgen-core` to your gradle project with

`implementation("no.nav.pdfgen:pdfgen-core:local-build")`

### Release
We use default GitHub release, 
This project uses [semantic versioning](https://semver.org/) and does NOT prefix tags or release titles with `v` i.e. use `1.2.3` instead of `v1.2.3` 

see guide about how to relese:[creating release GitHub](
https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository#creating-a-release)

### Upgrading the gradle wrapper
Find the newest version of gradle here: https://gradle.org/releases/ Then run this command:

```./gradlew wrapper --gradle-version $gradleVersjon```


## 👥 Contact

This project is currently maintained by the organisation [@navikt](https://github.com/navikt).

If you need to raise an issue or question about this library, please create an issue here and tag it with the appropriate label.

For contact requests within the [@navikt](https://github.com/navikt) org, you can use the Slack channel #pdfgen

If you need to contact anyone directly, please see contributors.

## ✏️ Contributing

To get started, please fork the repo and checkout a new branch. You can then build the library with the Gradle wrapper

```shell script
./gradlew build
```

See more info in [CONTRIBUTING.md](CONTRIBUTING.md)
