# 📚 Kluvs 🎬🍽️

[![Full Tests](https://github.com/kluvs-app/kluvs-mobile/actions/workflows/full-tests.yml/badge.svg)](https://github.com/kluvs-app/kluvs-mobile/actions/workflows/full-tests.yml)
[![codecov](https://codecov.io/gh/kluvs-app/kluvs-mobile/branch/main/graph/badge.svg)](https://codecov.io/gh/kluvs-app/kluvs-mobile)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-blue.svg?logo=kotlin)](https://kotlinlang.org)

<p align="center">
  <img src="assets/ic_kluvs.png" alt="Kluvs Logo" width="200"/>
</p>

## ℹ️ About

**Kluvs** is a Kotlin Multiplatform mobile application for managing book clubs and reading sessions across Discord communities.

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

- **Android Studio** (latest stable)
- **Xcode** 15+ (for iOS development)
- **JDK** 17+
- **Kotlin** 2.2.0+

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/ivangarzab/kluvs-mobile.git
   cd kluvs-mobile
   ```

2. **Configure Supabase credentials**

   Create a `gradle.properties` file in your home directory (`~/.gradle/gradle.properties`) or in the project root:
   ```properties
   SUPABASE_URL=your_supabase_url
   SUPABASE_KEY=your_supabase_anon_key
   TEST_SUPABASE_URL=your_test_supabase_url
   TEST_SUPABASE_KEY=your_test_supabase_anon_key
   ```

3. **Run the Android app**
   ```bash
   ./gradlew :composeApp:assembleDebug
   ```

4. **Run the iOS app**
   - Open `iosApp/iosApp.xcodeproj` in Xcode
   - Select your target device/simulator
   - Press Run (⌘R)

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
