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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.ivangarzab.kluvs.designsystem.components.KluvsSnackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.books.presentation.BooksState
import com.ivangarzab.kluvs.books.presentation.BooksViewModel
import com.ivangarzab.kluvs.designsystem.components.appbars.SearchTopAppBar
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.presentation.state.ScreenState
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.foregroundLightSecondary
import com.ivangarzab.kluvs.designsystem.theme.foregroundLightTertiary
import com.ivangarzab.kluvs.designsystem.theme.foregroundWarmTertiary
import com.ivangarzab.kluvs.designsystem.theme.ibmPlexSans
import com.ivangarzab.kluvs.designsystem.components.EmptyState
import com.ivangarzab.kluvs.designsystem.components.ErrorScreen
import com.ivangarzab.kluvs.designsystem.components.buttons.SecondaryButton
import com.ivangarzab.kluvs.designsystem.components.loading.LoadingSpinner
import com.ivangarzab.kluvs.designsystem.components.loading.PullToRefreshContainer
import com.ivangarzab.kluvs.ui.components.LoadingScreen
import com.ivangarzab.kluvs.ui.components.ScreenEffectHost
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
    val navController = rememberNavController()

    // Tapped books (from shelf, search, or a "more by this author" row) are threaded through
    // this closure rather than nav args, since they may not be back-referenceable from BooksState
    // (e.g. an author's other book fetched only via enrichment).
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadShelf()
    }

    ScreenEffectHost(viewModel.screenEffects, snackbarHostState)

    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = "list"
        ) {
            composable("list") {
                BooksScreenContent(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onRetryShelf = { viewModel.loadShelf() },
                    onRefreshShelf = { viewModel.loadShelf(forceRefresh = true) },
                    onQueryChange = viewModel::onQueryChange,
                    onSearch = viewModel::search,
                    onLoadMoreSearchResults = viewModel::loadMoreSearchResults,
                    onBookClick = { book ->
                        selectedBook = book
                        navController.navigate("detail/${book.id}")
                    }
                )
            }
            composable("detail/{bookId}") {
                selectedBook?.let { book ->
                    val shelfEntry = state.shelfEntries.find { it.book.id == book.id }
                    BookDetailScreen(
                        modifier = Modifier.fillMaxSize(),
                        book = book,
                        initialShelfStatus = shelfEntry?.shelf,
                        initialShelfSource = shelfEntry?.source,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToBook = { nextBook ->
                            selectedBook = nextBook
                            navController.navigate("detail/${nextBook.id}")
                        }
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            snackbar = { KluvsSnackbar(it) }
        )
    }
}

@Composable
fun BooksScreenContent(
    modifier: Modifier = Modifier,
    state: BooksState,
    onRetryShelf: () -> Unit = {},
    onRefreshShelf: () -> Unit = onRetryShelf,
    onQueryChange: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    onLoadMoreSearchResults: () -> Unit = {},
    onBookClick: (Book) -> Unit = {}
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
        SearchTopAppBar(
            header = stringResource(R.string.books),
            title = stringResource(R.string.your_shelf_title),
            isSearchActive = view == BooksView.Search,
            onSearchActiveChange = { active ->
                view = if (active) BooksView.Search else BooksView.Shelf
                if (!active) onQueryChange("")
            },
            searchQuery = state.query,
            onSearchQueryChange = onQueryChange,
            isSearchLoading = state.isSearching,
            searchPlaceholder = stringResource(R.string.search_books_hint),
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
                    onRefresh = onRefreshShelf,
                    onBookClick = onBookClick,
                    onSearchBooks = { view = BooksView.Search }
                )
            }
            BooksView.Search -> {
                SearchContent(
                    modifier = Modifier.weight(1f),
                    state = state,
                    onRetry = { onSearch(state.query) },
                    onLoadMore = onLoadMoreSearchResults,
                    onBookClick = onBookClick
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
    onRefresh: () -> Unit = onRetry,
    onBookClick: (Book) -> Unit,
    onSearchBooks: () -> Unit = {}
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
            is ScreenState.Loading -> BooksShelfSkeleton(modifier = Modifier.fillMaxSize())
            is ScreenState.Error -> ErrorScreen(message = targetState.message, onRetry = onRetry)
            is ScreenState.Empty -> {
                EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    heading = "Nothing shelved yet.",
                    body = "Search for a book and add it to Want to Read, Read, or Not Finished.",
                    action = { SecondaryButton(text = "Search Books", onClick = onSearchBooks) },
                )
            }
            is ScreenState.Content -> {
                PullToRefreshContainer(
                    isLoading = state.isLoadingShelf,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
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
                                        onBookClick = onBookClick
                                    )
                                }
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
    onBookClick: (Book) -> Unit
) {
    Column(modifier = modifier) {
        val isDark = isSystemInDarkTheme()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = sectionLabel(section).uppercase(),
                style = KluvsTheme.typography.eyebrow,
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
                    modifier = Modifier.width(120.dp),
                    book = entry.book,
                    shelfSource = entry.source,
                    onClick = { onBookClick(entry.book) }
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
    onLoadMore: () -> Unit = {},
    onBookClick: (Book) -> Unit
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
                val gridState = rememberLazyGridState()

                LaunchedEffect(gridState, state.searchResults.size) {
                    snapshotFlow {
                        val layoutInfo = gridState.layoutInfo
                        val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                        lastVisibleIndex >= layoutInfo.totalItemsCount - 4
                    }.collect { isNearEnd ->
                        if (isNearEnd) onLoadMore()
                    }
                }

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    gridItems(state.searchResults, key = { it.id }) { book ->
                        BookCard(
                            modifier = Modifier.fillMaxWidth(),
                            book = book,
                            onClick = { onBookClick(book) }
                        )
                    }
                    if (state.isLoadingMore) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingSpinner()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyState(heading: String, body: String) {
    // Neither the "haven't searched yet" nor the "no matches" variant has a real destination
    // to send the user to, so this carries no action.
    EmptyState(modifier = Modifier.fillMaxSize(), heading = heading, body = body)
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
        modifier = Modifier.background(color = KluvsTheme.colors.background),
        state = BooksState(isLoadingShelf = false)
    )
}

@PreviewLightDark
@Composable
fun Preview_BooksScreen_WithShelf() = KluvsTheme {
    BooksScreenContent(
        modifier = Modifier.background(color = KluvsTheme.colors.background),
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
