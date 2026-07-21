package com.ivangarzab.kluvs.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.books.presentation.BookDetailsState
import com.ivangarzab.kluvs.books.presentation.BookDetailsViewModel
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.BookVolumeInfo
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.theme.ebGaramond
import org.koin.compose.viewmodel.koinViewModel

/**
 * Book detail screen: cover header, volume info, "About the Author", "More by this author",
 * and shelf/like controls. Mirrors web's `BooksPage.tsx` detail panel section order.
 */
@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    book: Book,
    initialShelfStatus: ShelfStatus?,
    initialShelfSource: ShelfSource?,
    onNavigateBack: () -> Unit,
    onNavigateToBook: (Book) -> Unit,
    viewModel: BookDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(book.id) {
        viewModel.load(book, initialShelfStatus, initialShelfSource)
    }

    LaunchedEffect(state.operationError) {
        state.operationError?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onConsumeOperationError()
        }
    }

    Box(modifier = modifier) {
        BookDetailScreenContent(
            modifier = Modifier.fillMaxSize(),
            state = state,
            isRegistered = viewModel.isRegistered,
            onNavigateBack = onNavigateBack,
            onNavigateToBook = onNavigateToBook,
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
fun BookDetailScreenContent(
    modifier: Modifier = Modifier,
    state: BookDetailsState,
    isRegistered: Boolean,
    onNavigateBack: () -> Unit = {},
    onNavigateToBook: (Book) -> Unit = {},
    onAssignShelf: (ShelfStatus) -> Unit = {},
    onRemoveFromShelf: () -> Unit = {},
    onToggleLike: () -> Unit = {}
) {
    val book = state.book ?: return

    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (state.isMutationInProgress) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Cover header
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SubcomposeAsyncImage(
                    model = book.imageUrl,
                    contentDescription = book.title,
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop,
                    loading = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) },
                    error = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) }
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = book.title,
                        fontFamily = ebGaramond,
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    state.enrichment?.volumeInfo?.subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    book.year?.let { year ->
                        Text(
                            text = year.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            BookDetailActions(
                isRegistered = isRegistered,
                shelfStatus = state.shelfStatus,
                isLiked = state.isLiked,
                isMutationInProgress = state.isMutationInProgress,
                onShelfChange = { shelf ->
                    if (shelf == null) onRemoveFromShelf() else onAssignShelf(shelf)
                },
                onToggleLike = onToggleLike
            )

            state.enrichment?.volumeInfo?.let { volumeInfo ->
                VolumeInfoSection(volumeInfo = volumeInfo)
            }

            AuthorSection(
                modifier = Modifier.fillMaxWidth(),
                isLoading = state.isLoadingEnrichment,
                author = state.enrichment?.author
            )

            val authorBooks = state.enrichment?.authorBooks.orEmpty()
            if (authorBooks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "More by this author",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(authorBooks, key = { it.id }) { authorBook ->
                            BookCard(
                                book = authorBook,
                                onClick = { onNavigateToBook(authorBook) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VolumeInfoSection(modifier: Modifier = Modifier, volumeInfo: BookVolumeInfo) {
    if (volumeInfo.description == null && volumeInfo.publisher == null && volumeInfo.categories.isEmpty()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        volumeInfo.description?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (volumeInfo.publisher != null || volumeInfo.categories.isNotEmpty()) {
            Text(
                text = listOfNotNull(
                    volumeInfo.publisher,
                    volumeInfo.categories.takeIf { it.isNotEmpty() }?.joinToString(", ")
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_BookDetailScreen() = KluvsTheme {
    BookDetailScreenContent(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        state = BookDetailsState(
            book = Book(id = "1", title = "The Hobbit", author = "J.R.R. Tolkien", year = 1937, isbn = "978-0-395-07122-1"),
            isLoadingEnrichment = false,
            shelfStatus = ShelfStatus.CURRENTLY_READING
        ),
        isRegistered = true
    )
}
