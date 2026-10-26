import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseAppDistribution)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(projects.designsystem)
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.splashscreen)
            implementation(libs.androidx.browser)
            // Declared directly (not leaned on transitively via koin-compose) because
            // MainActivity uses koin-android's `by inject()` to grab PendingJoinCoordinator
            // for deep link intents that arrive before any composition exists.
            implementation(libs.koin.android)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.test.junit)
            implementation(libs.androidx.test.runner)
        }
        commonMain.dependencies {
            implementation(projects.shared)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin)
            implementation(libs.koin.compose)
            implementation(libs.bark)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Release signing: reads from ./keystore.properties locally (gitignored), or from
// KEYSTORE_FILE/KEYSTORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD env vars in CI.
// If neither is present, release builds stay unsigned (dev builds off main gate this).
val keystoreProperties = Properties().apply {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}

fun signingProp(propKey: String, envKey: String): String? =
    keystoreProperties.getProperty(propKey) ?: System.getenv(envKey)

// The selected BuildKonfig flavor, which decides which Supabase project — and which web app
// origin — this build points at. Declared here rather than next to the guards further down
// because `android.defaultConfig` below needs it to pick the App Links host. Defaults to
// "integration", matching core/network/build.gradle.kts.
val buildkonfigFlavorProvider: Provider<String> =
    providers.gradleProperty("buildkonfig.flavor").orElse("integration")

android {
    namespace = "com.ivangarzab.kluvs"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ivangarzab.kluvs"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 7
        versionName = "0.0.7"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Host this build claims for invite App Links (see AndroidManifest.xml). Keyed off the
        // BuildKonfig flavor rather than the build type so the verified host can never disagree
        // with InviteLink.build()'s origin — they are derived from the same switch.
        //
        // Deliberately one host per build, not both: minSdk is 24, and on pre-Android 12 devices
        // a single unverifiable host fails App Links verification for the *whole* app — declaring
        // the integration host in a production build would put prod invite links at risk.
        manifestPlaceholders["inviteLinkHost"] =
            if (buildkonfigFlavorProvider.get() == "production") "app.kluvs.com" else "app.kluvs.xyz"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        val storeFilePath = signingProp("storeFile", "KEYSTORE_FILE")
        if (storeFilePath != null) {
            create("release") {
                storeFile = file(storeFilePath)
                storePassword = signingProp("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = signingProp("keyAlias", "KEY_ALIAS")
                keyPassword = signingProp("keyPassword", "KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }

            firebaseAppDistribution {
                serviceCredentialsFile = System.getenv("FIREBASE_CREDENTIALS_FILE")
                    ?: "${System.getProperty("user.home")}/.config/firebase/kluvs-app-distribution.json"
                artifactType = "APK"
                groups = "og"
            }
        }

        // Release-candidate build: identical to `release` in signing/minification, but points at
        // the integration Supabase project instead of production — a low-stakes way to validate
        // a shipping-shaped build via Firebase before it goes to a Play Console test track.
        create("staging") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
            // Library modules (:designsystem, :shared, etc.) only declare debug/release variants —
            // fall back to their `release` variant since staging mirrors release everywhere else.
            matchingFallbacks += listOf("release")

            firebaseAppDistribution {
                serviceCredentialsFile = System.getenv("FIREBASE_CREDENTIALS_FILE")
                    ?: "${System.getProperty("user.home")}/.config/firebase/kluvs-app-distribution.json"
                artifactType = "APK"
                groups = "og"
            }
        }

        getByName("debug") {
            // https://firebase.google.com/docs/app-distribution/android/distribute-gradle?apptype=apk
            firebaseAppDistribution {
                serviceCredentialsFile = System.getenv("FIREBASE_CREDENTIALS_FILE")
                    ?: "${System.getProperty("user.home")}/.config/firebase/kluvs-app-distribution.json"
                artifactType = "APK"
                groups = "og"
//                releaseNotes = ""
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

// Guard against a build type silently pointing at the wrong Supabase project.
// `buildkonfig.flavor` and the Android build type are independent Gradle settings with no
// automatic link between them — this check makes mismatched combinations fail the build
// instead of shipping quietly wrong. `debug` is intentionally left unguarded: it defaults to
// "integration" but can be manually pointed at "production" for local testing when needed.
tasks.matching { it.name in setOf("assembleRelease", "installRelease", "bundleRelease") }.configureEach {
    val flavorProvider = buildkonfigFlavorProvider
    doFirst {
        val flavor = flavorProvider.get()
        check(flavor == "production") {
            "Refusing to run '$name' with buildkonfig.flavor=\"$flavor\". " +
                "Release builds must be run with -Pbuildkonfig.flavor=production, or the app ships pointing at the wrong Supabase project."
        }
    }
}

tasks.matching { it.name in setOf("assembleStaging", "installStaging", "bundleStaging") }.configureEach {
    val flavorProvider = buildkonfigFlavorProvider
    doFirst {
        val flavor = flavorProvider.get()
        check(flavor == "integration") {
            "Refusing to run '$name' with buildkonfig.flavor=\"$flavor\". " +
                "Staging builds must be run with -Pbuildkonfig.flavor=integration, or your Firebase testers get production data."
        }
    }
}

// Guard against silently producing an unsigned staging/release artifact. `signingConfigs`
// only creates the "release" signingConfig when a keystore is actually available (see above),
// so a missing KEYSTORE_FILE/keystore.properties otherwise fails silently: the build still
// succeeds, Firebase/Play just end up with an unsigned APK/AAB that no device can install.
// Scoped to these tasks specifically (not the signingConfigs block itself) because that block
// evaluates on every Gradle invocation, including plain local `assembleDebug`/`test` runs that
// intentionally have no keystore configured.
tasks.matching {
    it.name in setOf(
        "assembleRelease", "installRelease", "bundleRelease",
        "assembleStaging", "installStaging", "bundleStaging",
    )
}.configureEach {
    val hasReleaseSigningConfig = android.signingConfigs.findByName("release") != null
    doFirst {
        check(hasReleaseSigningConfig) {
            "Refusing to run '$name': no \"release\" signingConfig is available. " +
                "Set KEYSTORE_FILE/KEYSTORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD (CI) or keystore.properties (local), " +
                "or this task will silently produce an unsigned, uninstallable artifact."
        }
    }
}

