package com.ivangarzab.kluvs.network.status

import com.ivangarzab.bark.Bark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
 *
 * Requests alone can only notice recovery when something else makes one, so while the app is
 * offline *and* something is observing [isOffline], a [ConnectivityCheck] (the [ConnectivityProbe]) re-checks in the
 * background: first after [initialDelay], doubling up to [maxDelay], and stopping as soon as the
 * internet answers or nobody is observing. A platform network listener can later replace the
 * timer as the thing that triggers that check.
 */
class OfflineStatus internal constructor(
    private val probe: ConnectivityCheck?,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val initialDelay: Duration = INITIAL_PROBE_DELAY,
    private val maxDelay: Duration = MAX_PROBE_DELAY,
) {

    /** Reactive only (no background recovery check). Used where no probe is wanted. */
    constructor() : this(probe = null)

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    init {
        if (probe != null) {
            scope.launch {
                combine(_isOffline, _isOffline.subscriptionCount) { offline, subscribers ->
                    offline && subscribers > OWN_SUBSCRIPTIONS
                }
                    .distinctUntilChanged()
                    .collectLatest { shouldProbe -> if (shouldProbe) probeUntilReachable(probe) }
            }
        }
    }

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

    private suspend fun probeUntilReachable(probe: ConnectivityCheck) {
        var wait = initialDelay
        while (true) {
            delay(wait)
            if (probe.isReachable()) {
                markOnline()
                return
            }
            wait = (wait * 2).coerceAtMost(maxDelay)
        }
    }

    private companion object {
        /** This loop collects [_isOffline] itself, so it counts as one subscriber it must ignore. */
        const val OWN_SUBSCRIPTIONS = 1
        val INITIAL_PROBE_DELAY = 5.seconds
        val MAX_PROBE_DELAY = 30.seconds
    }
}
