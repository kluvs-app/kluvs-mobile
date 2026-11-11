package com.ivangarzab.kluvs.network.status

import com.ivangarzab.bark.Bark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether the app can currently reach the network, as observed from real requests.
 *
 * It is fed by [OfflineStatusPlugin], which sits on the Supabase client's HTTP client: any HTTP
 * response (even a 4xx or 5xx) means the network is reachable, while a request that fails before
 * getting a response (no connection, DNS, timeout) means it is not. Only the data layer reads
 * this; the UI reaches it through a repository so it can tell the user that what they see may be
 * saved data.
 *
 * Starts online: nothing is known until the first request has been attempted.
 */
class OfflineStatus {

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    internal fun markOffline() {
        if (_isOffline.compareAndSet(expect = false, update = true)) {
            Bark.i("Network: Unreachable; app is now offline")
        }
    }

    internal fun markOnline() {
        if (_isOffline.compareAndSet(expect = true, update = false)) {
            Bark.i("Network: Reachable again; app is back online")
        }
    }
}
