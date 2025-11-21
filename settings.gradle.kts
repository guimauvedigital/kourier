pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "kourier"
include(":amqp-core")
include(":amqp-client")
include(":amqp-client-robust")
include(":amqp-client-opentelemetry")
