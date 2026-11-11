package com.ivangarzab.kluvs.app

import androidx.lifecycle.ViewModel
import com.ivangarzab.kluvs.data.repositories.ConnectivityRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * App-level coordinator for network connectivity, as the shell should present it.
 *
 * Kept apart from [AppCoordinator], which only turns auth state into navigation. This one only
 * forwards what [ConnectivityRepository] knows: how the signal is produced and combined (request
 * outcomes today, a platform network listener later) is the repository's concern, not this
 * class's. A [ViewModel] like the other coordinators, so it can own a scope if the UI-facing
 * shape ever needs one (e.g. a debounced "back online" state).
 *
 * Platform code observes [isOffline] to show or hide the offline banner.
 */
class ConnectivityCoordinator(
    connectivityRepository: ConnectivityRepository
) : ViewModel() {

    /** `true` while the network is unreachable, so the UI can tell the user they see saved data. */
    val isOffline: StateFlow<Boolean> = connectivityRepository.isOffline
}
