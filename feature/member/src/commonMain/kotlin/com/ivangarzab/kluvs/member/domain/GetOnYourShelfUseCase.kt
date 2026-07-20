package com.ivangarzab.kluvs.member.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.member.presentation.ShelfItem
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.presentation.progress.GetSessionProgressUseCase
import com.ivangarzab.kluvs.presentation.state.DateTimeFormat
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * UseCase for fetching the "On Your Shelf" rows shown on the Me screen: the
 * active-session book for each club the member belongs to, with their own
 * reading progress on that session (via [GetSessionProgressUseCase]) and the
 * next upcoming discussion date. Mirrors web's ProfilePage `ShelfRow` data.
 *
 * @param memberRepository Repository for member data
 * @param clubRepository Repository for club data
 * @param getSessionProgress UseCase for the member's own progress on a session
 * @param formatDateTime UseCase for formatting dates
 */
@OptIn(ExperimentalTime::class)
class GetOnYourShelfUseCase(
    private val memberRepository: MemberRepository,
    private val clubRepository: ClubRepository,
    private val getSessionProgress: GetSessionProgressUseCase,
    private val formatDateTime: FormatDateTimeUseCase
) {
    /**
     * Fetches all shelf rows for the member's clubs with active sessions.
     *
     * @param userId The Discord user ID of the current user
     * @return Result containing list of [ShelfItem] if successful, or error if failed
     */
    suspend operator fun invoke(userId: String): Result<List<ShelfItem>> {
        Bark.d("Fetching on-your-shelf items (User ID: $userId)")
        return memberRepository.getMemberByUserId(userId).mapCatching { member: Member ->
            val clubs = member.clubs ?: emptyList()
            Bark.d("Found ${clubs.size} clubs for user (User ID: $userId)")
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            val items = clubs.mapNotNull { club ->
                val fullClub = clubRepository.getClub(club.id).getOrNull()
                fullClub?.activeSession?.let { session ->
                    val ownProgress = getSessionProgress(session.id, session.book.pageCount).getOrNull()
                    val nextDiscussionDate = session.discussions
                        .filter { it.date > now }
                        .minByOrNull { it.date }
                        ?.let { formatDateTime(it.date, DateTimeFormat.DATE_ONLY) }

                    val item = ShelfItem(
                        sessionId = session.id,
                        bookId = session.book.id,
                        bookTitle = session.book.title,
                        bookAuthor = session.book.author,
                        bookCoverUrl = session.book.imageUrl,
                        bookPageCount = session.book.pageCount,
                        clubId = club.id,
                        clubName = club.name,
                        nextDiscussionDate = nextDiscussionDate,
                        ownProgress = ownProgress
                    )
                    Bark.d("Added shelf item (Title: ${session.book.title}, Club: ${club.name})")
                    item
                }
            }
            Bark.i("Loaded on-your-shelf items (Count: ${items.size})")
            items
        }.onFailure { error ->
            Bark.e("Failed to fetch on-your-shelf items (User ID: $userId). User will see empty shelf.", error)
        }
    }
}
