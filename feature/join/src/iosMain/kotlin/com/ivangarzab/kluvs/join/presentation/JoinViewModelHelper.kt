package com.ivangarzab.kluvs.join.presentation

import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [JoinViewModel] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class JoinViewModelHelper : KoinComponent {

    private val viewModel: JoinViewModel by inject()
    private val coroutineScope: CoroutineScope by inject()

    /**
     * iOS-friendly observation method.
     *
     * Returns a [Closeable] that can be used to cancel the observation.
     */
    fun observeState(callback: (JoinState) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    fun onTokenChanged(token: String) = viewModel.onTokenChanged(token)
    fun previewInvite() = viewModel.previewInvite()
    fun onJoinClicked() = viewModel.onJoinClicked()
    fun onConsumeNeedsSignIn() = viewModel.onConsumeNeedsSignIn()
    fun onConsumeJoinedClubId() = viewModel.onConsumeJoinedClubId()
    fun onConsumeJoinError() = viewModel.onConsumeJoinError()
}
