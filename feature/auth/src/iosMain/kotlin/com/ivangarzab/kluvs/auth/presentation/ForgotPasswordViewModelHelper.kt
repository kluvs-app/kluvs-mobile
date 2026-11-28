package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModelHelper

/** iOS bridge to [ForgotPasswordViewModel]. */
@Suppress("unused")
class ForgotPasswordViewModelHelper : BaseViewModelHelper<ForgotPasswordState>() {

    override val viewModel: ForgotPasswordViewModel by viewModel()

    fun onEmailFieldChanged(value: String) = viewModel.onEmailFieldChanged(value)

    fun onSendResetLink() = viewModel.onSendResetLink()
}
