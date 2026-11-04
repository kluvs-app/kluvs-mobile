package com.ivangarzab.kluvs.appfunctions.discussions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeniedException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import com.ivangarzab.kluvs.appfunctions.AppFunctionSession
import com.ivangarzab.kluvs.appfunctions.DiscussionContext
import com.ivangarzab.kluvs.appfunctions.getOrThrowAppFunction
import com.ivangarzab.kluvs.appfunctions.resolveClub
import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionUseCase
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime

@RequiresApi(36)
internal suspend fun createDiscussion(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    createDiscussion: CreateDiscussionUseCase,
    title: String,
    location: String,
    dateTime: LocalDateTime,
    club: String?,
): DiscussionSummary {
    if (title.isBlank()) throw AppFunctionInvalidArgumentException("The discussion title can't be blank.")
    if (location.isBlank()) throw AppFunctionInvalidArgumentException("The discussion location can't be blank.")
    if (dateTime.isBefore(LocalDateTime.now())) {
        throw AppFunctionInvalidArgumentException("The discussion must be scheduled in the future.")
    }
    val resolved = resolveClub(session, clubRepository, club, needsActiveSession = true)
    val role = resolved.role
        ?: throw AppFunctionDeniedException("Couldn't confirm your role in ${resolved.club.name}.")
    val activeSession = checkNotNull(resolved.activeSession)
    val created = createDiscussion(
        CreateDiscussionUseCase.Params(
            sessionId = activeSession.id,
            title = title.trim(),
            location = location.trim(),
            date = dateTime.toKotlinLocalDateTime(),
        ),
        role,
    ).getOrThrowAppFunction()
    return DiscussionContext(resolved.club, activeSession, created).toSummary()
}
