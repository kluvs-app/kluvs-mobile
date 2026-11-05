# 📚 Kluvs 🎬🍽️

[![Full Tests](https://github.com/kluvs-app/kluvs-mobile/actions/workflows/full-tests.yml/badge.svg)](https://github.com/kluvs-app/kluvs-mobile/actions/workflows/full-tests.yml)
[![codecov](https://codecov.io/gh/kluvs-app/kluvs-mobile/branch/main/graph/badge.svg)](https://codecov.io/gh/kluvs-app/kluvs-mobile)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-blue.svg?logo=kotlin)](https://kotlinlang.org)

<p align="center">
  <img src="assets/ic_kluvs.png" alt="Kluvs Logo" width="200"/>
</p>

## ℹ️ About

**Kluvs** is a Kotlin Multiplatform mobile application for managing book clubs and reading sessions. Clubs can optionally link to a Discord server for community features via the companion bot, but Discord is not required — clubs work standalone through the app or web dashboard.

## ✨ Features

- 📖 **Book Club Management** - Create and join book clubs
- 👥 **Member Profiles** - Track participation
- 📅 **Session Tracking** - Keep up with reading schedules and discussions
- 🌐 **Cross-Platform** - Native apps for Android and iOS
- 🤖 **Companion Bot** - Discord companion bot available 
- 🔄 **Real-time Sync** - Powered by Supabase for live updates

## 🏗️ Tech Stack

- **Kotlin Multiplatform** - Shared business logic across platforms
- **Compose Multiplatform** - Modern declarative UI
- **Supabase** - Backend-as-a-Service for data and real-time features
- **Ktor** - Networking and API communication
- **Koin** - Dependency injection
- **[barK](https://github.com/ivangarzab/barK)** - Logging strategy for KMP
- **Kover** - Code coverage
- **Mokkery** - Testing framework

## 📂 Project Structure

```
kluvs-mobile/
├── composeApp/           # Android UI (Compose Multiplatform)
├── iosApp/               # iOS application entry point
├── shared/               # iOS framework export + DI + AppCoordinator
├── core/
│   ├── model/            # Domain models (User, Club, Member, etc.)
│   ├── network/          # Supabase client configuration
│   ├── auth/             # Authentication business logic
│   ├── data/             # Repositories and data sources
│   └── presentation/     # Shared UI utilities
├── feature/
│   ├── auth/             # Authentication UI
│   ├── clubs/            # Club details UI
│   └── member/           # Profile/stats UI
└── docs/                 # Architecture documentation
```

See `docs/MODULE_GRAPH.md` for the full dependency graph.

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (latest stable) with the Kotlin Multiplatform plugin
- **Xcode** 15+ (for iOS development), signed into your Apple Developer account
- **JDK** 17+
- **Ruby** 3.3 + Bundler (only for `fastlane`, i.e. Play Console uploads)
- **Supabase CLI** (`brew install supabase/tap/supabase`) — only for integration tests against a local backend (see [Testing](#-testing))

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/ivangarzab/kluvs-mobile.git
   cd kluvs-mobile
   ```

2. **Configure Gradle properties**

   Add these to `~/.gradle/gradle.properties` (never commit them). Every property can alternatively be supplied as an environment variable of the same name, which is how CI does it.

   | Property | Needed for |
   |---|---|
   | `SUPABASE_URL`, `SUPABASE_KEY` | Production flavor (`-Pbuildkonfig.flavor=production`) |
   | `SUPABASE_INTEGRATION_URL`, `SUPABASE_INTEGRATION_KEY` | Integration/staging flavor — the **default** when no flavor is set |
   | `TEST_SUPABASE_URL`, `TEST_SUPABASE_KEY` | Integration tests (local Supabase is `http://127.0.0.1:54321`) |
   | `SENTRY_DNS` | Crash reporting; the `shared` module fails the build if it's empty |

   The Supabase values come from each project's dashboard (Project Settings → API). Use the anon/publishable key, never the service-role key.

3. **Android SDK location**

   Android Studio writes `local.properties` (with `sdk.dir`) on first sync. It's gitignored.

4. **Run the Android app**
   ```bash
   ./gradlew :composeApp:installDebug
   ```

5. **Run the iOS app**
   - Open `iosApp/Kluvs.xcodeproj` in Xcode and set your signing team
   - Select the `iosApp` or `Kluvs Staging` scheme and a device/simulator
   - Press Run (⌘R)

   Both schemes are shared in the repo, so Android Studio generates its iOS run configurations from them automatically.

### Shared run configurations

Gradle run configurations (tests, Kover reports, Firebase distribution) are committed under `.run/` and appear in Android Studio automatically. `.idea/` is gitignored, so use "Store as project file" for any new shared configuration. That writes it to `.run/`.

### Release & distribution setup (maintainers only)

None of this is needed to build and run the app locally.

- **Release signing (Android):** either a gitignored `keystore.properties` in the project root with `storeFile`, `storePassword`, `keyAlias`, `keyPassword`, or the env vars `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. The `.jks` keystore itself is gitignored; get it from your secure backup.
- **Firebase App Distribution:** a service-account JSON at `~/.config/firebase/kluvs-app-distribution.json`, or point `FIREBASE_CREDENTIALS_FILE` at it. Used by the `appDistributionUpload*` tasks (the `Distribute` run configurations). `composeApp/google-services.json` is checked in.
- **Play Console upload:** `bundle install`, then `bundle exec fastlane deploy_internal aab_path:<path>`. This needs a Play Store JSON key (CI secret `PLAY_STORE_JSON_KEY`).
- **Sentry:** `sentry.properties` is gitignored; recreate it from your Sentry project settings.
- **CI secrets (GitHub Actions):** `SUPABASE_*`, `SUPABASE_INTEGRATION_*`, `TEST_SUPABASE_*`, `SENTRY_DNS`, `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `FIREBASE_APP_DISTRIBUTION_CREDENTIALS_JSON`, `PLAY_STORE_JSON_KEY`, `CODECOV_TOKEN`, `BACKEND_READ_TOKEN`, `API_SPEC_FETCH_PAT`.

## 🧪 Testing

### Run Unit Tests
```bash
# All modules (excludes integration tests)
./gradlew testDebugUnitTest -PexcludeTests="**/*IntegrationTest.class"
```

### Run Integration Tests (requires local Supabase)
```bash
./gradlew :shared:testDebugUnitTest --tests "*IntegrationTest"
```

### Generate Coverage Report
```bash
./gradlew :shared:koverHtmlReport
```
Reports are generated in `shared/build/reports/kover/html/`

## 🔄 CI/CD

The project uses GitHub Actions for continuous integration:

- **Unit Tests** - Fast feedback on every PR
- **Full Tests Suite** - Full test suite with Supabase on push to `main`
- **Code Coverage** - Tracked via Codecov

## 🙏 Acknowledgments

- Built with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- Powered by [Supabase](https://supabase.com)
- Backend API: [kluvs-backend](https://github.com/ivangarzab/kluvs-backend)
- Discord companion bot: [quill-bot](https://github.com/ivangarzab/quill-bot)
- KMP Logging: [barK](https://github.com/ivangarzab/barK)

---

<p align="center"><i>Made with 🖤️ using Kotlin Multiplatform</i></p>
