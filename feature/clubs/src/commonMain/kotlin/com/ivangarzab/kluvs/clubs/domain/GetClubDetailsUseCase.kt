package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.clubs.presentation.BookInfo
import com.ivangarzab.kluvs.clubs.presentation.ClubDetails
import com.ivangarzab.kluvs.clubs.presentation.DiscussionInfo
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.presentation.state.DateTimeFormat
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime

/**
 * UseCase for fetching complete club information for the GeneralTab.
 *
 * Transforms domain [Club] model into UI-friendly [ClubDetails] with:
 * - Calculated member count
 * - Current book being read
 * - Next upcoming discussion
 *
 * @param clubRepository Repository for club data
 * @param formatDateTime UseCase for formatting dates
 */
@OptIn(ExperimentalTime::class)
class GetClubDetailsUseCase(
    private val clubRepository: ClubRepository,
    private val formatDateTime: FormatDateTimeUseCase
) {
    /**
     * Fetches club details for the specified club.
     *
     * @param clubId The ID of the club to retrieve
     * @return Result containing [ClubDetails] if successful, or error if failed
     */
    suspend operator fun invoke(clubId: String, forceRefresh: Boolean = false): Result<ClubDetails> {
        Bark.d("Fetching club details (Club ID: $clubId)")
        return clubRepository.getClub(clubId, forceRefresh = forceRefresh).map { club: Club ->
            val now = now().toLocalDateTime(TimeZone.currentSystemDefault())
            val memberCount = club.members?.size ?: 0
            val hasActiveSession = club.activeSession != null
            val nextDiscussionCount = club.activeSession?.discussions?.count { it.date > now } ?: 0

            val details = ClubDetails(
                clubId = club.id,
                clubName = club.name,
                memberCount = memberCount,
                foundedYear = club.foundedDate?.year?.toString(),
                currentBook = club.activeSession?.book?.let {
                    BookInfo(
                        title = it.title,
                        author = it.author,
                        year = it.year?.toString(),
                        pageCount = it.pageCount,
                    )
                },
                nextDiscussion = club.activeSession?.discussions
                    ?.filter { it.date > now }
                    ?.minByOrNull { it.date }
                    ?.let {
                        DiscussionInfo(
                            title = it.title,
                            location = it.location ?: "TBD...",
                            formattedDate = formatDateTime(it.date, DateTimeFormat.FULL)
                        )
                    }
            )
            Bark.i("Loaded club details (Name: ${club.name}, Members: $memberCount, Active: $hasActiveSession, Next Discussions: $nextDiscussionCount)")
            details
        }.onFailure { error ->
            Bark.e("Failed to fetch club details (Club ID: $clubId). User will see error state.", error)
        }
    }
}
