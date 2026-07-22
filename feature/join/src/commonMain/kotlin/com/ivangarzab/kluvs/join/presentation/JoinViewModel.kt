package com.ivangarzab.kluvs.join.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.join.domain.JoinClubUseCase
import com.ivangarzab.kluvs.join.domain.PreviewInviteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The purpose of this [ViewModel] class is to serve the join-by-invite-token screen:
 * previewing the club behind a token and joining it.
 *
 * When [onJoinClicked] is called while signed out, this ViewModel does NOT attempt to join —
 * it only flips [JoinState.needsSignIn] so the platform UI can route through auth first. The
 * actual "resume join after sign-in" flow is owned by the app-level
 * `PendingJoinCoordinator` (in `:shared`), not this ViewModel, since it must outlive this
 * screen across an auth-state transition.
 */
class JoinViewModel(
    private val previewInvite: PreviewInviteUseCase,
    private val joinClub: JoinClubUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(JoinState())
    val state: StateFlow<JoinState> = _state.asStateFlow()

    init {
        _state.update { it.copy(isAuthenticated = authRepository.isAuthenticated.value) }
        viewModelScope.launch {
            authRepository.isAuthenticated.collect { isAuthenticated ->
                _state.update { it.copy(isAuthenticated = isAuthenticated) }
            }
        }
    }

    fun onTokenChanged(token: String) {
        _state.update { it.copy(tokenInput = token, preview = null, previewError = null) }
    }

    fun previewInvite() {
        val token = _state.value.tokenInput.trim()
        if (token.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingPreview = true, previewError = null) }
            previewInvite(token)
                .onSuccess { preview ->
                    Bark.i("Loaded invite preview (club ID: ${preview.id})")
                    _state.update { it.copy(isLoadingPreview = false, preview = preview) }
                }
                .onFailure { error ->
                    Bark.e("Failed to load invite preview", error)
                    _state.update {
                        it.copy(
                            isLoadingPreview = false,
                            preview = null,
                            previewError = "This invite link is invalid or has expired."
                        )
                    }
                }
        }
    }

    fun onJoinClicked() {
        val token = _state.value.tokenInput.trim()
        if (token.isEmpty()) return

        if (!_state.value.isAuthenticated) {
            Bark.d("Join requested while signed out — deferring to sign-in flow")
            _state.update { it.copy(needsSignIn = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isJoining = true, joinError = null) }
            joinClub(token)
                .onSuccess { clubId ->
                    Bark.i("Joined club (ID: $clubId)")
                    _state.update { it.copy(isJoining = false, joinedClubId = clubId) }
                }
                .onFailure { error ->
                    Bark.e("Failed to join club", error)
                    _state.update {
                        it.copy(isJoining = false, joinError = error.message ?: "Failed to join club")
                    }
                }
        }
    }

    fun onConsumeNeedsSignIn() {
        _state.update { it.copy(needsSignIn = false) }
    }

    fun onConsumeJoinedClubId() {
        _state.update { it.copy(joinedClubId = null) }
    }

    fun onConsumeJoinError() {
        _state.update { it.copy(joinError = null) }
    }
}
