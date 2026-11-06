import com.android.build.api.dsl.LibraryExtension
import config.AndroidConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.kover")
}

// Applied here instead of in plugins {}: Gradle generates this script's accessors by applying
// its plugins {} to a synthetic project that can't see gradle.properties, so AGP 9 there misses
// android.builtInKotlin/newDsl=false and rejects KMP. Real modules do read them. Remove with KT-297.
apply(plugin = "com.android.library")

// Common configuration for all :core and :feature KMP modules
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // Standard iOS targets
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = project.name
            isStatic = true
        }
    }
}

extensions.configure<LibraryExtension> {
    namespace = "com.ivangarzab.kluvs.${project.name.replace("-", ".")}"
    compileSdk = AndroidConfig.COMPILE_SDK
    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
    }
}

// Support test exclusions for Native/iOS targets
tasks.withType<KotlinNativeTest>().configureEach {
    if (project.hasProperty("excludeTests")) {
        val patterns = project.property("excludeTests").toString().split(",")
        patterns.forEach { pattern ->
            // KotlinNativeTest (iOS) filtering is different from JVM Test filtering.
            // It expects a pattern like "com.package.ClassName" or "com.package.*"
            // We transform the glob pattern into something closer to what it expects.
            val filteredPattern = pattern
                .replace("**/", "")
                .replace(".class", "")
                .replace("/", ".")
            
            filter.excludeTestsMatching(filteredPattern)
        }
    }
}
