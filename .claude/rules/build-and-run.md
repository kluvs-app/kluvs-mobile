# Building, running, and CI

## Who builds
- Don't run Gradle after making edits — not `installDebug`, not even a compile-only check — unless the user asks. They usually have Android Studio building at the same time, and a parallel CLI build fights over the same daemon and slows their machine down.
- If you can't tell whether the user is building right now, ask before you start a build.
- When a build *is* wanted to verify Android changes, run `./gradlew :composeApp:installDebug --quiet --console=plain` directly. It already compiles, so a standalone `compileDebugKotlinAndroid` first is a wasted run. Use compile-only tasks only for modules with no APK (e.g. `:core:data`, `:feature:clubs`).
- iOS: a one-off `xcodebuild` correctness check right after Swift edits is fine. Once the user is iterating on device themselves (reporting screenshots, "let's try again"), stop starting builds, because concurrent builds in the same DerivedData lock each other. Make the edits and wait for their report.

- Every Gradle invocation uses `--quiet --console=plain` (or redirects output) to keep token use down.
- A `PreToolUse` hook (`.claude/hooks/gradle-daemon-cleanup.sh`) kills idle Gradle/Kotlin daemons before each Gradle command to stop RAM bloat across parallel sessions.

## Iterating on visual changes
- While the user is iterating on one platform, change only that platform. Mirror to the other platform and to `design-system` docs only after they confirm the result. Touching every platform mid-experiment makes each "try something else" N times as expensive.

## iOS CI is Xcode Cloud
- Fix Xcode Cloud problems inside Xcode Cloud (`iosApp/ci_scripts/`, project config, workflow settings).
- Never suggest enabling the disabled `ios-tests` job in `.github/workflows/unit-tests.yml`, or any GitHub Actions macOS runner, as a fix or fallback. macOS runners cost too much, and Xcode Cloud also runs the archive/TestFlight release pipeline, so green PR checks on Actions don't fix anything.
- **Known outage (since 2026-11-16):** Xcode Cloud's `ci_post_clone.sh` often fails downloading Gradle dependencies (`Could not GET … Read timed out`, `No route to host`), even through the JetBrains caching proxy. This is infra, not the PR's code, and the iOS tests never ran. Check `get_issues`/`get_logs` first and say so in one line. Don't propose network workarounds unless asked. The user finds these failures very annoying and doesn't want them rediagnosed as code problems each time. To verify iOS locally, run `./gradlew iosSimulatorArm64Test -PexcludeTests="**/*IntegrationTest*" -Pbuildkonfig.flavor=integration --continue`, or the user runs the tests from Xcode. Re-check whether this is still happening; remove this bullet once it's resolved.
