package com.ivangarzab.kluvs.appfunctions.discussions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionSerializable
import com.ivangarzab.kluvs.appfunctions.AppFunctionSession
import com.ivangarzab.kluvs.appfunctions.getOrThrowAppFunction
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.AttendanceStatus
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Session
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toJavaLocalDateTime
import java.time.LocalDateTime

/** A scheduled meeting where a book club discusses its current book. */
@RequiresApi(36)
@AppFunctionSerializable(isDescribedByKDoc = true)
data class DiscussionSummary(
    /** The discussion's ID. */
    val discussionId: String,
    /** The discussion's title. */
    val title: String,
    /** When the discussion meets, in the device's local time. */
    val dateTime: LocalDateTime,
    /** Where the discussion meets, such as an address or "Online"; null if not set. */
    val location: String?,
    /** The ID of the club holding the discussion. */
    val clubId: String,
    /** The name of the club holding the discussion. */
    val clubName: String,
    /** The title of the book the club is reading. */
    val bookTitle: String,
)

internal data class DiscussionContext(val club: Club, val session: Session, val discussion: Discussion)

@RequiresApi(36)
internal fun DiscussionContext.toSummary() = DiscussionSummary(
    discussionId = discussion.id,
    title = discussion.title,
    dateTime = discussion.date.toJavaLocalDateTime(),
    location = discussion.location?.takeIf { it.isNotBlank() },
    clubId = club.id,
    clubName = club.name,
    bookTitle = session.book.title,
)

/** Active sessions of the signed-in member's clubs, optionally narrowed to [clubId]. */
internal suspend fun activeSessions(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    forceRefresh: Boolean = false,
    clubId: String? = null,
): List<Pair<Club, Session>> = coroutineScope {
    session.requireMember().clubs.orEmpty()
        .filter { clubId == null || it.id == clubId }
        .map { club -> async { clubRepository.getClub(club.id, forceRefresh = forceRefresh).getOrThrowAppFunction() } }
        .awaitAll()
        .mapNotNull { club -> club.activeSession?.let { club to it } }
}

/**
 * Finds [discussionId] among the user's clubs' active sessions — the only discussions the app
 * shows. Retries with a refresh so a just-created discussion isn't missed by a stale cache.
 */
internal suspend fun findDiscussion(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    discussionId: String,
): DiscussionContext {
    suspend fun search(forceRefresh: Boolean) = activeSessions(session, clubRepository, forceRefresh)
        .firstNotNullOfOrNull { (club, activeSession) ->
            activeSession.discussions.find { it.id == discussionId }?.let { DiscussionContext(club, activeSession, it) }
        }
    return search(forceRefresh = false) ?: search(forceRefresh = true)
        ?: throw AppFunctionElementNotFoundException("No discussion with ID $discussionId in your clubs' current reads.")
}

internal fun AttendanceStatus.toRsvp(): String = when (this) {
    AttendanceStatus.YES -> RSVP_GOING
    AttendanceStatus.MAYBE -> RSVP_MAYBE
    AttendanceStatus.NO -> RSVP_NOT_GOING
}

internal const val RSVP_GOING = "going"
internal const val RSVP_MAYBE = "maybe"
internal const val RSVP_NOT_GOING = "not_going"
internal const val RSVP_NONE = "none"
