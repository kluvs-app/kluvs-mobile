package com.ivangarzab.kluvs.member.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.member.presentation.OnYourShelfResult
import com.ivangarzab.kluvs.member.presentation.ShelfItem
import com.ivangarzab.kluvs.member.presentation.UpNextItem
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.presentation.progress.GetSessionProgressUseCase
import com.ivangarzab.kluvs.presentation.state.DateTimeFormat
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * UseCase for fetching the "On Your Shelf" rows and "Up Next" discussion shown
 * on the Me screen. For each club the member belongs to, reads the active
 * session's own reading progress (via [GetSessionProgressUseCase]) and the
 * next upcoming discussion date; separately tracks the single nearest
 * upcoming discussion across all clubs. Mirrors web's ProfilePage `ShelfRow`
 * and "Up Next" band data.
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
     * Fetches all shelf rows and the nearest upcoming discussion for the
     * member's clubs with active sessions.
     *
     * @param userId The Discord user ID of the current user
     * @return Result containing [OnYourShelfResult] if successful, or error if failed
     */
    suspend operator fun invoke(userId: String): Result<OnYourShelfResult> {
        Bark.d("Fetching on-your-shelf items (User ID: $userId)")
        return memberRepository.getMemberByUserId(userId).mapCatching { member: Member ->
            val clubs = member.clubs ?: emptyList()
            Bark.d("Found ${clubs.size} clubs for user (User ID: $userId)")
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            var nearestDiscussion: Discussion? = null
            var nearestDiscussionClubName: String? = null

            val items = clubs.mapNotNull { club ->
                val fullClub = clubRepository.getClub(club.id).getOrNull()
                fullClub?.activeSession?.let { session ->
                    val ownProgress = getSessionProgress(session.id, session.book.pageCount).getOrNull()
                    val upcomingDiscussions = session.discussions.filter { it.date > now }
                    val nextDiscussionDate = upcomingDiscussions
                        .minByOrNull { it.date }
                        ?.let { formatDateTime(it.date, DateTimeFormat.DATE_ONLY) }

                    upcomingDiscussions.minByOrNull { it.date }?.let { candidate ->
                        if (nearestDiscussion == null || candidate.date < nearestDiscussion!!.date) {
                            nearestDiscussion = candidate
                            nearestDiscussionClubName = club.name
                        }
                    }

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

            val upNext = nearestDiscussion?.let { discussion ->
                UpNextItem(
                    title = discussion.title,
                    clubName = nearestDiscussionClubName.orEmpty(),
                    location = discussion.location,
                    date = formatDateTime(discussion.date, DateTimeFormat.DATE_ONLY)
                )
            }

            Bark.i("Loaded on-your-shelf items (Count: ${items.size}, Up next: ${upNext != null})")
            OnYourShelfResult(shelf = items, upNext = upNext)
        }.onFailure { error ->
            Bark.e("Failed to fetch on-your-shelf items (User ID: $userId). User will see empty shelf.", error)
        }
    }
}
