import utils.getPropertyOrEnvVar

plugins {
    id("kluvs.kmp.library")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildKonfig)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
                api(project(":core:model"))

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)

                api(libs.supabase)
                implementation(libs.supabase.functions)
                implementation(libs.supabase.auth)
                implementation(libs.supabase.storage)

                implementation(libs.bark)
                implementation(libs.koin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

buildkonfig {
    packageName = "com.ivangarzab.kluvs.network"
    exposeObjectWithName = "BuildKonfig"

    defaultConfigs {
        // Production Supabase credentials
        val supabaseUrl: String = getPropertyOrEnvVar("SUPABASE_URL")
        val supabaseKey: String = getPropertyOrEnvVar("SUPABASE_KEY")
        require(supabaseUrl.isNotEmpty() && supabaseKey.isNotEmpty()) {
            "Make sure to provide the SUPABASE_URL and SUPABASE_KEY in your global gradle.properties file."
        }
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SUPABASE_KEY",
            supabaseKey
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SUPABASE_URL",
            supabaseUrl
        )

        // Testing Supabase credentials
        val testSupabaseUrl: String = getPropertyOrEnvVar("TEST_SUPABASE_URL")
        val testSupabaseKey: String = getPropertyOrEnvVar("TEST_SUPABASE_KEY")
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "TEST_SUPABASE_KEY",
            testSupabaseKey
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "TEST_SUPABASE_URL",
            testSupabaseUrl
        )
    }
}