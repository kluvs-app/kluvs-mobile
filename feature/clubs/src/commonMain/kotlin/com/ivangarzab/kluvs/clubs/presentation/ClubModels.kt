package com.ivangarzab.kluvs.clubs.presentation

import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.Role
import kotlinx.datetime.LocalDateTime

/**
 * UI model for a row in the clubs list screen (mirrors web's `/clubs` page).
 *
 * [bookTitle], [memberAvatarUrls], and [memberCount] are enriched via a per-club
 * detail fetch (mirroring web's `ClubsPage` per-row enrichment) and may be absent
 * if that fetch fails — the row still renders with [name] and [role].
 */
data class ClubListItem(
    val id: String,
    val name: String,
    val role: Role? = null,
    val bookTitle: String? = null,
    val bookCoverUrl: String? = null,
    val memberAvatarUrls: List<MemberAvatarInfo> = emptyList(),
    val memberCount: Int = 0
)

/** Minimal member identity needed to render an [AvatarStackMember]-equivalent row. */
data class MemberAvatarInfo(
    val memberId: String,
    val name: String,
    val avatarUrl: String?
)

/**
 * UI model for club overview displayed in GeneralTab.
 *
 * Contains formatted and derived data ready for direct display.
 */
data class ClubDetails(
    val clubId: String,
    val clubName: String,
    val memberCount: Int,
    val foundedYear: String?,
    val currentBook: BookInfo?,
    val nextDiscussion: DiscussionInfo?
)

/**
 * UI model for active reading session displayed in ActiveSessionTab.
 *
 * Contains the current book being read and a timeline of discussions.
 */
data class ActiveSessionDetails(
    val sessionId: String,
    val book: BookInfo,
    val dueDate: String,
    val rawDueDate: LocalDateTime?,
    val discussions: List<DiscussionTimelineItemInfo>,
    /** ID of the session's book — needed to create a progress entry against it. */
    val bookId: String = "",
    /** Per-member participation list; empty when the API response omits it. */
    val participants: List<SessionParticipantInfo> = emptyList()
)

/**
 * UI model for a member's participation in the active session.
 *
 * Powers the reading/skipping indicator in MembersTab and the
 * credited-readers preview in the end-session confirmation.
 */
data class SessionParticipantInfo(
    val memberId: String,
    val isReading: Boolean
)

/**
 * UI model for the signed-in member's own reading progress on the active session.
 *
 * [percent] and [label] are pre-computed for direct display, mirroring the
 * web app's ProgressRow ("X of Y pages", "N% complete", "Finished").
 */
data class OwnProgressInfo(
    val progressId: String,
    val type: ProgressType,
    val currentPage: Int?,
    val percentComplete: Float?,
    val isCompleted: Boolean,
    val percent: Int,
    val label: String
)

/**
 * UI model for a discussion in the timeline.
 *
 * Status flags (isPast, isNext, isFuture) enable UI to render different states.
 */
data class DiscussionTimelineItemInfo(
    val id: String,
    val title: String,
    val location: String,
    val date: String,
    val rawDate: LocalDateTime,
    val isPast: Boolean,
    val isNext: Boolean
)

/**
 * UI model for book information.
 *
 * Simplified view of Book domain model with only UI-needed fields.
 */
data class BookInfo(
    val title: String,
    val author: String,
    val year: String?,
    val pageCount: Int?
)

/**
 * UI model for upcoming discussion information.
 *
 * Used in GeneralTab to show next scheduled discussion.
 */
data class DiscussionInfo(
    val title: String,
    val location: String,
    val formattedDate: String
)

/**
 * UI model for member displayed in MembersTab list.
 *
 * [userId] mirrors [com.ivangarzab.kluvs.model.Member.userId] and is used by the UI to
 * identify the currently signed-in user's own row so self-action controls can be hidden.
 */
data class MemberListItemInfo(
    val memberId: String,
    val name: String,
    val handle: String,
    val avatarUrl: String?,
    val role: Role,
    val userId: String? = null
)

/**
 * Represents the outcome of a mutation operation (create/update/delete).
 * The ViewModel maps UseCase Result<T> into this for the UI layer.
 */
sealed interface OperationResult {
    data class Success(val message: String) : OperationResult
    data class Error(val message: String) : OperationResult
}
