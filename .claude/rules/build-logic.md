---
paths:
  - "buildSrc/**"
  - "**/*.gradle.kts"
  - "gradle/libs.versions.toml"
---

# Build logic

- Every `:core:*` and `:feature:*` KMP module applies the convention plugin `kluvs.kmp.library` (`buildSrc/src/main/kotlin/kluvs.kmp.library.gradle.kts`). Put shared KMP configuration there, not in individual modules. `:designsystem` is the exception: it's Android-only Compose and doesn't use the plugin.
- Test exclusion via `-PexcludeTests=<glob,...>` is wired up in the root `build.gradle.kts`.
- Kover aggregates coverage in the root `build.gradle.kts`. A new module needs a `kover(project(":…"))` line there, or its coverage isn't counted. Reports come from the root tasks `koverHtmlReport`/`koverXmlReport` and land in `build/reports/kover/`. Global excludes: generated code (`*.BuildConfig`, `*.BuildKonfig`), `**.di`, `**.dtos`, and a few no-logic types listed inline.
- Supabase credentials come from BuildKonfig in `:core:network` (`core/network/build.gradle.kts`): `SUPABASE_URL`/`SUPABASE_KEY` for production and `TEST_SUPABASE_URL`/`TEST_SUPABASE_KEY` for tests, set in `~/.gradle/gradle.properties` or the environment. Never read those files.
