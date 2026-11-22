# :core:database

Room (KMP) database that backs the cache-first repositories in `:core:data`.

## Purpose

Local persistence for Android and iOS: cached API data survives restarts and is served offline.
Cache freshness rules (`CacheTTL`, `CachePolicy`) live in `:core:data`, not here. Feature modules
never use this module directly (a `commonTest` dependency for in-memory DBs is fine).

## Key Components

- **`KluvsDatabase`**: interface exposing one DAO per table plus `withTransaction { }`;
  `KluvsDatabaseImpl` is the Room `@Database` (currently schema version 5, schemas exported to
  `schemas/`).
- **Entities** (`entities/`): servers, clubs, members, `ClubMemberCrossRef`, sessions,
  `SessionMemberEntity`, books, discussions, shelf, likes, progress, discussion notes,
  discussion attendance. Cached rows carry a `lastFetchedAt` timestamp for TTL checks.
- **DAOs** (`dao/`): e.g. `ClubDao`, `MemberDao`, `SessionDao`, `ShelfDao`, each with a
  `deleteAll()` used by `SignOutUseCase`.
- **Migrations** (`migrations/`): `Migration1To2` … `Migration4To5`, registered in
  `getKluvsDatabase()`. Use the `migrate-room-db` skill for schema changes; keep them
  backward-compatible.
- **`getKluvsDatabase(builder)`** + platform `getDatabaseBuilder()` (Android takes a `Context`).

## Dependencies

- No `project(...)` dependencies.
- Room runtime + KSP compiler, bundled SQLite driver, `kotlinx-datetime`, coroutines

## Usage

Wired by `:core:data`'s `databaseModule`:

```kotlin
single<KluvsDatabase> { getKluvsDatabase(getDatabaseBuilder(get())) }  // Android
```
