package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.clubs.presentation.ActiveSessionDetails
import com.ivangarzab.kluvs.clubs.presentation.BookInfo
import com.ivangarzab.kluvs.clubs.presentation.DiscussionTimelineItemInfo
import com.ivangarzab.kluvs.model.AttendanceRoster
import com.ivangarzab.kluvs.model.AttendanceStatus
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.clubs.presentation.SessionParticipantInfo
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.theme.brandPrimary
import com.ivangarzab.kluvs.theme.foregroundWarmDisabled
import com.ivangarzab.kluvs.ui.components.AttendanceControl
import com.ivangarzab.kluvs.ui.components.GhostButton
import com.ivangarzab.kluvs.ui.components.NoTabData
import kotlinx.datetime.LocalDateTime

@Composable
fun ActiveSessionTab(
    modifier: Modifier = Modifier,
    sessionDetails: ActiveSessionDetails?,
    userRole: Role? = null,
    onCreateSession: () -> Unit = {},
    onCreateDiscussion: () -> Unit = {},
    onEditDiscussion: (discussionId: String) -> Unit = {},
    onDeleteDiscussion: (discussionId: String) -> Unit = {},
    onOpenNote: (discussionId: String) -> Unit = {},
    discussionRosters: Map<String, AttendanceRoster> = emptyMap(),
    onLoadAttendanceRoster: (discussionId: String) -> Unit = {},
    onSetAttendance: (discussionId: String, status: AttendanceStatus) -> Unit = { _, _ -> },
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

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (sessionDetails.discussions.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.x_discussions_scheduled, sessionDetails.discussions.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic
                    )
                )
            }
            if (isAdminOrAbove) {
                GhostButton(
                    text = stringResource(R.string.add_discussion),
                    onClick = onCreateDiscussion,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        sessionDetails.discussions.let { discussions ->
            if (discussions.isEmpty()) {
                Text(
                    text = "No discussions scheduled yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
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
                            onDelete = { onDeleteDiscussion(discussion.id) },
                            onOpenNote = { onOpenNote(discussion.id) },
                            attendanceRoster = discussionRosters[discussion.id],
                            onLoadRoster = { onLoadAttendanceRoster(discussion.id) },
                            onSetAttendance = { status -> onSetAttendance(discussion.id, status) }
                        )
                    }
                }
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
    onOpenNote: () -> Unit = {},
    attendanceRoster: AttendanceRoster? = null,
    onLoadRoster: () -> Unit = {},
    onSetAttendance: (AttendanceStatus) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(discussion.id) { onLoadRoster() }

    // A dot/line is "lit" (copper) once its discussion is past or is the current next one —
    // this is what makes the rail read as a continuous copper thread through completed items.
    val isLit = discussion.isPast || discussion.isNext
    val neutralLineColor = MaterialTheme.colorScheme.surfaceVariant
    val litLineColor = brandPrimary.copy(alpha = 0.4f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .alpha(if (discussion.isPast) 0.5f else 1f),
        verticalAlignment = Alignment.Top
    ) {
        // Rail: fills the row's actual height (whatever the content column ends up
        // needing, attendance pill included) so the connecting line never falls short.
        Column(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fixed offset down to the dot — lit if this discussion itself is past/next
            // (the line leading in from a completed/current dot above).
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(38.dp)
                        .background(if (isLit) litLineColor else neutralLineColor)
                )
            } else {
                Spacer(Modifier.height(38.dp))
            }

            // Dot indicator
            Box(contentAlignment = Alignment.Center) {
                if (discussion.isNext) {
                    // Soft glow ring behind the "next" dot
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(brandPrimary.copy(alpha = 0.10f), CircleShape)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(if (discussion.isNext) 24.dp else 16.dp)
                        .background(
                            color = when {
                                discussion.isPast -> foregroundWarmDisabled
                                discussion.isNext -> brandPrimary
                                else -> Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .then(
                            if (!discussion.isPast && !discussion.isNext) {
                                Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (discussion.isPast) {
                        Icon(
                            painter = painterResource(R.drawable.ic_checkmark),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Fills the rest of the row's height, down to the next dot — lit only if
            // this discussion is past; the line out of "next" stays neutral.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(2.dp)
                    .background(
                        when {
                            isLast -> Color.Transparent
                            discussion.isPast -> litLineColor
                            else -> neutralLineColor
                        }
                    )
            )
        }

        Spacer(Modifier.width(12.dp))

        val textColor = MaterialTheme.colorScheme.onSurface
        val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (discussion.isNext) {
                    Text(
                        text = "UP NEXT",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(
                    text = discussion.title,
                    color = textColor,
                    style = if (discussion.isNext) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall
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
                        tint = secondaryTextColor
                    )
                    Text(
                        text = discussion.location,
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = discussion.date,
                    color = secondaryTextColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                AttendanceControl(
                    roster = attendanceRoster,
                    disabled = discussion.isPast,
                    onSetAttendance = onSetAttendance
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenNote) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Discussion note",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        sessionDetails = ActiveSessionDetails(
            sessionId = "0",
            book = BookInfo(title = "1984", author = "George Orwell", year = "1948", pageCount = null),
            bookId = "b0",
            participants = listOf(
                SessionParticipantInfo(memberId = "0", isReading = true),
                SessionParticipantInfo(memberId = "1", isReading = false)
            ),
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
