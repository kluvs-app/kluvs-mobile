package com.ivangarzab.kluvs.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
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
 * Book detail screen: cover header, category chips, shelf/like actions, "About",
 * "Details", "About the Author", and "More by this author". Mirrors web's
 * `BooksPage.tsx` detail panel section order (see [kluvs-frontend] for reference).
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

@OptIn(ExperimentalLayoutApi::class)
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
    val volumeInfo = state.enrichment?.volumeInfo

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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).padding(bottom = 32.dp),
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
                    volumeInfo?.subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            fontFamily = ebGaramond,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = metaLine(book, volumeInfo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (volumeInfo != null && volumeInfo.categories.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    volumeInfo.categories.take(5).forEach { category ->
                        CategoryChip(category)
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

            HorizontalDivider()

            // About
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionEyebrow(stringResource(R.string.book_about))
                val description = volumeInfo?.description
                Text(
                    text = description ?: stringResource(R.string.book_no_description),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = if (description == null) FontStyle.Italic else FontStyle.Normal,
                    color = if (description == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            val detailRows = buildDetailRows(book, volumeInfo)
            if (detailRows.isNotEmpty()) {
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionEyebrow(stringResource(R.string.book_details))
                    detailRows.forEachIndexed { index, (labelRes, value) ->
                        if (index > 0) HorizontalDivider()
                        DetailRow(label = stringResource(labelRes), value = value)
                    }
                }
            }

            if (state.isLoadingEnrichment || state.enrichment?.author != null) {
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionEyebrow(stringResource(R.string.book_about_the_author))
                    AuthorSection(
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = state.isLoadingEnrichment,
                        author = state.enrichment?.author
                    )
                }
            }

            val authorBooks = state.enrichment?.authorBooks.orEmpty()
            if (authorBooks.isNotEmpty()) {
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionEyebrow(stringResource(R.string.book_more_by_x, primaryAuthor(book.author)))
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
private fun SectionEyebrow(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CategoryChip(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(90.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun metaLine(book: Book, volumeInfo: BookVolumeInfo?): String {
    return listOfNotNull(
        book.author,
        book.year?.toString(),
        book.pageCount?.let { "$it pages" },
        volumeInfo?.publisher
    ).joinToString(" · ")
}

private fun buildDetailRows(book: Book, volumeInfo: BookVolumeInfo?): List<Pair<Int, String>> {
    val isbn = volumeInfo?.isbn13 ?: volumeInfo?.isbn10 ?: book.isbn
    return listOfNotNull(
        book.year?.let { R.string.book_field_published to it.toString() },
        book.pageCount?.let { R.string.book_field_pages to it.toString() },
        volumeInfo?.publisher?.let { R.string.book_field_publisher to it },
        isbn?.let { R.string.book_field_isbn to it },
        volumeInfo?.language?.let { R.string.book_field_language to displayLanguage(it) },
        book.edition?.let { R.string.book_field_edition to it }
    )
}

private fun primaryAuthor(author: String): String {
    return author
        .split(Regex("\\s*(?:,|&| and )\\s*", RegexOption.IGNORE_CASE))
        .firstOrNull()
        ?.trim()
        ?: author
}

private val languageDisplayNames = mapOf(
    "en" to "English", "es" to "Spanish", "fr" to "French", "de" to "German",
    "it" to "Italian", "pt" to "Portuguese", "nl" to "Dutch", "ja" to "Japanese",
    "zh" to "Chinese", "ko" to "Korean", "ru" to "Russian", "ar" to "Arabic",
    "hi" to "Hindi", "pl" to "Polish", "sv" to "Swedish", "tr" to "Turkish"
)

private fun displayLanguage(code: String): String {
    return languageDisplayNames[code.lowercase()] ?: code
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
