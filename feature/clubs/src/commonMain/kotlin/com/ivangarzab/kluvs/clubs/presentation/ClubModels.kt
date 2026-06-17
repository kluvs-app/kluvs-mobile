package com.ivangarzab.kluvs.clubs.presentation

import com.ivangarzab.kluvs.model.Role
import kotlinx.datetime.LocalDateTime

/**
 * Lightweight UI model for club selection/listing.
 *
 * Contains minimal data needed to display and select clubs.
 * Used for multi-club support where user can switch between clubs.
 * [role] is the current user's role in this club, populated from the member's clubs list.
 */
data class ClubListItem(
    val id: String,
    val name: String,
    val role: Role? = null
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
    val discussions: List<DiscussionTimelineItemInfo>
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
