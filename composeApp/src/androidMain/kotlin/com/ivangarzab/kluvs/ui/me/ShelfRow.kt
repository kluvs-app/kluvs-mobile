package com.ivangarzab.kluvs.ui.me

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.ivangarzab.kluvs.member.presentation.ShelfItem
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.progress.OwnProgressInfo
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.components.BookCoverPlaceholder
import com.ivangarzab.kluvs.designsystem.components.OwnProgressRow

/**
 * A single "On Your Shelf" row: the active-session book for one of the
 * member's clubs, with cover, club name, next discussion date, and the
 * shared [OwnProgressRow] for viewing/editing the member's own progress.
 *
 * Mirrors web's ProfilePage `ShelfRow`.
 */
@Composable
fun ShelfRow(
    item: ShelfItem,
    modifier: Modifier = Modifier,
    onUpdateProgress: (sessionId: String) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SubcomposeAsyncImage(
            model = item.bookCoverUrl,
            contentDescription = item.bookTitle,
            modifier = Modifier
                .width(52.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop,
            loading = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) },
            error = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) }
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.bookTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.bookAuthor,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = item.clubName.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(12.dp))

            OwnProgressRow(
                percent = item.ownProgress?.percent,
                statusLabel = item.ownProgress?.label,
                onUpdateProgress = { onUpdateProgress(item.sessionId) },
                leftLabel = item.nextDiscussionDate?.let { "Next · $it" } ?: "Your progress"
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_ShelfRow() = KluvsTheme {
    ShelfRow(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        item = ShelfItem(
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
        onUpdateProgress = {}
    )
}
