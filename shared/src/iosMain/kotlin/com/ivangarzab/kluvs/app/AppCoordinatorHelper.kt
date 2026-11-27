package com.ivangarzab.kluvs.app

import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [AppCoordinator] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class AppCoordinatorHelper : KoinComponent {

    private val appCoordinator: AppCoordinator by inject()
    private val coroutineScope: CoroutineScope by inject()

    /**
     * iOS-friendly observation method for navigation state.
     *
     * Returns a [com.ivangarzab.kluvs.presentation.Closeable] that can be used to cancel the observation.
     */
    fun observeNavigationState(callback: (NavigationState) -> Unit): Closeable {
        val job = appCoordinator.navigationState.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    fun onUpdateApp() = appCoordinator.onUpdateApp()
}
