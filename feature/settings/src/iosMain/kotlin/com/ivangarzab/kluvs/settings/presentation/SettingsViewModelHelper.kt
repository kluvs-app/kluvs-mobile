package com.ivangarzab.kluvs.settings.presentation

import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS bridge for [SettingsViewModel].
 *
 * Wraps the shared ViewModel so SwiftUI can observe state and invoke actions.
 */
@Suppress("unused")
class SettingsViewModelHelper : KoinComponent {

    private val viewModel: SettingsViewModel by inject()
    private val coroutineScope: CoroutineScope by inject()

    /**
     * iOS-friendly observation method.
     *
     * Returns a [Closeable] that can be used to cancel the observation.
     */
    fun observeState(callback: (SettingsState) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    fun loadProfile(userId: String) = viewModel.loadProfile(userId)

    fun onNameChanged(name: String) = viewModel.onNameChanged(name)

    fun onHandleChanged(handle: String) = viewModel.onHandleChanged(handle)

    fun onSaveProfile() = viewModel.onSaveProfile()

    fun onDismissSaveSuccess() = viewModel.onDismissSaveSuccess()

    fun uploadAvatar(imageData: ByteArray) = viewModel.uploadAvatar(imageData)

    fun onAvatarPickFailed(reason: String?) = viewModel.onAvatarPickFailed(reason)

    fun clearAvatarError() = viewModel.clearAvatarError()

    fun onChangePasswordSheetOpened() = viewModel.onChangePasswordSheetOpened()

    fun onChangePasswordSheetDismissed() = viewModel.onChangePasswordSheetDismissed()

    fun onNewPasswordFieldChanged(value: String) = viewModel.onNewPasswordFieldChanged(value)

    fun onConfirmPasswordFieldChanged(value: String) = viewModel.onConfirmPasswordFieldChanged(value)

    fun onSubmitChangePassword() = viewModel.onSubmitChangePassword()

    fun onDismissChangePasswordSuccess() = viewModel.onDismissChangePasswordSuccess()

    fun onDeleteAccountClicked() = viewModel.onDeleteAccountClicked()

    fun onDeleteAccountDialogDismissed() = viewModel.onDeleteAccountDialogDismissed()

    fun onDeleteAccountConfirmed() = viewModel.onDeleteAccountConfirmed()

    fun clearDeleteAccountError() = viewModel.clearDeleteAccountError()
}
