package com.ivangarzab.kluvs.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.domain.CheckUpdateRequiredUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App-level coordinator that manages navigation state based on app-wide conditions.
 *
 * Handles:
 * - Force updates: the installed app is older than the backend supports
 * - Authentication state (logged in/out)
 * - Future: Onboarding, etc.
 *
 * Platform code observes [navigationState] to perform actual navigation.
 */
class AppCoordinator(
    private val authRepository: AuthRepository,
    private val checkUpdateRequired: CheckUpdateRequiredUseCase
) : ViewModel() {

    private val authState = MutableStateFlow<NavigationState>(NavigationState.Initializing)
    private val updateRequired = MutableStateFlow(false)

    /** [NavigationState.UpdateRequired] takes priority over every auth state. */
    val navigationState: StateFlow<NavigationState> =
        combine(authState, updateRequired) { auth, mustUpdate ->
            if (mustUpdate) NavigationState.UpdateRequired else auth
        }.stateIn(viewModelScope, SharingStarted.Eagerly, NavigationState.Initializing)

    init {
        Bark.d("Initializing app coordinator")
        // Runs alongside auth restore so a slow or failed check never delays launch
        viewModelScope.launch {
            if (checkUpdateRequired()) {
                Bark.i("Navigation: Update required")
                updateRequired.value = true
            }
        }
        viewModelScope.launch {
            // Initialize auth (restore session if exists)
            authRepository.initialize()

            // Observe auth state changes
            authRepository.currentUser.collect { user ->
                authState.value = if (user != null) {
                    Bark.i("Navigation: User authenticated (ID: ${user.id})")
                    NavigationState.Authenticated(userId = user.id)
                } else {
                    Bark.i("Navigation: User unauthenticated")
                    NavigationState.Unauthenticated
                }
            }
        }
    }
}

/**
 * Represents app-level navigation state.
 * Platform code maps these states to actual navigation actions.
 */
sealed class NavigationState {
    /**
     * App is initializing (checking auth, loading config, etc.)
     * Platform should show splash screen or loading state.
     */
    data object Initializing : NavigationState()

    /**
     * User is not authenticated.
     * Platform should navigate to auth flow (login/signup).
     */
    data object Unauthenticated : NavigationState()

    /**
     * User is authenticated with the given userId.
     * Platform should navigate to main app.
     */
    data class Authenticated(val userId: String) : NavigationState()

    /**
     * The installed app is older than the backend's minimum supported version.
     * Platform should show the force-update screen and nothing else (hard block).
     */
    data object UpdateRequired : NavigationState()
}
