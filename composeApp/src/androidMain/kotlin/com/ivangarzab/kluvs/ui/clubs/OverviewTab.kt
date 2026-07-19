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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.clubs.presentation.ActiveSessionDetails
import com.ivangarzab.kluvs.clubs.presentation.BookInfo
import com.ivangarzab.kluvs.clubs.presentation.ClubDetails
import com.ivangarzab.kluvs.clubs.presentation.DiscussionInfo
import com.ivangarzab.kluvs.clubs.presentation.DiscussionTimelineItemInfo
import com.ivangarzab.kluvs.clubs.presentation.OwnProgressInfo
import com.ivangarzab.kluvs.clubs.presentation.SessionParticipantInfo
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.components.NoTabData
import com.ivangarzab.kluvs.ui.components.RoleEyebrow
import kotlinx.datetime.LocalDateTime

/**
 * Overview tab: club masthead, active-session summary (book, participation, own
 * progress), and an "up next" discussion teaser. Mirrors web's mobile Overview tab.
 * The full discussion timeline and end-session flow stay on the Discussions tab.
 */
@Composable
fun OverviewTab(
    modifier: Modifier = Modifier,
    clubDetails: ClubDetails? = null,
    sessionDetails: ActiveSessionDetails? = null,
    ownProgress: OwnProgressInfo? = null,
    userRole: Role? = null,
    onEditClub: () -> Unit = {},
    onDeleteClub: () -> Unit = {},
    onEditSession: () -> Unit = {},
    onEndSession: () -> Unit = {},
    onUpdateProgress: () -> Unit = {},
) {
    if (clubDetails == null) {
        NoTabData(
            modifier = modifier,
            text = R.string.no_club_details
        )
        return
    }

    val isOwner = userRole == Role.OWNER
    val isAdminOrAbove = userRole == Role.OWNER || userRole == Role.ADMIN

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Masthead
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.club_eyebrow).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = clubDetails.clubName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                ClubMetaRow(
                    userRole = userRole,
                    foundedYear = clubDetails.foundedYear,
                    memberCount = clubDetails.memberCount
                )
            }

            if (isOwner) {
                ClubOverflowMenu(
                    onEdit = onEditClub,
                    onDelete = onDeleteClub
                )
            }
        }

        if (sessionDetails != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            SessionSummary(
                sessionDetails = sessionDetails,
                ownProgress = ownProgress,
                isAdminOrAbove = isAdminOrAbove,
                onEditSession = onEditSession,
                onEndSession = onEndSession,
                onUpdateProgress = onUpdateProgress
            )

            val nextDiscussion = sessionDetails.discussions.firstOrNull { it.isNext }
            if (nextDiscussion != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                UpNextTeaser(discussion = nextDiscussion)
            }
        }
    }
}

@Composable
private fun SessionSummary(
    sessionDetails: ActiveSessionDetails,
    ownProgress: OwnProgressInfo?,
    isAdminOrAbove: Boolean,
    onEditSession: () -> Unit,
    onEndSession: () -> Unit,
    onUpdateProgress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.active_session_eyebrow).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = sessionDetails.book.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sessionDetails.book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isAdminOrAbove) {
                SessionOverflowMenu(
                    onEditSession = onEditSession,
                    onEndSession = onEndSession
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        val readingCount = sessionDetails.participants.count { it.isReading }
        Text(
            text = if (sessionDetails.participants.isNotEmpty()) {
                "$readingCount reading"
            } else {
                "No participants yet"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        OwnProgressRow(
            ownProgress = ownProgress,
            onUpdateProgress = onUpdateProgress
        )
    }
}

@Composable
private fun OwnProgressRow(
    ownProgress: OwnProgressInfo?,
    onUpdateProgress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LinearProgressIndicator(
                progress = { (ownProgress?.percent ?: 0) / 100f },
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onUpdateProgress) {
                Text(
                    text = if (ownProgress != null) "Update" else "Track Progress",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        ownProgress?.let {
            Text(
                text = it.label,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun UpNextTeaser(
    discussion: DiscussionTimelineItemInfo,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.up_next_eyebrow).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = discussion.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = discussion.location,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = discussion.date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ClubMetaRow(
    userRole: Role?,
    foundedYear: String?,
    memberCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        userRole?.let { role ->
            RoleEyebrow(role = role)
            MetaDot()
        }
        if (foundedYear != null) {
            Text(
                text = stringResource(R.string.founded_x, foundedYear).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MetaDot()
        }
        Text(
            text = stringResource(R.string.x_members, memberCount).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetaDot() {
    Box(
        modifier = Modifier
            .size(3.dp)
            .background(color = MaterialTheme.colorScheme.onSurfaceVariant, shape = CircleShape)
    )
}

@Composable
private fun ClubOverflowMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Club options",
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

@Composable
private fun SessionOverflowMenu(
    onEditSession: () -> Unit,
    onEndSession: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Session options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit Session") },
                onClick = {
                    expanded = false
                    onEditSession()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "End Session",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    expanded = false
                    onEndSession()
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_OverviewTab() = KluvsTheme {
    OverviewTab(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxSize(),
        clubDetails = ClubDetails(
            clubId = "club1",
            clubName = "Test Club Name",
            memberCount = 6,
            foundedYear = "2026",
            currentBook = BookInfo(
                title = "1984",
                author = "George Orwell",
                year = "1948",
                pageCount = 169
            ),
            nextDiscussion = DiscussionInfo(
                title = "Discussion #1",
                location = "Discord",
                formattedDate = "Tomorrow at 7:00 PM"
            )
        ),
        sessionDetails = ActiveSessionDetails(
            sessionId = "s0",
            book = BookInfo(title = "1984", author = "George Orwell", year = "1948", pageCount = 169),
            bookId = "b0",
            dueDate = "January 1st, 2030",
            rawDueDate = null,
            participants = listOf(
                SessionParticipantInfo(memberId = "0", isReading = true),
                SessionParticipantInfo(memberId = "1", isReading = false)
            ),
            discussions = listOf(
                DiscussionTimelineItemInfo(
                    id = "0",
                    title = "Chapters 10-19",
                    location = "Discord voice",
                    date = "Nov 3",
                    rawDate = LocalDateTime(2026, 11, 3, 19, 0),
                    isPast = false,
                    isNext = true
                )
            )
        ),
        ownProgress = OwnProgressInfo(
            progressId = "p0",
            type = ProgressType.PAGE,
            currentPage = 42,
            percentComplete = null,
            isCompleted = false,
            percent = 25,
            label = "42 of 169 pages"
        ),
        userRole = Role.OWNER
    )
}
