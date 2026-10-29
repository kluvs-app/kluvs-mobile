package com.ivangarzab.kluvs.appfunctions.discussions

import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import com.ivangarzab.kluvs.appfunctions.AppFunctionSession
import com.ivangarzab.kluvs.appfunctions.getOrThrowAppFunction
import com.ivangarzab.kluvs.clubs.domain.ClearAttendanceUseCase
import com.ivangarzab.kluvs.clubs.domain.GetAttendanceRosterUseCase
import com.ivangarzab.kluvs.clubs.domain.SetAttendanceUseCase
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.AttendanceStatus

/** The user's RSVP to a discussion after the change, with the updated attendance counts. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class RsvpResult(
    /** The discussion's ID. */
    val discussionId: String,
    /** The discussion's title. */
    val title: String,
    /** The user's RSVP: "going", "maybe", or "not_going"; null if cleared. */
    val myRsvp: String?,
    /** How many club members are going. */
    val goingCount: Int,
    /** How many club members might go. */
    val maybeCount: Int,
    /** How many club members aren't going. */
    val notGoingCount: Int,
)

internal suspend fun rsvpToDiscussion(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    setAttendance: SetAttendanceUseCase,
    clearAttendance: ClearAttendanceUseCase,
    getAttendanceRoster: GetAttendanceRosterUseCase,
    discussionId: String,
    response: String,
): RsvpResult {
    val status = when (response.trim().lowercase()) {
        RSVP_GOING -> AttendanceStatus.YES
        RSVP_MAYBE -> AttendanceStatus.MAYBE
        RSVP_NOT_GOING -> AttendanceStatus.NO
        RSVP_NONE -> null
        else -> throw AppFunctionInvalidArgumentException(
            "Response must be \"$RSVP_GOING\", \"$RSVP_MAYBE\", \"$RSVP_NOT_GOING\", or \"$RSVP_NONE\"."
        )
    }
    val context = findDiscussion(session, clubRepository, discussionId)
    if (status == null) {
        clearAttendance(discussionId).getOrThrowAppFunction()
    } else {
        setAttendance(SetAttendanceUseCase.Params(discussionId, status)).getOrThrowAppFunction()
    }
    val roster = getAttendanceRoster(discussionId).getOrThrowAppFunction()
    return RsvpResult(
        discussionId = discussionId,
        title = context.discussion.title,
        myRsvp = roster.myStatus?.toRsvp(),
        goingCount = roster.responses.count { it.status == AttendanceStatus.YES },
        maybeCount = roster.responses.count { it.status == AttendanceStatus.MAYBE },
        notGoingCount = roster.responses.count { it.status == AttendanceStatus.NO },
    )
}
