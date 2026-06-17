import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import tasks.SetupSentryTask
import utils.getPropertyOrEnvVar

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlinx.kover")
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.sentry)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true

            export(project(":core:model"))
            export(project(":core:auth"))
            export(project(":core:data"))
            export(project(":core:presentation"))
            export(project(":feature:auth"))
            export(project(":feature:clubs"))
            export(project(":feature:member"))
            export(project(":feature:settings"))
            export(libs.bark)
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:auth"))
            api(project(":core:data"))
            api(project(":core:presentation"))
            implementation(project(":core:network"))

            api(project(":feature:auth"))
            api(project(":feature:clubs"))
            api(project(":feature:member"))
            api(project(":feature:settings"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            api(libs.androidx.lifecycle.viewmodel)

            implementation(libs.koin)
            implementation(libs.bark)

        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
        }
        iosMain.dependencies {

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.koin.test)
        }
    }
}

android {
    namespace = "com.ivangarzab.kluvs.shared"
    //noinspection GradleDependency
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

buildkonfig {
    packageName = "com.ivangarzab.kluvs.shared"
    exposeObjectWithName = "BuildKonfig"

    defaultConfigs {
        val sentryDns = getPropertyOrEnvVar("SENTRY_DNS")
        require(sentryDns.isNotEmpty()) {
            "Make sure to provide the SENTRY_DNS in your global gradle.properties file."
        }
        buildConfigField(
            Type.STRING,
            "SENTRY_DNS",
            sentryDns
        )
        buildConfigField(Type.BOOLEAN, "IS_DEBUG", "false")
    }
    defaultConfigs("debug") {
        buildConfigField(Type.BOOLEAN, "IS_DEBUG", "true")
    }

}

val setupSentryTask = tasks.register<SetupSentryTask>("setupSentryForCi") {
    group = "ci"
    description = "Downloads Sentry binary matching Package.resolved for CI environments"

    // Define INPUT: Where is your Package.resolved?
    val resolvedPath = rootProject.file("iosApp/Kluvs.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved")
    packageResolvedFile.set(resolvedPath)

    // Define OUTPUT: Where should the framework go?
    // We put it in the root 'build' folder to keep the root project clean
    val outputDir = rootProject.layout.buildDirectory.dir("sentry-binary")
    frameworkDestDir.set(outputDir)

    // Safety: Only run if the output doesn't exist yet (Gradle handles this via outputs, but explicit check doesn't hurt)
    onlyIf { !outputDir.get().asFile.exists() }
}

// 2. Configure the Plugin
sentryKmp {
    autoInstall {
        enabled = true
        linker {
            enabled = true
            xcodeprojPath = rootProject.file("iosApp/Kluvs.xcodeproj").absolutePath
            // Check the specific frameworks output location
            val ciFrameworkDir = setupSentryTask.get().frameworkDestDir.get().asFile.resolve("Sentry.xcframework")
            if (ciFrameworkDir.exists()) {
                // If the CI task downloaded it, use it.
                frameworkPath.set(ciFrameworkDir.absolutePath)
            }
        }
    }
}

// Override linker behavior for test binaries to avoid Swift compatibility errors
// This makes Sentry symbols optional/undefined at link time for tests only
kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            // For test binaries: make undefined symbols non-fatal
            // This prevents "swiftCompatibility56 not found" linker errors in CI
            if (name.contains("Test", ignoreCase = true)) {
                linkerOpts("-Wl,-U,__swift_FORCE_LOAD_\$_swiftCompatibility56")
                linkerOpts("-Wl,-U,__swift_FORCE_LOAD_\$_swiftCompatibilityConcurrency")
            }
        }
    }
}
