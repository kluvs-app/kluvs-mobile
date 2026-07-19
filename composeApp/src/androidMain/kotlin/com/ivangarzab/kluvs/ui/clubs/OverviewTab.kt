package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.clubs.presentation.ActiveSessionDetails
import com.ivangarzab.kluvs.clubs.presentation.BookInfo
import com.ivangarzab.kluvs.clubs.presentation.ClubDetails
import com.ivangarzab.kluvs.clubs.presentation.DiscussionInfo
import com.ivangarzab.kluvs.clubs.presentation.DiscussionTimelineItemInfo
import com.ivangarzab.kluvs.clubs.presentation.MemberListItemInfo
import com.ivangarzab.kluvs.clubs.presentation.OwnProgressInfo
import com.ivangarzab.kluvs.clubs.presentation.SessionParticipantInfo
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.books.BookCoverPlaceholder
import com.ivangarzab.kluvs.ui.components.AvatarStack
import com.ivangarzab.kluvs.ui.components.AvatarStackMember
import com.ivangarzab.kluvs.ui.components.GhostButton
import com.ivangarzab.kluvs.ui.components.NoTabData
import kotlinx.datetime.LocalDateTime

/**
 * Overview tab: active-session summary (book, participation, own progress) and an
 * "up next" discussion teaser. Mirrors web's mobile Overview tab. The club masthead
 * (name, meta row) lives above the tab row in [ClubsScreenContent], not here.
 * The full discussion timeline and end-session flow stay on the Discussions tab.
 */
@Composable
fun OverviewTab(
    modifier: Modifier = Modifier,
    clubDetails: ClubDetails? = null,
    sessionDetails: ActiveSessionDetails? = null,
    ownProgress: OwnProgressInfo? = null,
    userRole: Role? = null,
    members: List<MemberListItemInfo> = emptyList(),
    currentUserId: String? = null,
    onEditSession: () -> Unit = {},
    onEndSession: () -> Unit = {},
    onUpdateProgress: () -> Unit = {},
    onCreateSession: () -> Unit = {},
    onToggleParticipation: (isReading: Boolean) -> Unit = {},
) {
    if (clubDetails == null) {
        NoTabData(
            modifier = modifier,
            text = R.string.no_club_details
        )
        return
    }

    val isAdminOrAbove = userRole == Role.OWNER || userRole == Role.ADMIN
    val currentMemberId = members.find { it.userId == currentUserId }?.memberId

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (sessionDetails != null) {
            val readingParticipants = sessionDetails.participants.filter { it.isReading }
            val readingMembers = readingParticipants.mapNotNull { participant ->
                members.find { it.memberId == participant.memberId }
            }.map { AvatarStackMember(id = it.memberId, name = it.name, avatarUrl = it.avatarUrl) }
            val isOwnReading = currentMemberId != null &&
                sessionDetails.participants.any { it.memberId == currentMemberId && it.isReading }

            SessionSummary(
                sessionDetails = sessionDetails,
                ownProgress = ownProgress,
                readingMembers = readingMembers,
                readingCount = readingParticipants.size,
                totalMemberCount = clubDetails.memberCount,
                isAdminOrAbove = isAdminOrAbove,
                isOwnReading = isOwnReading,
                canToggleParticipation = currentMemberId != null,
                onEditSession = onEditSession,
                onEndSession = onEndSession,
                onUpdateProgress = onUpdateProgress,
                onToggleParticipation = onToggleParticipation
            )

            val nextDiscussion = sessionDetails.discussions.firstOrNull { it.isNext }
            if (nextDiscussion != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                UpNextTeaser(discussion = nextDiscussion)
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        } else {
            NoActiveSessionState(
                isAdminOrAbove = isAdminOrAbove,
                onCreateSession = onCreateSession
            )
        }
    }
}

@Composable
private fun SessionSummary(
    sessionDetails: ActiveSessionDetails,
    ownProgress: OwnProgressInfo?,
    readingMembers: List<AvatarStackMember>,
    readingCount: Int,
    totalMemberCount: Int,
    isAdminOrAbove: Boolean,
    isOwnReading: Boolean,
    canToggleParticipation: Boolean,
    onEditSession: () -> Unit,
    onEndSession: () -> Unit,
    onUpdateProgress: () -> Unit,
    onToggleParticipation: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SubcomposeAsyncImage(
                model = sessionDetails.book.imageUrl,
                contentDescription = sessionDetails.book.title,
                modifier = Modifier
                    .width(80.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
                loading = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) },
                error = { BookCoverPlaceholder(modifier = Modifier.fillMaxWidth()) }
            )

            Column(modifier = Modifier.weight(1f)) {
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
                    }
                    if (isAdminOrAbove) {
                        SessionOverflowMenu(
                            onEditSession = onEditSession,
                            onEndSession = onEndSession
                        )
                    }
                }
                Text(
                    text = sessionDetails.book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (readingMembers.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarStack(members = readingMembers, size = 24.dp)
                    Text(
                        text = stringResource(R.string.x_of_y_reading, readingCount, totalMemberCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.no_participants_yet),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canToggleParticipation) {
                GhostButton(
                    text = stringResource(if (isOwnReading) R.string.opt_out else R.string.join_this_read),
                    onClick = { onToggleParticipation(!isOwnReading) }
                )
            }
        }

        if (isOwnReading) {
            Spacer(Modifier.height(12.dp))
            OwnProgressRow(
                ownProgress = ownProgress,
                onUpdateProgress = onUpdateProgress
            )
        }
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
            GhostButton(
                text = "Update",
                onClick = onUpdateProgress
            )
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
private fun NoActiveSessionState(
    isAdminOrAbove: Boolean,
    onCreateSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_session_yet_eyebrow).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.start_reading_together),
            style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (isAdminOrAbove) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onCreateSession) {
                Text(stringResource(R.string.start_session))
            }
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
            .background(color = MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 16.dp)
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
        members = listOf(
            MemberListItemInfo(memberId = "0", name = "Ana Silva", handle = "@ana", avatarUrl = null, role = Role.OWNER, userId = "u0")
        ),
        currentUserId = "u0",
        userRole = Role.OWNER
    )
}
