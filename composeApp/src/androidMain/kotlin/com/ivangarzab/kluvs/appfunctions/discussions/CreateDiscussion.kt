package com.ivangarzab.kluvs.appfunctions.discussions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import com.ivangarzab.kluvs.appfunctions.AppFunctionSession
import com.ivangarzab.kluvs.appfunctions.getOrThrowAppFunction
import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionUseCase
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime

@RequiresApi(36)
internal suspend fun createDiscussion(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    createDiscussion: CreateDiscussionUseCase,
    clubId: String,
    title: String,
    location: String,
    dateTime: LocalDateTime,
): DiscussionSummary {
    if (title.isBlank()) throw AppFunctionInvalidArgumentException("The discussion title can't be blank.")
    if (location.isBlank()) throw AppFunctionInvalidArgumentException("The discussion location can't be blank.")
    if (dateTime.isBefore(LocalDateTime.now())) {
        throw AppFunctionInvalidArgumentException("The discussion must be scheduled in the future.")
    }
    val role = session.requireClubRole(clubId)
    val club = clubRepository.getClub(clubId, forceRefresh = true).getOrThrowAppFunction()
    val activeSession = club.activeSession
        ?: throw AppFunctionElementNotFoundException("${club.name} isn't reading a book right now, so it has no session to add a discussion to.")
    val created = createDiscussion(
        CreateDiscussionUseCase.Params(
            sessionId = activeSession.id,
            title = title.trim(),
            location = location.trim(),
            date = dateTime.toKotlinLocalDateTime(),
        ),
        role,
    ).getOrThrowAppFunction()
    return DiscussionContext(club, activeSession, created).toSummary()
}
