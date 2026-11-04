plugins {
    id("kluvs.kmp.library")
    alias(libs.plugins.mokkery)
}

// The convention plugin's name-derived namespace collides with :core:auth, which AGP 9 rejects.
android {
    namespace = "com.ivangarzab.kluvs.feature.auth"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:presentation"))
            implementation(project(":core:auth"))

            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin)
            implementation(libs.bark)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        iosMain.dependencies {
            // Inherited from convention plugin
        }
    }
}