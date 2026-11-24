package com.ivangarzab.kluvs.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.JoinRepository
import com.ivangarzab.kluvs.presentation.error.toDisplayMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Coordinates the "join after sign-in" flow: a signed-out user previews an invite,
 * taps Join, gets routed through auth, and should land back in the club once
 * authenticated — without the Join screen itself surviving the auth-state
 * transition (both platforms wipe navigation state on every auth change).
 *
 * Also the entry point for invite deep links ([onInviteLinkOpened]): the platform layer
 * hands over a raw URL, and [incomingInviteToken] tells the navigation host to open the
 * Join screen. That indirection matters because a deep link can arrive before auth has
 * resolved — both platforms wipe navigation state on every auth transition, so the token
 * has to live somewhere that survives it.
 *
 * A singleton, same lifecycle tier as [AppCoordinator], since it must outlive
 * the Join screen's own ViewModel.
 */
class PendingJoinCoordinator(
    private val appCoordinator: AppCoordinator,
    private val joinRepository: JoinRepository
) : ViewModel() {

    private val _pendingToken = MutableStateFlow<String?>(null)

    private val _autoJoinResult = MutableSharedFlow<AutoJoinResult>(extraBufferCapacity = 1)
    val autoJoinResult: SharedFlow<AutoJoinResult> = _autoJoinResult

    private val _incomingInviteToken = MutableStateFlow<String?>(null)

    /**
     * Token from an invite deep link that has not yet been routed to the Join screen.
     * Consumed via [onConsumeIncomingInviteToken] once the platform has navigated.
     */
    val incomingInviteToken: StateFlow<String?> = _incomingInviteToken.asStateFlow()

    init {
        viewModelScope.launch {
            appCoordinator.navigationState.collect { state ->
                val token = _pendingToken.value
                if (state is NavigationState.Authenticated && token != null) {
                    _pendingToken.value = null
                    Bark.d("Auto-joining club after sign-in")
                    joinRepository.joinClub(token)
                        .onSuccess { clubId ->
                            Bark.i("Auto-joined club after sign-in (club ID: $clubId)")
                            _autoJoinResult.emit(AutoJoinResult.Success(clubId))
                        }
                        .onFailure { error ->
                            Bark.e("Auto-join after sign-in failed", error)
                            _autoJoinResult.emit(AutoJoinResult.Failure(error.toDisplayMessage()))
                        }
                }
            }
        }
    }

    /** Stashes [token] to be auto-joined the next time the user becomes authenticated. */
    fun setPendingToken(token: String) {
        _pendingToken.value = token
    }

    /**
     * Handles an invite deep link opened from outside the app. Returns true when [url] was a
     * recognized invite link, so the caller can fall through to other handlers when it wasn't.
     */
    fun onInviteLinkOpened(url: String): Boolean {
        val token = InviteLink.parseToken(url) ?: return false
        Bark.d("Received invite deep link")
        _incomingInviteToken.value = token
        return true
    }

    /** Clears [incomingInviteToken] once the platform has routed to the Join screen. */
    fun onConsumeIncomingInviteToken() {
        _incomingInviteToken.value = null
    }
}

/** Result of an auto-join attempt performed after a sign-in that had a pending invite token. */
sealed interface AutoJoinResult {
    data class Success(val clubId: String) : AutoJoinResult
    data class Failure(val message: String?) : AutoJoinResult
}
