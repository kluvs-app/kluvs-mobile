package com.ivangarzab.kluvs.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.theme.KluvsTheme

/**
 * A single book tile: cover, title, author/year, shelf-status picker, and like toggle.
 *
 * Shelf/like controls live directly on the card since there is no book detail screen yet.
 */
@Composable
fun BookCard(
    modifier: Modifier = Modifier,
    book: Book,
    shelfStatus: ShelfStatus?,
    isLiked: Boolean,
    onShelfChange: (ShelfStatus?) -> Unit,
    onToggleLike: () -> Unit,
    onClick: () -> Unit = {}
) {
    var showShelfMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AsyncImage(
            model = book.imageUrl,
            contentDescription = book.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp)),
            placeholder = painterResource(R.drawable.img_fallback),
            fallback = painterResource(R.drawable.img_fallback),
            contentScale = ContentScale.Crop
        )

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

        Box {
            Text(
                text = shelfLabel(shelfStatus),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { showShelfMenu = true }
            )

            DropdownMenu(
                expanded = showShelfMenu,
                onDismissRequest = { showShelfMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.shelf_none)) },
                    onClick = {
                        showShelfMenu = false
                        onShelfChange(null)
                    }
                )
                ShelfStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(shelfLabel(status)) },
                        onClick = {
                            showShelfMenu = false
                            onShelfChange(status)
                        }
                    )
                }
            }
        }

        IconButton(
            onClick = onToggleLike,
            modifier = Modifier.padding(0.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(
                    if (isLiked) R.string.unlike_book else R.string.like_book
                ),
                tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun shelfLabel(status: ShelfStatus?): String = when (status) {
    null -> stringResource(R.string.shelf_none)
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
            .padding(16.dp)
    ) {
        BookCard(
            book = Book(
                id = "42",
                title = "The Hobbit",
                author = "J.R.R. Tolkien",
                isbn = "978-0-395-07122-1"
            ),
            shelfStatus = ShelfStatus.CURRENTLY_READING,
            isLiked = true,
            onShelfChange = {},
            onToggleLike = {}
        )
    }
}
