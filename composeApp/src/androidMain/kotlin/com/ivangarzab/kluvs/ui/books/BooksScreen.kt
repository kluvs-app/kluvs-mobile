package com.ivangarzab.kluvs.ui.books

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.books.presentation.BooksState
import com.ivangarzab.kluvs.books.presentation.BooksViewModel
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.presentation.state.ScreenState
import com.ivangarzab.kluvs.theme.KluvsTheme
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
            onRemoveFromShelf = viewModel::onRemoveFromShelf,
            onToggleLike = viewModel::onToggleLike
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
    onRemoveFromShelf: (String) -> Unit = {},
    onToggleLike: (String) -> Unit = {}
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
        if (state.isMutationInProgress) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        when (view) {
            BooksView.Shelf -> {
                ShelfHeader(onSearchClick = { view = BooksView.Search })
                ShelfContent(
                    modifier = Modifier.weight(1f),
                    state = state,
                    onRetry = onRetryShelf,
                    onAssignShelf = onAssignShelf,
                    onRemoveFromShelf = onRemoveFromShelf,
                    onToggleLike = onToggleLike
                )
            }
            BooksView.Search -> {
                SearchBar(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    onBack = {
                        view = BooksView.Shelf
                        onQueryChange("")
                    }
                )
                SearchContent(
                    modifier = Modifier.weight(1f),
                    state = state,
                    onRetry = { onSearch(state.query) },
                    onAssignShelf = onAssignShelf,
                    onRemoveFromShelf = onRemoveFromShelf,
                    onToggleLike = onToggleLike
                )
            }
        }
    }
}

@Composable
private fun ShelfHeader(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.my_shelf),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_books)
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back)
            )
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.search_books_hint)) }
        )
    }
}

@Composable
private fun ShelfContent(
    modifier: Modifier = Modifier,
    state: BooksState,
    onRetry: () -> Unit,
    onAssignShelf: (String, ShelfStatus) -> Unit,
    onRemoveFromShelf: (String) -> Unit,
    onToggleLike: (String) -> Unit
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
                                    likedBookIds = state.likedBookIds,
                                    onAssignShelf = onAssignShelf,
                                    onRemoveFromShelf = onRemoveFromShelf,
                                    onToggleLike = onToggleLike
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
    likedBookIds: Set<String>,
    onAssignShelf: (String, ShelfStatus) -> Unit,
    onRemoveFromShelf: (String) -> Unit,
    onToggleLike: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = sectionLabel(section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries, key = { it.book.id }) { entry ->
                BookCard(
                    book = entry.book,
                    shelfStatus = entry.shelf,
                    isLiked = likedBookIds.contains(entry.book.id),
                    onShelfChange = { newShelf ->
                        if (newShelf == null) {
                            onRemoveFromShelf(entry.book.id)
                        } else {
                            onAssignShelf(entry.book.id, newShelf)
                        }
                    },
                    onToggleLike = { onToggleLike(entry.book.id) }
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
    onRemoveFromShelf: (String) -> Unit,
    onToggleLike: (String) -> Unit
) {
    val searchError = state.searchError
    val screenState = when {
        state.searchResults.isNotEmpty() -> ScreenState.Content
        state.isSearching -> ScreenState.Loading
        searchError != null -> ScreenState.Error(searchError)
        state.query.isBlank() -> null
        else -> ScreenState.Empty
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (screenState) {
            null -> Unit
            is ScreenState.Loading -> LoadingScreen()
            is ScreenState.Error -> ErrorScreen(message = screenState.message, onRetry = onRetry)
            is ScreenState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_search_results),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is ScreenState.Content -> {
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
                            isLiked = state.likedBookIds.contains(book.id),
                            onShelfChange = { newShelf ->
                                if (newShelf == null) {
                                    onRemoveFromShelf(book.id)
                                } else {
                                    onAssignShelf(book.id, newShelf)
                                }
                            },
                            onToggleLike = { onToggleLike(book.id) }
                        )
                    }
                }
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
fun Preview_BooksScreen() = KluvsTheme {
    BooksScreenContent(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        state = BooksState(isLoadingShelf = false)
    )
}
