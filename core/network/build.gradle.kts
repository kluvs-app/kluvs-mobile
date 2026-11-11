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
            implementation(libs.ktor.client.mock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

// BuildKonfig evaluates every `defaultConfigs(flavor)` block below at configuration time,
// regardless of which flavor `buildkonfig.flavor` actually selects — so without this check,
// every Gradle invocation would require credentials for BOTH flavors, not just the active one.
val selectedBuildkonfigFlavor: String =
    providers.gradleProperty("buildkonfig.flavor").getOrElse("integration")

buildkonfig {
    packageName = "com.ivangarzab.kluvs.network"
    exposeObjectWithName = "BuildKonfig"

    defaultConfigs {
        // Testing Supabase credentials (used by integration tests, independent of flavor)
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

    // Selected via the `buildkonfig.flavor` Gradle property (see gradle.properties),
    // e.g. `./gradlew build -Pbuildkonfig.flavor=production`. Defaults to "integration".
    defaultConfigs("integration") {
        val supabaseUrl: String = getPropertyOrEnvVar("SUPABASE_INTEGRATION_URL")
        val supabaseKey: String = getPropertyOrEnvVar("SUPABASE_INTEGRATION_KEY")
        if (selectedBuildkonfigFlavor == "integration") {
            require(supabaseUrl.isNotEmpty() && supabaseKey.isNotEmpty()) {
                "Make sure to provide SUPABASE_INTEGRATION_URL and SUPABASE_INTEGRATION_KEY in your global gradle.properties file."
            }
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
    }

    defaultConfigs("production") {
        // Production Supabase credentials
        val supabaseUrl: String = getPropertyOrEnvVar("SUPABASE_URL")
        val supabaseKey: String = getPropertyOrEnvVar("SUPABASE_KEY")
        if (selectedBuildkonfigFlavor == "production") {
            require(supabaseUrl.isNotEmpty() && supabaseKey.isNotEmpty()) {
                "Make sure to provide the SUPABASE_URL and SUPABASE_KEY in your global gradle.properties file."
            }
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
    }
}