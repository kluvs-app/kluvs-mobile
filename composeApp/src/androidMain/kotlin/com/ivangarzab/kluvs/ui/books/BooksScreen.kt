package com.ivangarzab.kluvs.ui.books

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.books.presentation.BooksState
import com.ivangarzab.kluvs.books.presentation.BooksViewModel
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.presentation.state.ScreenState
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.theme.ebGaramond
import com.ivangarzab.kluvs.theme.foregroundLightSecondary
import com.ivangarzab.kluvs.theme.foregroundLightTertiary
import com.ivangarzab.kluvs.theme.foregroundWarmTertiary
import com.ivangarzab.kluvs.theme.ibmPlexSans
import com.ivangarzab.kluvs.ui.components.ErrorScreen
import com.ivangarzab.kluvs.ui.components.LoadingScreen
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

private val SHELF_SECTIONS = listOf(
    ShelfStatus.CURRENTLY_READING,
    ShelfStatus.READ,
    ShelfStatus.WANT_TO_READ,
    ShelfStatus.NOT_FINISHED
)

private enum class BooksView { Shelf, Search }

private const val SEARCH_DEBOUNCE_MS = 400L

@Composable
fun BooksScreen(
    modifier: Modifier = Modifier,
    viewModel: BooksViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadShelf()
    }

    LaunchedEffect(state.operationError) {
        state.operationError?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onConsumeOperationError()
        }
    }

    Box(modifier = modifier) {
        BooksScreenContent(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRetryShelf = { viewModel.loadShelf() },
            onQueryChange = viewModel::onQueryChange,
            onSearch = viewModel::search,
            onAssignShelf = viewModel::onAssignShelf,
            onRemoveFromShelf = viewModel::onRemoveFromShelf
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BooksScreenContent(
    modifier: Modifier = Modifier,
    state: BooksState,
    onRetryShelf: () -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    onAssignShelf: (String, ShelfStatus) -> Unit = { _, _ -> },
    onRemoveFromShelf: (String) -> Unit = {}
) {
    var view by remember { mutableStateOf(BooksView.Shelf) }

    // Debounce search, matching web's 400ms delay
    LaunchedEffect(state.query) {
        if (view == BooksView.Search) {
            delay(SEARCH_DEBOUNCE_MS)
            onSearch(state.query)
        }
    }

    Column(modifier = modifier) {
        BooksTopBar(
            isSearchActive = view == BooksView.Search,
            isSearching = state.isSearching,
            query = state.query,
            onQueryChange = onQueryChange,
            onSearchClick = { view = BooksView.Search },
            onBackClick = {
                view = BooksView.Shelf
                onQueryChange("")
            }
        )

        if (state.isMutationInProgress) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        when (view) {
            BooksView.Shelf -> {
                ShelfContent(
                    modifier = Modifier.weight(1f),
                    state = state,
                    onRetry = onRetryShelf,
                    onAssignShelf = onAssignShelf,
                    onRemoveFromShelf = onRemoveFromShelf
                )
            }
            BooksView.Search -> {
                SearchContent(
                    modifier = Modifier.weight(1f),
                    state = state,
                    onRetry = { onSearch(state.query) },
                    onAssignShelf = onAssignShelf,
                    onRemoveFromShelf = onRemoveFromShelf
                )
            }
        }
    }
}

@Composable
private fun ShelfContent(
    modifier: Modifier = Modifier,
    state: BooksState,
    onRetry: () -> Unit,
    onAssignShelf: (String, ShelfStatus) -> Unit,
    onRemoveFromShelf: (String) -> Unit
) {
    val shelfError = state.shelfError
    val screenState = when {
        state.shelfEntries.isNotEmpty() -> ScreenState.Content
        state.isLoadingShelf -> ScreenState.Loading
        shelfError != null -> ScreenState.Error(shelfError)
        else -> ScreenState.Empty
    }

    AnimatedContent(
        targetState = screenState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ShelfTransition",
        modifier = modifier
    ) { targetState ->
        when (targetState) {
            is ScreenState.Loading -> LoadingScreen()
            is ScreenState.Error -> ErrorScreen(message = targetState.message, onRetry = onRetry)
            is ScreenState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_books_shelved),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is ScreenState.Content -> {
                val entriesBySection = state.shelfEntries.groupBy { it.shelf }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SHELF_SECTIONS.forEach { section ->
                        val entries = entriesBySection[section].orEmpty()
                        if (entries.isNotEmpty()) {
                            item(key = section.name) {
                                ShelfSection(
                                    section = section,
                                    entries = entries,
                                    onAssignShelf = onAssignShelf,
                                    onRemoveFromShelf = onRemoveFromShelf
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShelfSection(
    modifier: Modifier = Modifier,
    section: ShelfStatus,
    entries: List<ShelfEntry>,
    onAssignShelf: (String, ShelfStatus) -> Unit,
    onRemoveFromShelf: (String) -> Unit
) {
    Column(modifier = modifier) {
        val isDark = isSystemInDarkTheme()
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            // Eyebrow — design-system component.eyebrow: IBM Plex Sans 11px/500, uppercase, 0.14em tracking
            Text(
                text = sectionLabel(section).uppercase(),
                fontFamily = ibmPlexSans,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.14.em,
                color = if (isDark) Color(0xFFB0B0B0) else foregroundLightSecondary
            )
            Text(
                text = entries.size.toString(),
                fontFamily = ibmPlexSans,
                fontSize = 10.sp,
                color = if (isDark) foregroundWarmTertiary else foregroundLightTertiary
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries, key = { it.book.id }) { entry ->
                BookCard(
                    book = entry.book,
                    shelfStatus = entry.shelf,
                    shelfSource = entry.source,
                    onShelfChange = { newShelf ->
                        if (newShelf == null) {
                            onRemoveFromShelf(entry.book.id)
                        } else {
                            onAssignShelf(entry.book.id, newShelf)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchContent(
    modifier: Modifier = Modifier,
    state: BooksState,
    onRetry: () -> Unit,
    onAssignShelf: (String, ShelfStatus) -> Unit,
    onRemoveFromShelf: (String) -> Unit
) {
    val searchError = state.searchError

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.query.isBlank() -> {
                SearchEmptyState(
                    heading = stringResource(R.string.start_typing),
                    body = stringResource(R.string.start_typing_hint)
                )
            }
            state.isSearching && state.searchResults.isEmpty() -> LoadingScreen()
            searchError != null -> ErrorScreen(message = searchError, onRetry = onRetry)
            state.searchResults.isEmpty() -> {
                SearchEmptyState(
                    heading = stringResource(R.string.no_matches),
                    body = stringResource(R.string.no_books_found_for_x, state.query)
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    gridItems(state.searchResults, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            shelfStatus = null,
                            onShelfChange = { newShelf ->
                                if (newShelf == null) {
                                    onRemoveFromShelf(book.id)
                                } else {
                                    onAssignShelf(book.id, newShelf)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyState(heading: String, body: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StackedCoverPlaceholder()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = heading,
                    fontFamily = ebGaramond,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun sectionLabel(status: ShelfStatus): String = when (status) {
    ShelfStatus.CURRENTLY_READING -> stringResource(R.string.shelf_currently_reading)
    ShelfStatus.READ -> stringResource(R.string.shelf_read)
    ShelfStatus.WANT_TO_READ -> stringResource(R.string.shelf_want_to_read)
    ShelfStatus.NOT_FINISHED -> stringResource(R.string.shelf_not_finished)
}

@PreviewLightDark
@Composable
fun Preview_BooksScreen_Empty() = KluvsTheme {
    BooksScreenContent(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        state = BooksState(isLoadingShelf = false)
    )
}

@PreviewLightDark
@Composable
fun Preview_BooksScreen_WithShelf() = KluvsTheme {
    BooksScreenContent(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        state = BooksState(
            isLoadingShelf = false,
            shelfEntries = fakeShelfEntries()
        )
    )
}

private fun fakeShelfEntries(): List<ShelfEntry> = listOf(
    ShelfEntry(
        shelf = ShelfStatus.CURRENTLY_READING,
        source = ShelfSource.SESSION,
        book = Book(id = "1", title = "The Hobbit", author = "J.R.R. Tolkien", year = 1937, isbn = "978-0-395-07122-1")
    ),
    ShelfEntry(
        shelf = ShelfStatus.CURRENTLY_READING,
        book = Book(id = "2", title = "Dune", author = "Frank Herbert", year = 1965, isbn = "978-0-441-01359-3")
    ),
    ShelfEntry(
        shelf = ShelfStatus.READ,
        source = ShelfSource.SESSION,
        book = Book(id = "3", title = "Project Hail Mary", author = "Andy Weir", year = 2021, isbn = "978-0-593-13520-4")
    ),
    ShelfEntry(
        shelf = ShelfStatus.WANT_TO_READ,
        book = Book(id = "4", title = "The Fifth Season", author = "N.K. Jemisin", year = 2015, isbn = "978-0-316-22929-6")
    ),
    ShelfEntry(
        shelf = ShelfStatus.NOT_FINISHED,
        book = Book(id = "5", title = "Infinite Jest", author = "David Foster Wallace", year = 1996, isbn = "978-0-316-92004-9")
    )
)
