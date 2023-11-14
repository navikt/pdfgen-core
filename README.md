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
