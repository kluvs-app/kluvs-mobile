pluginManagement {
    repositories {
        // Xcode Cloud only: see the comment in the root settings.gradle.kts.
        if (System.getenv("CI_BUILD_ID") != null) {
            maven("https://artifacts-caching-proxy.aws.intellij.net/plugins.gradle.org/m2/")
            maven("https://artifacts-caching-proxy.aws.intellij.net/maven-central/")
        }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}