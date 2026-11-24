plugins {
    // Plugins provided by buildSrc are available project-wide via convention plugins.
    // Only declare plugins here that are NOT in buildSrc dependencies.
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebaseAppDistribution) apply false
    id("org.jetbrains.kotlinx.kover")
}

// Global test exclusion support - allows excluding tests via -PexcludeTests="**/*IntegrationTest.class"
subprojects {
    tasks.withType<Test>().configureEach {
        filter {
            // Don't fail when --tests filter matches zero tests
            isFailOnNoMatchingTests = false
        }
        if (project.hasProperty("excludeTests")) {
            val exclusions = project.property("excludeTests").toString().split(",")
            exclude(exclusions)
        }
    }
}

// Kover dependencies for aggregation - pulls coverage from all modules
dependencies {
    kover(project(":core:model"))
    kover(project(":core:network"))
    kover(project(":core:database"))
    kover(project(":core:auth"))
    kover(project(":core:data"))
    kover(project(":core:presentation"))
    kover(project(":feature:auth"))
    kover(project(":feature:clubs"))
    kover(project(":feature:member"))
    kover(project(":feature:books"))
    kover(project(":feature:settings"))
    kover(project(":feature:join"))
    kover(project(":shared"))
}
kover {
    reports {
        total {
            filters {
                excludes {
                    classes("*.BuildConfig", "*.BuildKonfig") // Filter out generated code
                    classes("**.SentrySetupKt") // Sentry configuration - side-effect only, hard to unit test
                    classes("**.ScreenState*") // Sealed interface - no logic to test
                    classes("**.LoginNavigation*") // Sealed class - no logic to test
                    classes("**.AuthMode*") // Enum - no logic to test
                    classes("**.CacheTTL") // Constants object - no logic to test
                    packages("**.di", "**.dtos") // Filter out DI and DTOs globally
                }
            }
            xml { onCheck = true }
            html { onCheck = true }
        }
    }
}
