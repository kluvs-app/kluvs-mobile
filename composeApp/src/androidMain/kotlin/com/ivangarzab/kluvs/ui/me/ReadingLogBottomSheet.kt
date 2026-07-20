package com.ivangarzab.kluvs.ui.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.model.BookSummary
import com.ivangarzab.kluvs.model.ClubPreview
import com.ivangarzab.kluvs.model.ReadingLog
import com.ivangarzab.kluvs.model.ReadingLogEntry
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.books.BookCoverPlaceholder

/**
 * Bottom sheet showing the signed-in member's reading log: sessions grouped
 * into "Currently Reading" and "Read". Mirrors web's ReadingLogModal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingLogBottomSheet(
    log: ReadingLog?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.reading_log),
                style = MaterialTheme.typography.titleMedium
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ReadingLogSection(
                    title = stringResource(R.string.shelf_currently_reading),
                    entries = log?.active.orEmpty()
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                ReadingLogSection(
                    title = stringResource(R.string.shelf_read),
                    entries = log?.finished.orEmpty()
                )
            }
        }
    }
}

@Composable
private fun ReadingLogSection(
    title: String,
    entries: List<ReadingLogEntry>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.nothing_here_yet),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                entries.forEach { entry -> ReadingLogEntryRow(entry) }
            }
        }
    }
}

@Composable
private fun ReadingLogEntryRow(entry: ReadingLogEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SubcomposeAsyncImage(
            model = entry.book?.imageUrl,
            contentDescription = entry.book?.title,
            modifier = Modifier
                .width(40.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop,
            loading = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) },
            error = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) }
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = entry.book?.title.orEmpty(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.book?.author.orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.club?.name.orEmpty().uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_ReadingLogBottomSheet() = KluvsTheme {
    ReadingLogSection(
        title = "Currently Reading",
        entries = listOf(
            ReadingLogEntry(
                sessionId = "s0",
                book = BookSummary(id = "b0", title = "How AI Thinks", author = "Nigel Toon"),
                club = ClubPreview(id = "c0", name = "Showcase Kluv")
            )
        )
    )
}
