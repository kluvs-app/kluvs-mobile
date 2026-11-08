package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.SubcomposeAsyncImage
import com.ivangarzab.kluvs.designsystem.components.bookcover.BookCoverPlaceholder
import com.ivangarzab.kluvs.designsystem.components.fields.InputField
import com.ivangarzab.kluvs.designsystem.components.icons.Icon
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.presentation.util.formatBookYear
import kotlinx.coroutines.delay

private const val BOOK_SEARCH_DEBOUNCE_MS = 400L

/**
 * Search-and-select field for choosing a book when creating a session — the combobox flagged
 * as "separate, unbuilt" in [com.ivangarzab.kluvs.designsystem.components.fields.SearchField]'s
 * doc comment. Debounced free-text search yields a dropdown of results rendered as a floating
 * [Popup] anchored above the field (drawn in its own window layer, so it neither pushes the
 * rest of the bottom sheet around nor gets clipped by its scroll container, and opens away from
 * the keyboard instead of under it). Selecting a result collapses the field into a locked
 * "selected book" row, mirroring web's `BookSearchInput`. Uses [InputField] rather than
 * [com.ivangarzab.kluvs.designsystem.components.fields.SearchField] so it matches the height of
 * neighboring form fields (e.g. the due-date [PickerField]) instead of SearchField's compact
 * top-app-bar sizing.
 */
@Composable
fun BookSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    results: List<Book>,
    selectedBook: Book?,
    isSearching: Boolean,
    isRegistering: Boolean,
    error: String?,
    onSelect: (Book) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(query, selectedBook) {
        if (selectedBook == null && query.isNotBlank()) {
            delay(BOOK_SEARCH_DEBOUNCE_MS)
            onSearch(query)
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (selectedBook != null) {
            SelectedBookRow(book = selectedBook, onClear = onClear)
        } else {
            val density = LocalDensity.current
            var fieldWidthPx by remember { mutableIntStateOf(0) }
            var dropdownHeightPx by remember { mutableIntStateOf(0) }
            val showDropdown = query.isNotBlank() && (results.isNotEmpty() || !isSearching)

            Box(
                modifier = Modifier.onGloballyPositioned {
                    fieldWidthPx = it.size.width
                },
            ) {
                InputField(
                    label = "Book",
                    value = query,
                    onValueChange = onQueryChange,
                    helperText = if (isSearching || isRegistering) "Searching…" else null,
                )

                // Floats above the field, in its own window layer — this is deliberately a
                // Popup, not an inline sibling composable, so opening it never reflows or gets
                // clipped by the enclosing bottom sheet's scroll container.
                if (showDropdown) {
                    Popup(
                        alignment = Alignment.TopStart,
                        offset = IntOffset(0, -(dropdownHeightPx + with(density) { 4.dp.roundToPx() })),
                        properties = PopupProperties(focusable = false),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(with(density) { fieldWidthPx.toDp() })
                                .onGloballyPositioned { dropdownHeightPx = it.size.height },
                        ) {
                            when {
                                results.isNotEmpty() -> {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 240.dp)
                                            .background(KluvsTheme.colors.card, RoundedCornerShape(8.dp)),
                                    ) {
                                        items(results, key = { it.id.ifEmpty { it.externalGoogleId ?: it.title } }) { book ->
                                            BookResultRow(book = book, onClick = { onSelect(book) })
                                        }
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "No books found for \"$query\"",
                                        style = KluvsTheme.typography.caption,
                                        color = KluvsTheme.colors.contentMuted,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(KluvsTheme.colors.card, RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (error != null) {
            Text(
                text = error,
                style = KluvsTheme.typography.caption,
                color = KluvsTheme.colors.danger,
            )
        }
    }
}

@Composable
private fun BookResultRow(book: Book, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookThumbnail(imageUrl = book.imageUrl, contentDescription = book.title)
        Column {
            Text(
                text = book.title,
                style = KluvsTheme.typography.body.medium,
                color = KluvsTheme.colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = book.author,
                style = KluvsTheme.typography.caption,
                color = KluvsTheme.colors.contentMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            book.year?.let { year ->
                Text(
                    text = formatBookYear(year),
                    style = KluvsTheme.typography.caption,
                    color = KluvsTheme.colors.contentMuted,
                )
            }
        }
    }
}

@Composable
private fun SelectedBookRow(book: Book, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KluvsTheme.colors.card, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookThumbnail(imageUrl = book.imageUrl, contentDescription = book.title)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = book.title,
                style = KluvsTheme.typography.body.medium,
                color = KluvsTheme.colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = book.author,
                style = KluvsTheme.typography.caption,
                color = KluvsTheme.colors.contentMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            type = IconType.Close,
            contentDescription = "Change book",
            tint = KluvsTheme.colors.contentMuted,
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onClear),
        )
    }
}

@Composable
private fun BookThumbnail(imageUrl: String?, contentDescription: String) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = Modifier
            .width(40.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(4.dp)),
        contentScale = ContentScale.Crop,
        loading = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) },
        error = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) },
    )
}

@PreviewLightDark
@Composable
private fun Preview_BookSearchField_Results() = KluvsTheme {
    Box(modifier = Modifier.background(KluvsTheme.colors.background).padding(16.dp)) {
        BookSearchField(
            query = "hobbit",
            onQueryChange = {},
            onSearch = {},
            results = listOf(
                Book(id = "1", title = "The Hobbit", author = "J.R.R. Tolkien", year = 1937, isbn = null),
                Book(id = "2", title = "The Hobbit: Illustrated Edition", author = "J.R.R. Tolkien", year = 1997, isbn = null),
            ),
            selectedBook = null,
            isSearching = false,
            isRegistering = false,
            error = null,
            onSelect = {},
            onClear = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview_BookSearchField_Selected() = KluvsTheme {
    Box(modifier = Modifier.background(KluvsTheme.colors.background).padding(16.dp)) {
        BookSearchField(
            query = "The Hobbit",
            onQueryChange = {},
            onSearch = {},
            results = emptyList(),
            selectedBook = Book(id = "1", title = "The Hobbit", author = "J.R.R. Tolkien", year = 1937, isbn = null),
            isSearching = false,
            isRegistering = false,
            error = null,
            onSelect = {},
            onClear = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview_BookSearchField_Empty() = KluvsTheme {
    Box(modifier = Modifier.background(KluvsTheme.colors.background).padding(16.dp)) {
        BookSearchField(
            query = "zzzznotabook",
            onQueryChange = {},
            onSearch = {},
            results = emptyList(),
            selectedBook = null,
            isSearching = false,
            isRegistering = false,
            error = null,
            onSelect = {},
            onClear = {},
        )
    }
}
