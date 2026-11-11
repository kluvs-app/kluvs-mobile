package com.ivangarzab.kluvs.member.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.SignOutUseCase
import com.ivangarzab.kluvs.data.error.toAppError
import com.ivangarzab.kluvs.presentation.error.toUserMessage
import com.ivangarzab.kluvs.member.domain.GetCurrentUserProfileUseCase
import com.ivangarzab.kluvs.member.domain.GetOnYourShelfUseCase
import com.ivangarzab.kluvs.member.domain.GetReadingLogUseCase
import com.ivangarzab.kluvs.member.domain.GetUserStatisticsUseCase
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.progress.SaveProgressUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The purpose of this [ViewModel] class is to serve the Me screen.
 */
class MeViewModel(
    private val getCurrentUserProfile: GetCurrentUserProfileUseCase,
    private val getUserStatistics: GetUserStatisticsUseCase,
    private val getOnYourShelf: GetOnYourShelfUseCase,
    private val getReadingLog: GetReadingLogUseCase,
    private val saveProgressUseCase: SaveProgressUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MeState())
    val state: StateFlow<MeState> = _state.asStateFlow()

    private var currentUserId: String? = null
    private var loadUserDataJob: Job? = null

    fun loadUserData(userId: String, forceRefresh: Boolean = false) {
        currentUserId = userId

        // Cancel any in-flight load so overlapping calls (tab re-entry + pull-to-refresh)
        // can't finish out of order and let an older, staler result land last.
        loadUserDataJob?.cancel()
        loadUserDataJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

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
                profileResult.exceptionOrNull()?.toAppError()?.toUserMessage(),
                statsResult.exceptionOrNull()?.toAppError()?.toUserMessage(),
                shelfResult.exceptionOrNull()?.toAppError()?.toUserMessage()
            )
            val error = when {
                errors.isEmpty() -> null
                errors.distinct().size == 1 -> errors.first() // All errors are identical
                else -> "Multiple things went wrong. Please try again."
            }
            // A profile that's already on screen is never replaced by an error page: a failed
            // (re)load keeps the existing content and reports the failure through the snackbar.
            val hadContent = _state.value.profile != null
            error?.let { e ->
                Bark.e("Failed to fetch member details (ID: $userId). ${if (hadContent) "Keeping what's on screen." else "Showing error."}", Exception(e))
            } ?: Bark.i("Successfully loaded member details (ID: $userId)")

            // Update state with all results. When keeping content, each piece only changes if
            // its fetch succeeded.
            val onYourShelf = shelfResult.getOrNull()
            _state.update {
                it.copy(
                    isLoading = false,
                    error = if (hadContent) null else error,
                    snackbarError = if (hadContent && error != null) error else it.snackbarError,
                    profile = profileResult.getOrNull() ?: if (hadContent) it.profile else null,
                    statistics = statsResult.getOrNull() ?: if (hadContent) it.statistics else null,
                    shelf = onYourShelf?.shelf ?: if (hadContent) it.shelf else emptyList(),
                    upNext = if (onYourShelf != null) onYourShelf.upNext else if (hadContent) it.upNext else null
                )
            }
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
        val item = _state.value.shelf.find { it.sessionId == sessionId } ?: return
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
                    _state.update { s ->
                        s.copy(shelf = s.shelf.map { if (it.sessionId == sessionId) it.copy(ownProgress = updated) else it })
                    }
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Progress update (Session ID: $sessionId). ${error.message}", error)
                    _state.update { it.copy(snackbarError = error.message) }
                }
        }
    }

    fun onReadingLogClicked() {
        _state.update { it.copy(showReadingLog = true) }
        if (_state.value.readingLog != null) return

        viewModelScope.launch {
            _state.update { it.copy(isReadingLogLoading = true) }
            getReadingLog()
                .onSuccess { log -> _state.update { it.copy(readingLog = log, isReadingLogLoading = false) } }
                .onFailure { error ->
                    Bark.e("Failed to load reading log. ${error.message}", error)
                    _state.update { it.copy(isReadingLogLoading = false, snackbarError = error.message) }
                }
        }
    }

    fun onReadingLogDismissed() {
        _state.update { it.copy(showReadingLog = false) }
    }

    fun onSignOutClicked() {
        _state.update { it.copy(showLogoutConfirmation = true) }
    }

    fun onSignOutDialogDismissed() {
        _state.update { it.copy(showLogoutConfirmation = false) }
    }

    fun onSignOutDialogConfirmed() = viewModelScope.launch {
        Bark.i("Signing out user")
        _state.update { it.copy(showLogoutConfirmation = false) }
        signOutUseCase()
    }

    fun clearSnackbarError() {
        _state.update { it.copy(snackbarError = null) }
    }
}
