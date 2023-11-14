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

Most commonly, pdfgen-core is used with templates, fonts, additional resources, and potential test data to verify that valid PDFs get produced by the aforementioned templates.

Check GitHub releases to find the latest `release` version 
Check [Github releases](https://github.com/navikt/pdfgen-core/releases) to find the latest `release` version

In your own repository create subfolders in `templates` and `data`
```bash
mkdir {templates,data}/directory_name # directory_name can be anything, but it'll be a necessary part of the API later
````
* `templates/directory_name/` should then be populated with your .hbs-templates. the names of these templates will also decide parts of the API paths
* `data/directory_name/` should be populated with json files with names corresponding to a target .hbs-template, this can be used to test your PDFs during development of templates.

Additionally create subfolder `resources` containing additional resources which can be referred in your .hbs-templates


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

## Developing pdfgen-core

### Build and run tests
`./gradlew shadowJar`

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
