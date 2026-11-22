---
name: koin-watcher
description: >
  Koin dependency injection rules for this project. Auto-loads when creating
  new classes, registering dependencies, discussing DI, Koin modules, injection,
  ViewModels, UseCases, repositories, or any code that needs to be wired up.
user-invocable: false
---

# Koin Watcher

When any new injectable class is created, it MUST be registered in the correct Koin module. Forgetting this causes runtime crashes — Koin fails silently at compile time.

## Module Ownership — Register Here

| What you're adding | Module file | Variable name |
|---|---|---|
| Network infrastructure (`SupabaseClient`, connectivity/offline status) | `core/network/src/commonMain/.../di/CoreNetworkModule.kt` | `coreNetworkModule` |
| Room database instance | `actual val databaseModule` in `core/data/src/{androidMain,iosMain}/.../di/DatabaseModule.{android,ios}.kt` (included by `coreDataModule`) | `databaseModule` |
| New Service, DataSource, Repository | `core/data/src/commonMain/.../di/CoreDataModule.kt` | `coreDataModule` |
| New auth-related class | `core/auth/src/commonMain/.../di/CoreAuthModule.kt` | `coreAuthModule` |
| New shared presentation utility | `core/presentation/src/commonMain/.../di/CorePresentationModule.kt` | `corePresentationModule` |
| New feature ViewModel or UseCase | `feature/{name}/src/commonMain/.../di/{Name}FeatureModule.kt` | `{name}FeatureModule` |
| Platform-specific (Android/iOS only) | `actual val` in the appropriate `androidMain` or `iosMain` DI file | — |

## Scopes — Use the Right One

- **`single { }` / `singleOf(::Class)`** — One instance shared app-wide. Use for: Clients, Services, DataSources, Repositories, Database.
- **`factory { }` / `factoryOf(::Class)`** — New instance every time it's requested. Use for: ViewModels, UseCases.

**Prefer `factoryOf(::Class)` and `singleOf(::Class)`** — Koin auto-injects constructor parameters. Only use the `single { }` / `factory { }` lambda form when manual wiring is needed (e.g., passing a specific parameter or named qualifier).

## New Feature Module Checklist

When a new feature module is created, two things must happen:

1. Create `feature/{name}/src/commonMain/.../di/{Name}FeatureModule.kt`
2. Add `{name}FeatureModule` to the list in `shared/src/commonMain/.../di/KoinHelper.kt`

If step 2 is missed, the entire feature module is invisible to Koin at runtime.

## Platform-Specific Modules

When a dependency has Android/iOS variants, use the `expect`/`actual` pattern:

- Declare `expect val myModule: Module` in `commonMain`
- Implement `actual val myModule` in both `androidMain` and `iosMain`
- Include the `expect` val in the parent module via `includes(myModule)`

Existing examples: `databaseModule` in `CoreDataModule`, `secureStorageModule` in `CoreAuthModule`.

## Interface Registration

Always register by interface, not by implementation:

```kotlin
// Correct
single<MyRepository> { MyRepositoryImpl(get(), get()) }
singleOf(::MyRepositoryImpl).bind<MyRepository>()

// Wrong — leaks implementation details into the DI graph
single { MyRepositoryImpl(get(), get()) }
```