package com.ivangarzab.kluvs.appfunctions.discussions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionSerializable
import com.ivangarzab.kluvs.appfunctions.AppFunctionSession
import com.ivangarzab.kluvs.appfunctions.getOrThrowAppFunction
import com.ivangarzab.kluvs.clubs.domain.GetAttendanceRosterUseCase
import com.ivangarzab.kluvs.clubs.domain.GetDiscussionNoteUseCase
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.AttendanceStatus
import java.time.LocalDate

/** Full details of a book club discussion, including the user's own RSVP and note. */
@RequiresApi(36)
@AppFunctionSerializable(isDescribedByKDoc = true)
data class DiscussionDetails(
    /** When, where, and for which club and book the discussion is. */
    val discussion: DiscussionSummary,
    /** The author of the book the club is reading. */
    val bookAuthor: String,
    /** The user's RSVP: "going", "maybe", or "not_going"; null if they haven't responded. */
    val myRsvp: String?,
    /** How many club members are going. */
    val goingCount: Int,
    /** How many club members might go. */
    val maybeCount: Int,
    /** How many club members aren't going. */
    val notGoingCount: Int,
    /** How many members the club has. */
    val clubMemberCount: Int,
    /** The user's private note for this discussion; null if they haven't written one. */
    val myNote: String?,
)

@RequiresApi(36)
internal suspend fun listUpcomingDiscussions(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    clubId: String?,
): List<DiscussionSummary> {
    val today = LocalDate.now()
    return activeSessions(session, clubRepository, clubId = clubId)
        .flatMap { (club, activeSession) -> activeSession.discussions.map { DiscussionContext(club, activeSession, it) } }
        .map { it.toSummary() }
        .filter { !it.dateTime.toLocalDate().isBefore(today) }
        .sortedBy { it.dateTime }
}

@RequiresApi(36)
internal suspend fun getDiscussionDetails(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    getAttendanceRoster: GetAttendanceRosterUseCase,
    getDiscussionNote: GetDiscussionNoteUseCase,
    discussionId: String,
): DiscussionDetails {
    val context = findDiscussion(session, clubRepository, discussionId)
    val roster = getAttendanceRoster(discussionId).getOrThrowAppFunction()
    val note = getDiscussionNote(discussionId).getOrThrowAppFunction()
    return DiscussionDetails(
        discussion = context.toSummary(),
        bookAuthor = context.session.book.author,
        myRsvp = roster.myStatus?.toRsvp(),
        goingCount = roster.responses.count { it.status == AttendanceStatus.YES },
        maybeCount = roster.responses.count { it.status == AttendanceStatus.MAYBE },
        notGoingCount = roster.responses.count { it.status == AttendanceStatus.NO },
        clubMemberCount = roster.totalMembers,
        myNote = note?.content,
    )
}
