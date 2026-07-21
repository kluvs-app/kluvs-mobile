package com.ivangarzab.kluvs.ui.me

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.member.presentation.ShelfItem
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.progress.OwnProgressInfo
import com.ivangarzab.kluvs.theme.KluvsTheme

/**
 * "On Your Shelf" section on the Me screen: eyebrow header + book count caption,
 * then one [ShelfRow] per active-session book. Mirrors web's ProfilePage shelf list.
 */
@Composable
fun ShelfSection(
    modifier: Modifier = Modifier,
    shelf: List<ShelfItem>,
    onUpdateProgress: (sessionId: String) -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(R.string.on_your_shelf).uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            if (shelf.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.books_in_progress_x, shelf.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        Spacer(Modifier.padding(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            shelf.forEach { item ->
                ShelfRow(item = item, onUpdateProgress = onUpdateProgress)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_ShelfSection() = KluvsTheme {
    ShelfSection(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        shelf = listOf(
            ShelfItem(
                sessionId = "s0",
                bookId = "b0",
                bookTitle = "How AI Thinks",
                bookAuthor = "Nigel Toon",
                bookCoverUrl = null,
                bookPageCount = 328,
                clubId = "c0",
                clubName = "Showcase Kluv",
                nextDiscussionDate = "December 31, 2026",
                ownProgress = OwnProgressInfo(
                    progressId = "p0",
                    type = ProgressType.PAGE,
                    currentPage = 42,
                    percentComplete = null,
                    isCompleted = false,
                    percent = 13,
                    label = "42 of 328 pages"
                )
            ),
            ShelfItem(
                sessionId = "s1",
                bookId = "b1",
                bookTitle = "1984",
                bookAuthor = "George Orwell",
                bookCoverUrl = null,
                bookPageCount = null,
                clubId = "c1",
                clubName = "Classics Club",
                nextDiscussionDate = null,
                ownProgress = null
            )
        ),
        onUpdateProgress = {}
    )
}
