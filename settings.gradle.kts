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
        mavenCentral()
    }
}

include(":composeApp")
include(":shared")
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
