plugins {
    id("kluvs.kmp.library")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mokkery)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:auth"))
            implementation(project(":core:api"))

            implementation(libs.ktor.client.core)
            implementation(libs.supabase.functions)
            implementation(libs.supabase.storage)

            implementation(libs.room.runtime)

            implementation(libs.koin)
            implementation(libs.bark)
        }
        commonTest.dependencies {
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {


        }
        iosMain.dependencies {

        }
    }
}