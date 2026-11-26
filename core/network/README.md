# :core:network

Supabase client setup, build-time config, and network reachability.

## Purpose

Provides the single `SupabaseClient` (Functions, Auth, Storage) used by `:core:auth` and
`:core:data`, the generated `BuildKonfig` with Supabase credentials, and the offline signal the
data layer exposes to the UI.

## Key Components

- **`coreNetworkModule`**: Koin module creating `SupabaseClient`, `OfflineStatus` and `KluvsClient`.
- **`BuildKonfig`** (generated): `SUPABASE_URL` / `SUPABASE_KEY` for the selected flavor
  (`integration` by default, or `production` via `-Pbuildkonfig.flavor=production`), plus
  `TEST_SUPABASE_URL` / `TEST_SUPABASE_KEY` for integration tests. Values come from Gradle
  properties or env vars; never commit them.
- **`OfflineStatus`**: `isOffline: StateFlow<Boolean>`, fed by `OfflineStatusPlugin` on the
  client's HTTP pipeline and re-checked in the background by `ConnectivityProbe` while offline.
  Read only by the data layer (`ConnectivityRepository`).
- **`KluvsClient`**: the installed app's platform and release version, read at runtime
  (Android `PackageInfo.versionName`, iOS `CFBundleShortVersionString`) and sent on every request
  as `X-Kluvs-Client: android/<version>` or `ios/<version>`.
- **`JsonHelper`**: the shared `Json` config for Supabase function responses.
- **`DateParsingUtils`**: `parseDateOnlyString()` / `parseDateTimeString()` for API date strings.

## Dependencies

- `:core:model` (api)
- `supabase-kt` (functions, auth, storage), Ktor client, Koin, barK

## Usage

Feature modules never use this module directly. Repositories in `:core:data` get the client
through Koin:

```kotlin
internal class ClubServiceImpl(private val supabase: SupabaseClient) : ClubService
```
