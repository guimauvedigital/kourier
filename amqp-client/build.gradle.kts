plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ksp)
    alias(libs.plugins.maven)
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    pom {
        name.set("amqp-client")
        description.set("AMQP Client for kourier.")
        url.set("https://github.com/guimauvedigital/kourier")

        licenses {
            license {
                name.set("GPL-3.0")
                url.set("https://opensource.org/licenses/GPL-3.0")
            }
        }
        developers {
            developer {
                id.set("NathanFallet")
                name.set("Nathan Fallet")
                email.set("contact@nathanfallet.me")
                url.set("https://www.nathanfallet.me")
            }
        }
        scm {
            url.set("https://github.com/guimauvedigital/kourier.git")
        }
    }
}

kotlin {
    // Tiers are in accordance with <https://kotlinlang.org/docs/native-target-support.html>
    // Tier 1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // Tier 2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // Tier 3
    mingwX64()
    //watchosDeviceArm64() // Not supported by ktor

    // jvm & js
    jvmToolchain(21)
    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.js.ExperimentalJsExport")
            }
        }
        val commonMain by getting {
            dependencies {
                api(project(":amqp-core"))
                api(libs.ktor.network)
                api(libs.ktor.network.tls)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.tests.mockk)
            }
        }
    }
}
