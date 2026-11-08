package com.ivangarzab.kluvs.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ivangarzab.kluvs.designsystem.components.bookcover.BookCoverPlaceholder
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.presentation.util.formatBookYear

/**
 * A single book tile: cover (with a read-ribbon badge for Kluvs-session books), title,
 * author, and year. Purely a browsing tile — tapping navigates to the book detail screen,
 * where shelf/like functionality actually lives.
 */
@Composable
fun BookCard(
    modifier: Modifier = Modifier,
    book: Book,
    shelfSource: ShelfSource? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
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

            Column {
                Text(
                    text = book.title,
                    style = KluvsTheme.typography.title.small,
                    color = KluvsTheme.colors.content,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = KluvsTheme.typography.caption,
                    color = KluvsTheme.colors.contentMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                book.year?.let { year ->
                    Text(
                        text = formatBookYear(year),
                        style = KluvsTheme.typography.caption,
                        color = KluvsTheme.colors.contentMuted
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_BookCard() = KluvsTheme {
    Box(
        modifier = Modifier
            .background(color = KluvsTheme.colors.background)
    ) {
        BookCard(
            modifier = Modifier.width(120.dp),
            book = Book(
                id = "42",
                title = "The Hobbit",
                author = "J.R.R. Tolkien",
                year = 1937,
                isbn = "978-0-395-07122-1"
            ),
            shelfSource = ShelfSource.SESSION
        )
    }
}
