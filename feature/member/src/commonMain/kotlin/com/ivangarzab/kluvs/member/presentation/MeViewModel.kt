package com.ivangarzab.kluvs.member.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.SignOutUseCase
import com.ivangarzab.kluvs.member.domain.GetCurrentUserProfileUseCase
import com.ivangarzab.kluvs.member.domain.GetOnYourShelfUseCase
import com.ivangarzab.kluvs.member.domain.GetReadingLogUseCase
import com.ivangarzab.kluvs.member.domain.GetUserStatisticsUseCase
import com.ivangarzab.kluvs.member.domain.UpdateAvatarUseCase
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.progress.SaveProgressUseCase
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
    private val updateAvatarUseCase: UpdateAvatarUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MeState())
    val state: StateFlow<MeState> = _state.asStateFlow()

    private var currentUserId: String? = null

    fun loadUserData(userId: String) {
        currentUserId = userId

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Launch all 3 UseCase calls in parallel
            val deferredProfile = async { getCurrentUserProfile(userId) }
            val deferredStats = async { getUserStatistics(userId) }
            val deferredShelf = async { getOnYourShelf(userId) }

            // Await all results
            val profileResult = deferredProfile.await()
            val statsResult = deferredStats.await()
            val shelfResult = deferredShelf.await()

            // Aggregate errors
            val errors = listOfNotNull(
                profileResult.exceptionOrNull()?.message,
                statsResult.exceptionOrNull()?.message,
                shelfResult.exceptionOrNull()?.message
            )
            val error = when {
                errors.isEmpty() -> null
                errors.distinct().size == 1 -> errors.first() // All errors are identical
                else -> "Multiple errors occurred"
            }
            error?.let { e ->
                Bark.e("Failed to fetch member details (ID: $userId). Serving cached data if available.", Exception(e))
            } ?: Bark.i("Successfully loaded member details (ID: $userId)")

            // Update state with all results
            val onYourShelf = shelfResult.getOrNull()
            _state.update {
                it.copy(
                    isLoading = false,
                    error = error,
                    profile = profileResult.getOrNull(),
                    statistics = statsResult.getOrNull(),
                    shelf = onYourShelf?.shelf ?: emptyList(),
                    upNext = onYourShelf?.upNext
                )
            }
        }
    }

    fun refresh() {
        Bark.d("Refreshing member data")
        currentUserId?.let { loadUserData(it) }
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

    fun uploadAvatar(imageData: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isUploadingAvatar = true, snackbarError = null) }

            val memberId = _state.value.profile?.memberId
            if (memberId == null) {
                Bark.e("No member ID available to update avatar. Please retry.", null)
                _state.update {
                    it.copy(
                        isUploadingAvatar = false,
                        snackbarError = "No member ID available"
                    )
                }
                return@launch
            }

            updateAvatarUseCase(memberId, imageData)
                .onSuccess { newAvatarUrl ->
                    Bark.i("Avatar uploaded successfully (ID: $memberId)")
                    _state.update {
                        it.copy(profile = it.profile?.copy(avatarUrl = newAvatarUrl))
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to upload avatar (ID: $memberId). Please retry.", error)
                    _state.update { it.copy(snackbarError = error.message) }
                }

            _state.update { it.copy(isUploadingAvatar = false) }
        }
    }

    fun clearAvatarError() {
        _state.update { it.copy(snackbarError = null) }
    }
}
