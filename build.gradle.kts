plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.maven) apply false
    alias(libs.plugins.dokka)
}

allprojects {
    group = "dev.kourier"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}
