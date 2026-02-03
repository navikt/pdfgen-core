import com.diffplug.gradle.spotless.SpotlessTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


group = "no.nav.pdfgen"
version = properties["version"]?.takeIf { it is String && it.isNotEmpty() && it != "unspecified" } ?: "local-build"
println(version)

val javaVersion = JvmTarget.JVM_25

val handlebarsVersion = "4.3.1"
val jacksonVersion = "2.21.0"
val jaxbVersion = "4.0.6"
val jaxbApiVersion = "2.3.1"
val jsoupVersion = "1.22.1"
val logbackVersion = "1.5.25"
val logstashEncoderVersion = "9.0"
val openHtmlToPdfVersion = "1.1.37"
val prometheusVersion = "0.16.0"
val junitJupiterVersion = "6.0.2"
val verapdfVersion = "1.28.2"
val ktfmtVersion = "0.44"
val kotlinloggerVersion = "7.0.14"

///Due to vulnerabilities
val commonsIoVersion = "2.21.0"
val rhinoVersion = "1.9.0"


plugins {
    kotlin("jvm") version "2.3.0"
    id("com.diffplug.spotless") version "8.1.0"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("maven-publish")
    id("java-library")
}

kotlin {
    compilerOptions {
        jvmTarget.set(javaVersion)
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform {}
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

   withType<Exec> {
        println("⚈ ⚈ ⚈ Running Add Pre Commit Git Hook Script on Build ⚈ ⚈ ⚈")
        commandLine("cp", "./.scripts/pre-commit", "./.git/hooks")
        println("✅ Added Pre Commit Git Hook Script.")

    }

    withType<SpotlessTask> {
        spotless{
            kotlin { ktfmt(ktfmtVersion).kotlinlangStyle() }
            check {
                dependsOn("spotlessApply")
            }
        }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("com.github.jknack:handlebars:$handlebarsVersion")
    implementation("com.github.jknack:handlebars-jackson2:$handlebarsVersion")
    implementation("io.github.openhtmltopdf:openhtmltopdf-pdfbox:$openHtmlToPdfVersion")
    implementation("io.github.openhtmltopdf:openhtmltopdf-slf4j:$openHtmlToPdfVersion")
    implementation("io.github.openhtmltopdf:openhtmltopdf-svg-support:$openHtmlToPdfVersion")
    constraints {
        implementation("commons-io:commons-io:$commonsIoVersion") {
            because("Due to vulnerabilities in io.github.openhtmltopdf:openhtmltopdf-svg-support")
        }
    }

    implementation("org.jsoup:jsoup:$jsoupVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbVersion")

    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")

    implementation("org.verapdf:validation-model-jakarta:$verapdfVersion")
    constraints {
        implementation("org.mozilla:rhino:$rhinoVersion") {
            because("Due to vulnerabilities in org.verapdf:validation-model-jakarta")
        }
    }
    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinloggerVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
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
                        name.set("MIT License")
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
