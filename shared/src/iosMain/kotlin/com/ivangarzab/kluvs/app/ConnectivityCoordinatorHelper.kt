package com.ivangarzab.kluvs.app

import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [ConnectivityCoordinator] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class ConnectivityCoordinatorHelper : KoinComponent {

    private val coordinator: ConnectivityCoordinator by inject()
    private val coroutineScope: CoroutineScope by inject()

    /**
     * iOS-friendly observation method for the offline state: `true` while the network is
     * unreachable. The current value is delivered immediately.
     *
     * Returns a [Closeable] that can be used to cancel the observation.
     */
    fun observeIsOffline(callback: (Boolean) -> Unit): Closeable {
        val job = coordinator.isOffline.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }
}
