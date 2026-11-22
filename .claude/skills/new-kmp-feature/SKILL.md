---
name: new-kmp-feature
description: >
  Guided workflow for adding a brand-new KMP feature module to this project
  (a new top-level screen/area with its own feature/{name} module and
  ViewModel). Auto-loads when planning or discussing creating one of those
  from scratch. Does NOT apply to modifying or extending an existing
  screen/component within an already-existing feature module (e.g. adding
  a field, search, or widget to an existing form) — that's a normal scoped
  change, not a new feature. Enforces the 4-phase phased approach.
allowed-tools: Read, Write, Edit, Grep, Glob
---

# New KMP Feature Skill

This skill guides the creation of a new feature following the project's 4-phase KMP approach.
It enforces a stop-and-verify gate after each phase before proceeding.

---

## When Auto-Invoked (Planning Context)

Provide this structure passively to inform plans:
- Phase 1 (Data Layer) must come before Phase 2 (Feature Layer)
- Android UI lives in `composeApp/`, not in the feature module
- The iOS bridge is a single `{ViewModel}Helper.kt` in `iosMain`
- Every new feature module must be registered in `KoinHelper.kt`
- New UseCases and ViewModels use `factoryOf` in the feature's DI module

---

## When Invoked Explicitly (`/new-kmp-feature`)

### Step 1 — Interview

Ask in a single message:
1. What is the feature name? (used for module name, package, class names)
2. What does it display / what does the user do with it?
3. Does it need new data from Supabase, or does it reuse existing repositories?
4. Does it need any new local caching (Room entities)?
5. Which platform(s) are in scope? (Android only / iOS only / both)

Do NOT proceed until the user has answered all questions.

---

### Step 2 — Generate a full file plan

Based on the answers, produce a complete list of every file that will be created or modified, grouped by phase. Confirm with the user before writing anything.

Use this file map as reference:

**Phase 1 — Data Layer** (skip if reusing existing repositories)
- `core/data/src/commonMain/.../remote/api/{Feature}Service.kt`
- `core/data/src/commonMain/.../remote/api/{Feature}ServiceImpl.kt`
- `core/data/src/commonMain/.../remote/source/{Feature}RemoteDataSource.kt`
- `core/data/src/commonMain/.../remote/source/{Feature}RemoteDataSourceImpl.kt`
- `core/data/src/commonMain/.../remote/mappers/{Entity}Mappers.kt`
- `core/data/src/commonMain/.../repositories/{Feature}Repository.kt`
- `core/data/src/commonMain/.../repositories/{Feature}RepositoryImpl.kt`
- Register in `CoreDataModule.kt`

**Phase 2 — Feature Layer (shared)**
- `feature/{name}/build.gradle.kts`
- `feature/{name}/src/commonMain/.../di/{Name}FeatureModule.kt`
- `feature/{name}/src/commonMain/.../domain/{UseCase}.kt` (one per use case)
- `feature/{name}/src/commonMain/.../presentation/{Name}ViewModel.kt`
- `feature/{name}/src/commonMain/.../presentation/{Name}State.kt`
- `feature/{name}/src/commonMain/.../presentation/{Name}Models.kt`
- Register feature module in `shared/.../di/KoinHelper.kt`
- Tests: `{UseCase}Test.kt`, `{Name}ViewModelTest.kt`

**Phase 3 — Android UI**
- `composeApp/src/androidMain/.../ui/{name}/{Name}Screen.kt`
- Supporting composables as needed
- Wire into navigation in `composeApp`

**Phase 4 — iOS Bridge**
- `feature/{name}/src/iosMain/.../{Name}ViewModelHelper.kt`
- `feature/{name}/src/iosTest/.../{Name}ViewModelHelperTest.kt`

---

### Phase Execution Rules

Execute one phase at a time. After completing each phase:

1. List every file that was created or modified
2. State clearly: **"Phase N complete. Please build and verify before I continue."**
3. Wait for the user's go-ahead before starting the next phase

Never proceed to the next phase autonomously.

---

### Coding Conventions to Follow

**build.gradle.kts**
```kotlin
plugins {
    id("kluvs.kmp.library")
    alias(libs.plugins.mokkery)
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:data"))
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin)
            implementation(libs.bark)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
```

**ViewModel**
- Extends `ViewModel()`
- Single `MutableStateFlow<{Name}State>` exposed as read-only `StateFlow`
- Updates via `_state.update { it.copy(...) }`
- Parallel data loading with `async` / `await`
- No platform-specific code

**State**
- Plain `data class` with `isLoading`, `error`, and data fields
- Default values on all fields so it can be instantiated empty

**UseCases**
- One responsibility per class
- `suspend operator fun invoke(...)` pattern
- Returns `Result<T>`
- Transforms domain models into UI models — ViewModels do NOT do this

**DI module**
```kotlin
val {name}FeatureModule = module {
    factoryOf(::UseCase1)
    factoryOf(::UseCase2)
    factoryOf(::FeatureViewModel)
}
```

**iOS Helper**
```kotlin
class {Name}ViewModelHelper : KoinComponent {
    private val viewModel: {Name}ViewModel by inject()
    private val coroutineScope: CoroutineScope by inject()

    fun observeState(callback: ({Name}State) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }
    // Delegate all public ViewModel methods below
}
```

**Android Screen**
- Outer composable: receives params + `viewModel = koinViewModel()`
- Inner composable (`ScreenContent`): pure UI, receives state + lambdas
- Use `AnimatedContent` for Loading / Error / Empty / Content transitions
- Call `viewModel.loadData(id)` inside `LaunchedEffect(id)`