package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.clubs.presentation.OwnProgressInfo
import com.ivangarzab.kluvs.data.repositories.ProgressRepository
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType

/**
 * UseCase for saving the signed-in member's reading progress — an upsert:
 * updates the existing entry when [Params.progressId] is present, otherwise
 * creates a new one tied to the session.
 *
 * Mirrors the web app's ReadingProgressModal semantics, including the
 * "mark as finished" toggle mapping to [ProgressStatus.COMPLETED].
 *
 * @param progressRepository Repository for the member's reading progress
 */
class SaveProgressUseCase(
    private val progressRepository: ProgressRepository
) {
    data class Params(
        /** Existing progress entry to update; null creates a new entry. */
        val progressId: String?,
        /** The session book's ID — required when creating a new entry. */
        val bookId: String,
        /** Session to tie a newly created entry to. */
        val sessionId: String,
        /** The session book's page count, used to derive display values. */
        val pageCount: Int?,
        val type: ProgressType,
        val currentPage: Int?,
        val percentComplete: Float?,
        val markFinished: Boolean
    )

    /**
     * Saves the progress entry and returns the refreshed display model.
     *
     * @return Result containing the updated [OwnProgressInfo] if successful
     */
    suspend operator fun invoke(params: Params): Result<OwnProgressInfo> {
        val status = if (params.markFinished) ProgressStatus.COMPLETED else ProgressStatus.IN_PROGRESS

        val result = if (params.progressId != null) {
            Bark.d("Updating own progress (ID: ${params.progressId})")
            progressRepository.updateProgress(
                progressId = params.progressId,
                type = params.type,
                currentPage = params.currentPage,
                percentComplete = params.percentComplete,
                status = status
            )
        } else {
            Bark.d("Creating own progress (Book ID: ${params.bookId}, Session ID: ${params.sessionId})")
            progressRepository.createProgress(
                bookId = params.bookId,
                type = params.type,
                currentPage = params.currentPage,
                percentComplete = params.percentComplete,
                sessionId = params.sessionId
            ).let { created ->
                // The create endpoint cannot set a status, so a finished-on-create
                // needs an immediate status update on the new entry.
                if (params.markFinished) {
                    created.getOrNull()?.let { progress ->
                        progressRepository.updateProgress(
                            progressId = progress.id,
                            type = params.type,
                            currentPage = params.currentPage,
                            percentComplete = params.percentComplete,
                            status = ProgressStatus.COMPLETED
                        )
                    } ?: created
                } else {
                    created
                }
            }
        }

        return result.map { it.toOwnProgressInfo(params.pageCount) }
            .onSuccess { Bark.i("Progress saved (ID: ${it.progressId}, ${it.label})") }
            .onFailure { Bark.e("Failed to save progress (Session ID: ${params.sessionId}). Retry.", it) }
    }
}
