package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.net.URL
import javax.inject.Inject

/**
 * Downloads Sentry.xcframework (version from libs.versions.toml `sentryCocoa`). The iOS app links
 * it directly instead of through Swift Package Manager, which downloads every Sentry binary
 * variant (~450 MB) and kept timing out on Xcode Cloud.
 */
abstract class SetupSentryTask : DefaultTask() {

    @get:Input
    abstract val sentryVersion: Property<String>

    @get:OutputDirectory
    abstract val frameworkDestDir: DirectoryProperty

    // INJECTED SERVICES (The fix for Configuration Cache errors)
    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun setup() {
        val version = sentryVersion.get()
        println("🎯 Sentry Cocoa version: $version")

        // 2. Download
        val url = "https://github.com/getsentry/sentry-cocoa/releases/download/$version/Sentry.xcframework.zip"
        val zipFile = frameworkDestDir.get().asFile.resolve("Sentry.zip")

        println("⬇️ CI: Downloading $url...")
        // Timeouts so a stalled connection fails (and ci_post_clone.sh retries it) instead of hanging.
        val connection = URL(url).openConnection().apply {
            connectTimeout = 20_000
            readTimeout = 20_000
        }
        connection.getInputStream().use { input ->
            zipFile.outputStream().use { output -> input.copyTo(output) }
        }

        // 3. Unzip with the system unzip: Gradle's zipTree drops symlinks, which breaks the
        // xcframework's code signature, and Xcode refuses to link an unverifiable framework.
        println("📦 CI: Unzipping framework...")
        execOperations.exec {
            commandLine("unzip", "-q", "-o", zipFile.absolutePath, "-d", frameworkDestDir.get().asFile.absolutePath)
        }

        // 4. Cleanup
        zipFile.delete()
        println("✅ CI: Sentry setup complete.")
    }
}