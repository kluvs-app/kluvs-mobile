package com.ivangarzab.kluvs.settings.presentation

import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModelHelper
import org.koin.core.component.inject

/**
 * iOS bridge for [SettingsViewModel].
 *
 * Wraps the shared ViewModel so SwiftUI can observe state and invoke actions.
 */
@Suppress("unused")
class SettingsViewModelHelper : BaseViewModelHelper<SettingsState>() {

    override val viewModel: SettingsViewModel by inject()

    fun loadProfile(userId: String) = viewModel.loadProfile(userId)

    fun onNameChanged(name: String) = viewModel.onNameChanged(name)

    fun onHandleChanged(handle: String) = viewModel.onHandleChanged(handle)

    fun onSaveProfile() = viewModel.onSaveProfile()

    fun uploadAvatar(imageData: ByteArray) = viewModel.uploadAvatar(imageData)

    fun onAvatarPickFailed(reason: String?) = viewModel.onAvatarPickFailed(reason)

    fun onOpenSheet(sheet: SettingsSheet) = viewModel.onOpenSheet(sheet)

    fun onDismissSheet() = viewModel.onDismissSheet()

    fun onOpenDialog(dialog: SettingsDialog) = viewModel.onOpenDialog(dialog)

    fun onDismissDialog() = viewModel.onDismissDialog()

    fun onNewPasswordFieldChanged(value: String) = viewModel.onNewPasswordFieldChanged(value)

    fun onConfirmPasswordFieldChanged(value: String) = viewModel.onConfirmPasswordFieldChanged(value)

    fun onSubmitChangePassword() = viewModel.onSubmitChangePassword()

    fun onDeleteAccountConfirmed() = viewModel.onDeleteAccountConfirmed()
}
