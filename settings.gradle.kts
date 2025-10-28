pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Plugins
            version("kotlin", "2.1.21")
            plugin("multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("kover", "org.jetbrains.kotlinx.kover").version("0.8.3")
            plugin("detekt", "io.gitlab.arturbosch.detekt").version("1.23.8")
            plugin("dokka", "org.jetbrains.dokka").version("2.0.0")
            plugin("ksp", "com.google.devtools.ksp").version("2.1.21-2.0.2")
            plugin("maven", "com.vanniktech.maven.publish").version("0.30.0")

            // Kaccelero
            version("kaccelero", "0.6.7")
            library("kaccelero-core", "dev.kaccelero", "core").versionRef("kaccelero")

            // Ktor
            version("ktor", "3.1.3")
            library("ktor-network", "io.ktor", "ktor-network").versionRef("ktor")
            library("ktor-network-tls", "io.ktor", "ktor-network-tls").versionRef("ktor")

            // Others
            library("kotlinx-io", "org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
        }
    }
}

rootProject.name = "kourier"
include(":amqp-core")
include(":amqp-client")
include(":amqp-client-robust")
