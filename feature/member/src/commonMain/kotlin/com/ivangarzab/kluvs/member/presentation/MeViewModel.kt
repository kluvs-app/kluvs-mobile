package com.ivangarzab.kluvs.member.presentation

import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.SignOutUseCase
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import com.ivangarzab.kluvs.presentation.error.toDisplayMessage
import com.ivangarzab.kluvs.member.domain.GetCurrentUserProfileUseCase
import com.ivangarzab.kluvs.member.domain.GetOnYourShelfUseCase
import com.ivangarzab.kluvs.member.domain.GetReadingLogUseCase
import com.ivangarzab.kluvs.member.domain.GetUserStatisticsUseCase
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.progress.SaveProgressUseCase
import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

/**
 * The purpose of this ViewModel is to serve the Me screen.
 */
class MeViewModel(
    private val getCurrentUserProfile: GetCurrentUserProfileUseCase,
    private val getUserStatistics: GetUserStatisticsUseCase,
    private val getOnYourShelf: GetOnYourShelfUseCase,
    private val getReadingLog: GetReadingLogUseCase,
    private val saveProgressUseCase: SaveProgressUseCase,
    private val signOutUseCase: SignOutUseCase,
) : BaseViewModel<MeState>(MeState()) {

    private var currentUserId: String? = null
    private var loadUserDataJob: Job? = null

    fun loadUserData(userId: String, forceRefresh: Boolean = false) {
        currentUserId = userId

        // Cancel any in-flight load so overlapping calls (tab re-entry + pull-to-refresh)
        // can't finish out of order and let an older, staler result land last.
        loadUserDataJob?.cancel()
        loadUserDataJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            val loadStart = TimeSource.Monotonic.markNow()

            // Launch all 3 UseCase calls in parallel
            val deferredProfile = async { getCurrentUserProfile(userId, forceRefresh) }
            val deferredStats = async { getUserStatistics(userId, forceRefresh) }
            val deferredShelf = async { getOnYourShelf(userId, forceRefresh) }

            // Await all results
            val profileResult = deferredProfile.await()
            val statsResult = deferredStats.await()
            val shelfResult = deferredShelf.await()

            // Aggregate errors
            val errors = listOfNotNull(
                profileResult.exceptionOrNull()?.toDisplayMessage(),
                statsResult.exceptionOrNull()?.toDisplayMessage(),
                shelfResult.exceptionOrNull()?.toDisplayMessage()
            )
            val error = when {
                errors.isEmpty() -> null
                errors.distinct().size == 1 -> errors.first() // All errors are identical
                else -> "Multiple things went wrong. Please try again."
            }
            // A profile that's already on screen is never replaced by an error page: a failed
            // (re)load keeps the existing content and reports the failure through the snackbar.
            val hadContent = currentState.profile != null
            error?.let { e ->
                Bark.e("Failed to fetch member details (ID: $userId). ${if (hadContent) "Keeping what's on screen." else "Showing error."}", Exception(e))
            } ?: Bark.i("Successfully loaded member details (ID: $userId)")
            Bark.d("Me load took ${loadStart.elapsedNow().inWholeMilliseconds} ms (ID: $userId, forceRefresh=$forceRefresh)")

            // Update state with all results. When keeping content, each piece only changes if
            // its fetch succeeded.
            val onYourShelf = shelfResult.getOrNull()
            updateState {
                it.copy(
                    isLoading = false,
                    error = if (hadContent) null else error,
                    profile = profileResult.getOrNull() ?: if (hadContent) it.profile else null,
                    statistics = statsResult.getOrNull() ?: if (hadContent) it.statistics else null,
                    shelf = onYourShelf?.shelf ?: if (hadContent) it.shelf else emptyList(),
                    upNext = if (onYourShelf != null) onYourShelf.upNext else if (hadContent) it.upNext else null
                )
            }
            if (hadContent && error != null) send(ScreenEffect.ShowSnackbar.error(error))
        }
    }

    fun refresh(forceRefresh: Boolean = false) {
        Bark.d("Refreshing member data (forceRefresh=$forceRefresh)")
        currentUserId?.let { loadUserData(it, forceRefresh) }
    }

    /**
     * Saves the signed-in member's reading progress on the given session's shelf item.
     *
     * Unlike a full refresh, only the affected shelf row is patched in state.
     */
    fun onSaveProgress(
        sessionId: String,
        type: ProgressType,
        currentPage: Int?,
        percentComplete: Float?,
        markFinished: Boolean
    ) {
        val item = currentState.shelf.find { it.sessionId == sessionId } ?: return
        viewModelScope.launch {
            saveProgressUseCase(
                SaveProgressUseCase.Params(
                    progressId = item.ownProgress?.progressId,
                    bookId = item.bookId,
                    sessionId = item.sessionId,
                    pageCount = item.bookPageCount,
                    type = type,
                    currentPage = currentPage,
                    percentComplete = percentComplete,
                    markFinished = markFinished
                )
            )
                .onSuccess { updated ->
                    updateState { s ->
                        s.copy(shelf = s.shelf.map { if (it.sessionId == sessionId) it.copy(ownProgress = updated) else it })
                    }
                    closeSheet(ifShowing = MeSheet.UpdateProgress(sessionId))
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Progress update (Session ID: $sessionId). ${error.message}", error)
                    send(ScreenEffect.ShowSnackbar.error(error.toDisplayMessage()))
                }
        }
    }

    /** Opens [sheet], replacing any sheet already open. The reading log loads the first time it opens. */
    fun onOpenSheet(sheet: MeSheet) {
        updateState { it.copy(activeSheet = sheet) }
        if (sheet == MeSheet.ReadingLog) loadReadingLog()
    }

    /** Closes the open sheet: Cancel, the close button, swipe down, back, or a tap outside. */
    fun onDismissSheet() {
        updateState { it.copy(activeSheet = null) }
    }

    /** Opens [dialog] on top of the screen. */
    fun onOpenDialog(dialog: MeDialog) {
        updateState { it.copy(activeDialog = dialog) }
    }

    /** Closes the open dialog. Confirming a dialog closes it too. */
    fun onDismissDialog() {
        updateState { it.copy(activeDialog = null) }
    }

    fun onSignOutDialogConfirmed() = viewModelScope.launch {
        Bark.i("Signing out user")
        onDismissDialog()
        signOutUseCase()
    }

    private fun loadReadingLog() {
        if (currentState.readingLog != null) return
        viewModelScope.launch {
            updateState { it.copy(isReadingLogLoading = true) }
            getReadingLog()
                .onSuccess { log -> updateState { it.copy(readingLog = log, isReadingLogLoading = false) } }
                .onFailure { error ->
                    Bark.e("Failed to load reading log. ${error.message}", error)
                    updateState { it.copy(isReadingLogLoading = false) }
                    send(ScreenEffect.ShowSnackbar.error(error.toDisplayMessage()))
                }
        }
    }

    /**
     * Closes the open sheet after a successful save, but only if it's still [ifShowing]. The user
     * may have dismissed it mid-save and opened another one, which must stay open.
     */
    private fun closeSheet(ifShowing: MeSheet) {
        updateState { if (it.activeSheet == ifShowing) it.copy(activeSheet = null) else it }
    }
}
