package com.ivangarzab.kluvs.app

import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [PendingJoinCoordinator] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class PendingJoinCoordinatorHelper : KoinComponent {

    private val coordinator: PendingJoinCoordinator by inject()
    private val coroutineScope: CoroutineScope by inject()

    /**
     * iOS-friendly observation method for auto-join results.
     *
     * Returns a [Closeable] that can be used to cancel the observation.
     */
    fun observeAutoJoinResult(callback: (AutoJoinResult) -> Unit): Closeable {
        val job = coordinator.autoJoinResult.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    fun setPendingToken(token: String) = coordinator.setPendingToken(token)
}
