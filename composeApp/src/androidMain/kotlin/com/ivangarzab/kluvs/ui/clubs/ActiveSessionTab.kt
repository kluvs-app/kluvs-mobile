package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.clubs.presentation.ActiveSessionDetails
import com.ivangarzab.kluvs.clubs.presentation.BookInfo
import com.ivangarzab.kluvs.clubs.presentation.DiscussionTimelineItemInfo
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.components.NoTabData
import kotlinx.datetime.LocalDateTime

@Composable
fun ActiveSessionTab(
    modifier: Modifier = Modifier,
    sessionDetails: ActiveSessionDetails?,
    userRole: Role? = null,
    onCreateSession: () -> Unit = {},
    onEditSession: () -> Unit = {},
    onCreateDiscussion: () -> Unit = {},
    onEditDiscussion: (discussionId: String) -> Unit = {},
    onDeleteDiscussion: (discussionId: String) -> Unit = {},
) {
    val isOwner = userRole == Role.OWNER
    val isAdminOrAbove = userRole == Role.OWNER || userRole == Role.ADMIN

    if (sessionDetails == null) {
        if (isOwner) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = stringResource(R.string.no_session_details),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onCreateSession) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Create Session")
                }
            }
        } else {
            NoTabData(
                modifier = modifier,
                text = R.string.no_session_details
            )
        }
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Session Book Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sessionDetails.book.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.by_author, sessionDetails.book.author),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                    append(stringResource(R.string.due_date))
                                }
                                append(" ${sessionDetails.dueDate}")
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (isOwner) {
                        IconButton(onClick = onEditSession) {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = "Edit session",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                text = stringResource(R.string.discussion_timeline),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )

            sessionDetails.discussions.let { discussions ->
                // Timeline
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(discussions) { index, discussion ->
                        DiscussionTimelineItem(
                            discussion = discussion,
                            isFirst = index == 0,
                            isLast = index == discussions.size - 1,
                            showAdminActions = isAdminOrAbove,
                            onEdit = { onEditDiscussion(discussion.id) },
                            onDelete = { onDeleteDiscussion(discussion.id) }
                        )
                    }
                }
            }

            if (isAdminOrAbove) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCreateDiscussion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Add Discussion",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DiscussionTimelineItem(
    discussion: DiscussionTimelineItemInfo,
    isFirst: Boolean,
    isLast: Boolean,
    showAdminActions: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.surfaceVariant
    val rowColor = if (discussion.isNext) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.background
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = rowColor, shape = RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Vertical line through entire height
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top line section
                if (!isFirst) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(40.dp)
                            .background(lineColor)
                    )
                } else {
                    Spacer(Modifier.height(40.dp))
                }

                // Spacer for bottom part
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(if (!isLast) lineColor else Color.Transparent)
                )
            }

            // Circle indicator positioned at top with padding
            Box(
                modifier = Modifier
                    .padding(top = 38.dp)
                    .size(24.dp)
                    .background(
                        color = if (discussion.isPast || discussion.isNext) {
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = if (discussion.isPast) 0.75f else 1.0f
                            )
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (discussion.isPast) {
                    Icon(
                        painter = painterResource(R.drawable.ic_checkmark),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // Discussion content
        val textColor = MaterialTheme.colorScheme.onSurface.copy(
            alpha = if (discussion.isPast) 0.5f else 1.0f
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = discussion.title,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.ic_location),
                        contentDescription = null,
                        tint = textColor
                    )
                    Text(
                        text = discussion.location,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = discussion.date,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (showAdminActions) {
                DiscussionOverflowMenu(
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun DiscussionOverflowMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Discussion options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_ActiveSessionTab() = KluvsTheme {
    ActiveSessionTab(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        sessionDetails = ActiveSessionDetails(
            sessionId = "0",
            book = BookInfo(title = "1984", author = "George Orwell", year = "1948", pageCount = null),
            dueDate = "January 1st, 2030",
            rawDueDate = null,
            discussions = listOf(
                DiscussionTimelineItemInfo(id = "0", title = "Discussion #1", location = "Coffee Shop", date = "Jan 15, 2025 at 7:00 PM", isPast = true, isNext = false, rawDate = LocalDateTime(2025, 1, 15, 19, 0)),
                DiscussionTimelineItemInfo(id = "1", title = "Discussion #2", location = "Library", date = "Jan 29, 2025 at 7:00 PM", isPast = true, isNext = false, rawDate = LocalDateTime(2025, 1, 29, 19, 0)),
                DiscussionTimelineItemInfo(id = "2", title = "Discussion #3", location = "Book Store", date = "Feb 12, 2025 at 7:00 PM", isPast = false, isNext = true, rawDate = LocalDateTime(2025, 2, 12, 19, 0)),
                DiscussionTimelineItemInfo(id = "3", title = "Discussion #4", location = "Community Center", date = "Feb 26, 2025 at 7:00 PM", isPast = false, isNext = false, rawDate = LocalDateTime(2025, 2, 26, 19, 0)),
            )
        ),
        userRole = Role.OWNER
    )
}
