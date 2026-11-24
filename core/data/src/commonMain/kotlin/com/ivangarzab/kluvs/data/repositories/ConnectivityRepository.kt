package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.network.status.OfflineStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository exposing whether the app can currently reach the network.
 *
 * The signal comes from real requests (any response means reachable, a connection failure or
 * timeout means not), so it flips on the first request that fails and flips back on the next that
 * succeeds. It is the app's cue that what the user sees may be saved data.
 */
interface ConnectivityRepository {

    /** `true` while the network is unreachable. Starts `false` until a request says otherwise. */
    val isOffline: StateFlow<Boolean>
}

internal class ConnectivityRepositoryImpl(
    offlineStatus: OfflineStatus
) : ConnectivityRepository {

    override val isOffline: StateFlow<Boolean> = offlineStatus.isOffline
}
