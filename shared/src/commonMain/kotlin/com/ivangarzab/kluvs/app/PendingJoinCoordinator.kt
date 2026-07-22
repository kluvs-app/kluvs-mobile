package com.ivangarzab.kluvs.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.JoinRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Coordinates the "join after sign-in" flow: a signed-out user previews an invite,
 * taps Join, gets routed through auth, and should land back in the club once
 * authenticated — without the Join screen itself surviving the auth-state
 * transition (both platforms wipe navigation state on every auth change).
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
                            _autoJoinResult.emit(AutoJoinResult.Failure(error.message))
                        }
                }
            }
        }
    }

    /** Stashes [token] to be auto-joined the next time the user becomes authenticated. */
    fun setPendingToken(token: String) {
        _pendingToken.value = token
    }
}

/** Result of an auto-join attempt performed after a sign-in that had a pending invite token. */
sealed interface AutoJoinResult {
    data class Success(val clubId: String) : AutoJoinResult
    data class Failure(val message: String?) : AutoJoinResult
}
