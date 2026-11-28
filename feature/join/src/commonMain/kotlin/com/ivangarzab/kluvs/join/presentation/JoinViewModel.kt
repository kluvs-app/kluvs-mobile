package com.ivangarzab.kluvs.join.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.join.domain.JoinClubUseCase
import com.ivangarzab.kluvs.join.domain.ObserveIsAuthenticatedUseCase
import com.ivangarzab.kluvs.join.domain.PreviewInviteUseCase
import com.ivangarzab.kluvs.presentation.error.toDisplayMessage
import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

/**
 * The purpose of this ViewModel is to serve the join-by-invite-token screen:
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
    private val observeIsAuthenticated: ObserveIsAuthenticatedUseCase
) : BaseViewModel<JoinState>(JoinState()) {

    init {
        val isAuthenticatedFlow = observeIsAuthenticated()
        updateState { it.copy(isAuthenticated = isAuthenticatedFlow.value) }
        viewModelScope.launch {
            isAuthenticatedFlow.collect { isAuthenticated ->
                updateState { it.copy(isAuthenticated = isAuthenticated) }
            }
        }
    }

    fun onTokenChanged(token: String) {
        updateState { it.copy(tokenInput = token, preview = null, previewError = null) }
    }

    fun previewInvite() {
        val token = currentState.tokenInput.trim()
        if (token.isEmpty()) return

        viewModelScope.launch {
            updateState { it.copy(isLoadingPreview = true, previewError = null) }
            previewInvite(token)
                .onSuccess { preview ->
                    Bark.i("Loaded invite preview (club ID: ${preview.id})")
                    updateState { it.copy(isLoadingPreview = false, preview = preview) }
                }
                .onFailure { error ->
                    Bark.e("Failed to load invite preview", error)
                    updateState {
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
        val token = currentState.tokenInput.trim()
        if (token.isEmpty()) return

        if (!currentState.isAuthenticated) {
            Bark.d("Join requested while signed out — deferring to sign-in flow")
            updateState { it.copy(needsSignIn = true) }
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isJoining = true, joinError = null) }
            joinClub(token)
                .onSuccess { clubId ->
                    Bark.i("Joined club (ID: $clubId)")
                    updateState { it.copy(isJoining = false, joinedClubId = clubId) }
                }
                .onFailure { error ->
                    Bark.e("Failed to join club", error)
                    updateState {
                        it.copy(isJoining = false, joinError = error.toDisplayMessage())
                    }
                }
        }
    }

    fun onConsumeNeedsSignIn() {
        updateState { it.copy(needsSignIn = false) }
    }

    fun onConsumeJoinedClubId() {
        updateState { it.copy(joinedClubId = null) }
    }

    fun onConsumeJoinError() {
        updateState { it.copy(joinError = null) }
    }
}
