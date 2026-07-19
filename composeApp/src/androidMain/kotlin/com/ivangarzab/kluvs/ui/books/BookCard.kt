package com.ivangarzab.kluvs.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.theme.KluvsTheme

/**
 * A single book tile: cover (with a read-ribbon badge for Kluvs-session books), title,
 * author, year, and a shelf-status menu.
 *
 * The shelf menu lives directly on the card since there is no book detail screen yet.
 */
@Composable
fun BookCard(
    modifier: Modifier = Modifier,
    book: Book,
    shelfStatus: ShelfStatus?,
    shelfSource: ShelfSource? = null,
    onShelfChange: (ShelfStatus?) -> Unit,
    onClick: () -> Unit = {}
) {
    var showShelfMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box {
                SubcomposeAsyncImage(
                    model = book.imageUrl,
                    contentDescription = book.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(4.dp)), // radius.sm — design-system/docs/book-cover.md
                    contentScale = ContentScale.Crop,
                    loading = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) },
                    error = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) }
                )

                if (shelfSource == ShelfSource.SESSION) {
                    ReadRibbon(
                        modifier = Modifier.align(Alignment.TopEnd),
                        size = ReadRibbonSize.LG,
                        contentDescription = stringResource(R.string.kluvs_read_ribbon)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (book.year != null) {
                        Text(
                            text = book.year.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Search results straight from Google Books have no local DB id yet (book detail /
                // registration flow is a separate ticket), so shelving isn't available until then.
                val isRegistered = book.id.toIntOrNull() != null
                if (isRegistered) {
                    Box {
                        IconButton(
                            onClick = { showShelfMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.shelf_status),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showShelfMenu,
                            onDismissRequest = { showShelfMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.shelf_none)) },
                                trailingIcon = { if (shelfStatus == null) SelectedCheck() },
                                onClick = {
                                    showShelfMenu = false
                                    onShelfChange(null)
                                }
                            )
                            ShelfStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(shelfLabel(status)) },
                                    trailingIcon = { if (shelfStatus == status) SelectedCheck() },
                                    onClick = {
                                        showShelfMenu = false
                                        onShelfChange(status)
                                    }
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
private fun SelectedCheck() {
    Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun shelfLabel(status: ShelfStatus): String = when (status) {
    ShelfStatus.CURRENTLY_READING -> stringResource(R.string.shelf_currently_reading)
    ShelfStatus.READ -> stringResource(R.string.shelf_read)
    ShelfStatus.WANT_TO_READ -> stringResource(R.string.shelf_want_to_read)
    ShelfStatus.NOT_FINISHED -> stringResource(R.string.shelf_not_finished)
}

@PreviewLightDark
@Composable
fun Preview_BookCard() = KluvsTheme {
    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        BookCard(
            book = Book(
                id = "42",
                title = "The Hobbit",
                author = "J.R.R. Tolkien",
                year = 1937,
                isbn = "978-0-395-07122-1"
            ),
            shelfStatus = ShelfStatus.CURRENTLY_READING,
            shelfSource = ShelfSource.SESSION,
            onShelfChange = {}
        )
    }
}
