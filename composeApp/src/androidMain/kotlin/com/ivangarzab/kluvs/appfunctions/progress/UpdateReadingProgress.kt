package com.ivangarzab.kluvs.appfunctions.progress

import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import com.ivangarzab.kluvs.appfunctions.AppFunctionSession
import com.ivangarzab.kluvs.appfunctions.getOrThrowAppFunction
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.progress.GetSessionProgressUseCase
import com.ivangarzab.kluvs.presentation.progress.SaveProgressUseCase

/** The user's reading progress on a club's current book after the update. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ReadingProgressResult(
    /** The club's ID. */
    val clubId: String,
    /** The title of the book the club is reading. */
    val bookTitle: String,
    /** How far through the book the user is, from 0 to 100. */
    val percentRead: Int,
    /** Whether the user has finished the book. */
    val isFinished: Boolean,
    /** A short summary, such as "120 of 300 pages" or "Finished". */
    val summary: String,
)

internal suspend fun updateReadingProgress(
    session: AppFunctionSession,
    clubRepository: ClubRepository,
    getSessionProgress: GetSessionProgressUseCase,
    saveProgress: SaveProgressUseCase,
    clubId: String,
    currentPage: Int?,
    percentComplete: Float?,
    markFinished: Boolean,
): ReadingProgressResult {
    if (currentPage != null && percentComplete != null) {
        throw AppFunctionInvalidArgumentException("Give either a page number or a percentage, not both.")
    }
    if (currentPage == null && percentComplete == null && !markFinished) {
        throw AppFunctionInvalidArgumentException("Give a page number, a percentage, or mark the book as finished.")
    }
    if (percentComplete != null && percentComplete !in 0f..100f) {
        throw AppFunctionInvalidArgumentException("The percentage must be between 0 and 100.")
    }

    session.requireClubRole(clubId)
    val club = clubRepository.getClub(clubId, forceRefresh = true).getOrThrowAppFunction()
    val activeSession = club.activeSession
        ?: throw AppFunctionElementNotFoundException("${club.name} isn't reading a book right now.")
    val book = activeSession.book
    val pageCount = book.pageCount

    if (currentPage != null && (currentPage < 0 || (pageCount != null && currentPage > pageCount))) {
        throw AppFunctionInvalidArgumentException(
            if (pageCount != null) "The page must be between 0 and $pageCount." else "The page can't be negative."
        )
    }

    val (type, page, percent) = when {
        currentPage != null -> Triple(ProgressType.PAGE, currentPage, null)
        percentComplete != null -> Triple(ProgressType.PERCENT, null, percentComplete)
        pageCount != null -> Triple(ProgressType.PAGE, pageCount, null)
        else -> Triple(ProgressType.PERCENT, null, 100f)
    }

    val existing = getSessionProgress(activeSession.id, pageCount).getOrThrowAppFunction()
    val saved = saveProgress(
        SaveProgressUseCase.Params(
            progressId = existing?.progressId,
            bookId = book.id,
            sessionId = activeSession.id,
            pageCount = pageCount,
            type = type,
            currentPage = page,
            percentComplete = percent,
            markFinished = markFinished,
        )
    ).getOrThrowAppFunction()

    return ReadingProgressResult(
        clubId = clubId,
        bookTitle = book.title,
        percentRead = saved.percent,
        isFinished = saved.isCompleted,
        summary = saved.label,
    )
}
