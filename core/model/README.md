# :core:model

Domain models shared across the whole app.

## Purpose

Plain Kotlin data classes and enums for the business objects in Kluvs. No framework, platform or
serialization code: wire DTOs live in `:core:api` and are mapped to these in `:core:data`. Dates
use `kotlinx-datetime`.

## Key Components

| Model | Description |
|-------|-------------|
| `User` | Authenticated user (`AuthProvider`) |
| `Member` | Member profile (name, handle, avatar, books read, clubs) |
| `Club`, `ClubMember`, `Role` | Club with members, active/past sessions, `JoinPolicy`, invite token |
| `Session`, `Discussion` | Reading session with book and scheduled discussions |
| `Book`, `BookSearchResult`, `BookEnrichment` | Book data and search/enrichment results |
| `ShelfEntry`, `ShelfStatus` | The member's personal book shelf |
| `ReadingProgress`, `ReadingLog` | Reading progress and history |
| `DiscussionNote`, `AttendanceRoster`, `AttendanceStatus` | Discussion notes and attendance |
| `ClubPreview` | Invite-link preview of a club |
| `Server` | Discord server |
| `AppError`, `UserFacingException` | Error vocabulary shown to the UI |

## Dependencies

- No `project(...)` dependencies (leaf module).
- `kotlinx-datetime`

## Usage

```kotlin
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Member
```
