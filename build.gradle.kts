import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "no.nav.pdfgen"
version = providers.gradleProperty("version").orNull
    ?.takeIf { it.isNotEmpty() && it != "unspecified" }
    ?: "local-build"
println(version)

val javaVersion = 25

val handlebarsVersion = "4.5.8"
val handlebarsJackson2Version = "4.3.1"
val jacksonVersion = "2.22.4"
val jackson3Version = "3.2.4"
val jaxbVersion = "4.0.9"
val jsoupVersion = "1.23.2"
val logbackVersion = "1.6.6"
val openHtmlToPdfVersion = "1.1.105"
val prometheusVersion = "0.16.0"
val micrometerVersion = "1.17.2"
val junitJupiterVersion = "6.1.3"
val verapdfVersion = "1.30.3"
val kotlinloggerVersion = "8.0.4"

// /Due to vulnerabilities
val commonsIoVersion = "2.22.0"
val rhinoVersion = "1.9.2"

plugins {
    id("java-library")
    kotlin("jvm") version "2.4.21"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("com.github.ben-manes.versions") version "0.66.0"
    id("maven-publish")
}

kotlin {
    jvmToolchain(javaVersion)
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

ktlint { version.set("1.8.0") }

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Dependency alignment
    implementation(platform("tools.jackson:jackson-bom:$jackson3Version"))
    constraints {
        implementation("org.mozilla:rhino:$rhinoVersion") {
            because("Due to vulnerabilities in org.verapdf:validation-model-jakarta")
        }
        implementation("commons-io:commons-io:$commonsIoVersion") {
            because("Due to vulnerabilities in io.github.openhtmltopdf:openhtmltopdf-svg-support")
        }
    }

    // PDF generation and templates
    api("com.github.jknack:handlebars:$handlebarsVersion")
    implementation("com.github.jknack:handlebars-jackson2:$handlebarsJackson2Version")
    implementation("io.github.openhtmltopdf:openhtmltopdf-pdfbox:$openHtmlToPdfVersion")
    implementation("io.github.openhtmltopdf:openhtmltopdf-svg-support:$openHtmlToPdfVersion")

    // JSON serialization
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    // Runtime dependencies
    runtimeOnly("io.github.openhtmltopdf:openhtmltopdf-slf4j:$openHtmlToPdfVersion")
    runtimeOnly("org.jsoup:jsoup:$jsoupVersion")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:$jaxbVersion")

    // Logging and metrics
    // Deprecated compatibility API; remove with the Simpleclient fallback in 2.0.0.
    api("io.prometheus:simpleclient:$prometheusVersion")
    api("io.micrometer:micrometer-core:$micrometerVersion")
    implementation("org.verapdf:validation-model-jakarta:$verapdfVersion")
    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinloggerVersion")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }

                scm {
                    connection = "scm:git:https://github.com/navikt/pdfgen-core.git"
                    developerConnection = "scm:git:https://github.com/navikt/pdfgen-core.git"
                    url = "https://github.com/navikt/pdfgen-core"
                }
            }
        }
    }

    repositories {
        maven {
            name = "pdfgen-core"
            description = "Bibliotek som inneholder kode for å generere PDF"
            url = uri("https://maven.pkg.github.com/navikt/pdfgen-core")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_PASSWORD")
            }
        }
    }
}
