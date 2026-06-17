package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.clubs.presentation.ActiveSessionDetails
import com.ivangarzab.kluvs.clubs.presentation.BookInfo
import com.ivangarzab.kluvs.clubs.presentation.ClubListItem
import com.ivangarzab.kluvs.clubs.presentation.DiscussionTimelineItemInfo
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.presentation.state.DateTimeFormat
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime

/**
 * UseCase for fetching active reading session with discussion timeline for ActiveSessionTab.
 *
 * Transforms domain [Club] model into UI-friendly [ActiveSessionDetails] with:
 * - Book information
 * - Sorted discussions with status indicators (isPast, isNext, isFuture)
 * - Formatted dates
 *
 * @param clubRepository Repository for club data
 * @param formatDateTime UseCase for formatting dates
 */
@OptIn(ExperimentalTime::class)
class GetActiveSessionUseCase(
    private val clubRepository: ClubRepository,
    private val formatDateTime: FormatDateTimeUseCase
) {
    /**
     * Fetches active session details for the specified club.
     *
     * Returns null if the club has no active session.
     * Discussions are sorted chronologically and marked with status flags.
     *
     * @param clubId The ID of the club to retrieve the active session for
     * @return Result containing [ActiveSessionDetails] if successful, null if no active session, or error if failed
     */
    suspend operator fun invoke(clubId: String, forceRefresh: Boolean = false): Result<ActiveSessionDetails?> {
        Bark.d("Fetching active session (Club ID: $clubId)")
        return clubRepository.getClub(clubId, forceRefresh = forceRefresh).map { club: Club ->
            club.activeSession?.let { session ->
                Bark.d("Found active session (Session ID: ${session.id})")
                val now = now().toLocalDateTime(TimeZone.currentSystemDefault())
                val sortedDiscussions = session.discussions.sortedBy { it.date }

                // Find the index of the next discussion (first future discussion)
                // Compare dates only - discussions remain active until the day passes
                val nextDiscussionIndex = sortedDiscussions.indexOfFirst { it.date.date > now.date }

                val details = ActiveSessionDetails(
                    sessionId = session.id,
                    book = BookInfo(
                        title = session.book.title,
                        author = session.book.author,
                        year = session.book.year?.toString(),
                        pageCount = session.book.pageCount
                    ),
                    dueDate = session.dueDate?.let { formatDateTime(it, DateTimeFormat.DATE_ONLY) } ?: "No due date",
                    rawDueDate = session.dueDate,
                    discussions = sortedDiscussions.mapIndexed { index, discussion ->
                        DiscussionTimelineItemInfo(
                            id = discussion.id,
                            title = discussion.title,
                            location = discussion.location ?: "TBD...",
                            date = formatDateTime(discussion.date, DateTimeFormat.FULL),
                            rawDate = discussion.date,
                            // If no upcoming discussions (nextDiscussionIndex == -1), all are past
                            // Otherwise, discussions before the next one are past
                            isPast = nextDiscussionIndex == -1 || index < nextDiscussionIndex,
                            // Only mark as next if there IS a next discussion
                            isNext = nextDiscussionIndex != -1 && index == nextDiscussionIndex
                        )
                    }
                )
                Bark.i("Loaded active session details (Book: ${session.book.title}, Discussions: ${sortedDiscussions.size})")
                details
            } ?: run {
                Bark.d("No active session found for club (Club ID: $clubId)")
                null
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch active session (Club ID: $clubId). Returning null on error.", error)
        }
    }
}

/**
 * UseCase for fetching the current user's [Club] list for the MainScreen.
 *
 * Extract list of clubs form the Member model into a UI-friendly list.
 *
 * @param memberRepository Repository for member data
 */
class GetMemberClubsUseCase(
    private val memberRepository: MemberRepository
) {
    /**
     * Fetches all clubs for a member by their user ID.
     *
     * @param userId The auth user ID to look up
     * @return Result containing list of [com.ivangarzab.kluvs.clubs.presentation.ClubListItem], or error if failed
     */
    suspend operator fun invoke(userId: String): Result<List<ClubListItem>> {
        Bark.d("Fetching member clubs (User ID: $userId)")
        return memberRepository.getMemberByUserId(userId).map { member ->
            val clubItems = member.clubs?.map { club ->
                ClubListItem(
                    id = club.id,
                    name = club.name,
                    role = club.role
                )
            } ?: emptyList()
            Bark.i("Loaded member clubs (Count: ${clubItems.size})")
            clubItems
        }.onFailure { error ->
            Bark.e("Failed to fetch member clubs (User ID: $userId). User will see empty clubs list.", error)
        }
    }

}