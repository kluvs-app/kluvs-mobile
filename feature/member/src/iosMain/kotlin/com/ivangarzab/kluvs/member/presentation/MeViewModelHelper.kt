package com.ivangarzab.kluvs.member.presentation

import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [MeViewModelHelper] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class MeViewModelHelper : KoinComponent {

    private val viewModel: MeViewModel by inject()
    private val coroutineScope: CoroutineScope by inject()

    /**
     * iOS-friendly observation method.
     *
     * Returns a [com.ivangarzab.kluvs.presentation.Closeable] that can be used to cancel the observation.
     */
    fun observeState(callback: (MeState) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    fun loadUserData(userId: String) = viewModel.loadUserData(userId)

    fun refresh() = viewModel.refresh()

    fun showLogoutConfirmation() = viewModel.onSignOutClicked()

    fun hideLogoutConfirmation() = viewModel.onSignOutDialogDismissed()

    fun confirmLogout() = viewModel.onSignOutDialogConfirmed()

    fun uploadAvatar(imageData: ByteArray) = viewModel.uploadAvatar(imageData)

    fun clearAvatarError() = viewModel.clearAvatarError()

    /** [percentComplete] is a plain Int (0-100) — converted to Float internally, mirroring ClubDetailsViewModelHelper. */
    fun onSaveProgress(sessionId: String, type: ProgressType, currentPage: Int?, percentComplete: Int?, markFinished: Boolean) =
        viewModel.onSaveProgress(sessionId, type, currentPage, percentComplete?.toFloat(), markFinished)

    fun onReadingLogClicked() = viewModel.onReadingLogClicked()

    fun onReadingLogDismissed() = viewModel.onReadingLogDismissed()
}