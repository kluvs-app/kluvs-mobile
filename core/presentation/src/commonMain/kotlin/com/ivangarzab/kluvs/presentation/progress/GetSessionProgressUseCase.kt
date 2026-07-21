package com.ivangarzab.kluvs.presentation.progress

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ProgressRepository
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress
import kotlin.math.roundToInt

/**
 * UseCase for fetching the signed-in member's reading progress on a session.
 *
 * The `progress` endpoint is member-scoped (the backend resolves the member
 * from the auth token), so this returns at most one entry for the session.
 * Transforms the domain [ReadingProgress] into a display-ready [OwnProgressInfo].
 *
 * @param progressRepository Repository for the member's reading progress
 */
class GetSessionProgressUseCase(
    private val progressRepository: ProgressRepository
) {
    /**
     * Fetches the member's progress entry for the given session.
     *
     * @param sessionId The session to look up progress for
     * @param pageCount The session book's page count, used to derive the percent for page-type progress
     * @return Result containing [OwnProgressInfo], or null if the member has no entry yet
     */
    suspend operator fun invoke(sessionId: String, pageCount: Int?): Result<OwnProgressInfo?> {
        Bark.d("Fetching own session progress (Session ID: $sessionId)")
        return progressRepository.getProgress(sessionId = sessionId).map { entries ->
            entries.firstOrNull()?.toOwnProgressInfo(pageCount).also { info ->
                if (info != null) {
                    Bark.i("Loaded own session progress (ID: ${info.progressId}, ${info.label})")
                } else {
                    Bark.d("No progress entry for session yet (Session ID: $sessionId)")
                }
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch own session progress (Session ID: $sessionId). Progress row will be empty.", error)
        }
    }
}

/**
 * Maps a domain [ReadingProgress] into the display-ready [OwnProgressInfo],
 * mirroring the web app's ProgressRow derivation of percent and label.
 */
internal fun ReadingProgress.toOwnProgressInfo(pageCount: Int?): OwnProgressInfo {
    val percent = when {
        type == ProgressType.PAGE && pageCount != null && pageCount > 0 ->
            minOf(100, ((currentPage ?: 0) * 100f / pageCount).roundToInt())
        type == ProgressType.PERCENT -> minOf(100, (percentComplete ?: 0f).roundToInt())
        else -> 0
    }
    val label = when {
        status == ProgressStatus.COMPLETED -> "Finished"
        type == ProgressType.PAGE && pageCount != null -> "${currentPage ?: 0} of $pageCount pages"
        else -> "${(percentComplete ?: 0f).roundToInt()}% complete"
    }
    return OwnProgressInfo(
        progressId = id,
        type = type,
        currentPage = currentPage,
        percentComplete = percentComplete,
        isCompleted = status == ProgressStatus.COMPLETED,
        percent = percent,
        label = label
    )
}
