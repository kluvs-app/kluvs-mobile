plugins {
    `kotlin-dsl`
}

repositories {
    google()
    // CI only: see the Maven Central mirror note in the root settings.gradle.kts.
    if (System.getenv("CI") != null) {
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
    }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.multiplatform.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
}
