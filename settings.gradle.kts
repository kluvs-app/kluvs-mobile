rootProject.name = "Kluvs"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        // CI only: Xcode Cloud can't reliably reach repo.maven.apache.org, so try Google's
        // Maven Central mirror first. mavenCentral() below stays as the fallback.
        if (System.getenv("CI") != null) {
            maven("https://maven-central.storage-download.googleapis.com/maven2/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        // CI only: see the note in pluginManagement above.
        if (System.getenv("CI") != null) {
            maven("https://maven-central.storage-download.googleapis.com/maven2/")
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":shared")
include(":designsystem")
// core
include(":core:model")
include(":core:api")
include(":core:network")
include(":core:database")
include(":core:auth")
include(":core:data")
include(":core:presentation")
// features
include(":feature:auth")
include(":feature:clubs")
include(":feature:member")
include(":feature:settings")
include(":feature:books")
include(":feature:join")
