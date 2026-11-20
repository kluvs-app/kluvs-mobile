rootProject.name = "Kluvs"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        // Xcode Cloud only (CI_BUILD_ID is set there, not locally or on GitHub Actions). Since
        // 2026-11-16 its machines intermittently can't reach dl.google.com or repo.maven.apache.org
        // ("No route to host" for 10+ minutes at a time). Resolve through JetBrains' public caching
        // proxy (hosted on AWS), which mirrors both; the original repositories below stay as fallback.
        if (System.getenv("CI_BUILD_ID") != null) {
            maven("https://artifacts-caching-proxy.aws.intellij.net/dl.google.com/dl/android/maven2/") {
                name = "GoogleViaJetBrainsCache"
                mavenContent {
                    includeGroupAndSubgroups("androidx")
                    includeGroupAndSubgroups("com.android")
                    includeGroupAndSubgroups("com.google")
                }
            }
            maven("https://artifacts-caching-proxy.aws.intellij.net/maven-central/") {
                name = "MavenCentralViaJetBrainsCache"
            }
            maven("https://artifacts-caching-proxy.aws.intellij.net/plugins.gradle.org/m2/") {
                name = "GradlePluginPortalViaJetBrainsCache"
            }
        }
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
        // Xcode Cloud only: see the comment in pluginManagement above.
        if (System.getenv("CI_BUILD_ID") != null) {
            maven("https://artifacts-caching-proxy.aws.intellij.net/dl.google.com/dl/android/maven2/") {
                name = "GoogleViaJetBrainsCache"
                mavenContent {
                    includeGroupAndSubgroups("androidx")
                    includeGroupAndSubgroups("com.android")
                    includeGroupAndSubgroups("com.google")
                }
            }
            maven("https://artifacts-caching-proxy.aws.intellij.net/maven-central/") {
                name = "MavenCentralViaJetBrainsCache"
            }
        }
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
