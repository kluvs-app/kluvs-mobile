# :core:data

Repositories over remote (Supabase Edge Functions) and local (Room) data sources.

## Purpose

Data access only: fetch, cache, and map wire DTOs to `:core:model` domain types. Every repository
method returns `Result<T>`. Business logic does not live here; it belongs in each feature's
UseCases (`feature/*/domain/`).

```
Repository → RemoteDataSource → Service → Edge Function   (DTOs from :core:api)
           ↘ LocalDataSource → Room DAO (:core:database)  (TTL cache via CachePolicy/CacheTTL)
```

## Key Components

- **Repositories** (`repositories/`) — e.g. `ClubRepository`, `MemberRepository`, `SessionRepository`,
  `BookRepository`, `ShelfRepository`, `JoinRepository`. Public interface plus `internal` impl.
- **Services** (`remote/api/`) — one per Edge Function, calling `supabase.functions.invoke(...)`.
- **Mappers** (`remote/mappers/`, `local/mappers/`) — DTO ↔ domain ↔ entity.
- **Cache** (`local/cache/`) — `CachePolicy`, `CacheTTL`, cache fallback when offline.
- **`Throwable.toAppError()`** (`error/`) — classifies raw failures into `AppError`.
- `coreDataModule` — Koin wiring.

## Dependencies

- `:core:model` (api), `:core:network`, `:core:database`, `:core:auth`, `:core:api`

## Usage

Repositories are consumed by UseCases, never directly by ViewModels:

```kotlin
// feature/books/.../domain/GetShelfUseCase.kt (simplified)
class GetShelfUseCase(private val shelfRepository: ShelfRepository) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<ShelfEntry>> =
        shelfRepository.getShelf(forceRefresh)
}
```

Repositories are cache-first: fresh, complete Room data is served unless `forceRefresh = true`;
on remote failure the cached value is returned (`recoverWithCache`) and `ConnectivityRepository`
reports offline state.

## Integration tests

`*IntegrationTest.kt` (in `commonTest/.../remote/api/`) hit a local Supabase from `kluvs-backend`
(cloned at `../kluvs-backend`). Requires the Supabase CLI.

1. In `kluvs-backend`: `npx supabase start`, then `npx supabase status` for the API URL
   (typically `http://127.0.0.1:54321`) and the anon key.
2. Set `TEST_SUPABASE_URL` and `TEST_SUPABASE_KEY` (the anon key from `supabase status`) in
   `~/.gradle/gradle.properties`.
3. Run: `./gradlew :core:data:testDebugUnitTest --tests "*IntegrationTest"`

After new backend migrations, the user runs `npx supabase db reset` in `kluvs-backend` (drops and
recreates the DB, applies all migrations, reseeds from `supabase/seed.sql`). Claude must never run it.

- 404/NotFound or schema errors: migrations probably not applied (needs a db reset).
- Connection errors: check `supabase status` and the URL.

Seed data lives in `kluvs-backend/supabase/seed.sql`; each test file's header documents what it
relies on (e.g. `ClubServiceIntegrationTest.kt`).
