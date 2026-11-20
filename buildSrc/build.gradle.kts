plugins {
    `kotlin-dsl`
}

repositories {
    // Xcode Cloud only: see the comment in settings.gradle.kts.
    if (System.getenv("CI_BUILD_ID") != null) {
        maven("https://artifacts-caching-proxy.aws.intellij.net/dl.google.com/dl/android/maven2/")
        maven("https://artifacts-caching-proxy.aws.intellij.net/maven-central/")
        maven("https://artifacts-caching-proxy.aws.intellij.net/plugins.gradle.org/m2/")
    }
    google()
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
