# :feature:books

Books tab: the member's personal shelf, book search, likes, and book details.

## Purpose

Shared presentation and domain logic for browsing and searching books, assigning them to a shelf
status, liking them, and showing an enriched book-details view.

## Key Components

- **`BooksViewModel`**: `state: StateFlow<BooksState>`; shelf loading, paged search
  (`search()`, `loadMoreSearchResults()`), `onAssignShelf()`, `onRemoveFromShelf()`,
  `onToggleLike()`.
- **`BookDetailsViewModel`**: `state: StateFlow<BookDetailsState>`; `load(book, ...)` fetches
  enrichment and like status, plus the same shelf/like actions for a single book.
- **UseCases** (`domain/`): `GetShelfUseCase`, `AssignShelfUseCase`, `RemoveFromShelfUseCase`,
  `SearchBooksUseCase`, `RegisterBookUseCase`, `ToggleLikeUseCase`, `GetLikeStatusUseCase`,
  `GetBookEnrichmentUseCase`.
- **`BooksViewModelHelper`**, **`BookDetailsViewModelHelper`** (iosMain): iOS bridge, consumed by
  `iosApp/iosApp/Helpers/BooksViewModelWrapper.swift` and `BookDetailsViewModelWrapper.swift`.
- `booksFeatureModule`: Koin wiring.

## Dependencies

- `:core:model`
- `:core:data`
- `:core:presentation`

## Usage

```kotlin
@Composable
fun BooksScreen(viewModel: BooksViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadShelf() }
    // render state.shelfEntries; search via viewModel.search(state.query)
}
```
