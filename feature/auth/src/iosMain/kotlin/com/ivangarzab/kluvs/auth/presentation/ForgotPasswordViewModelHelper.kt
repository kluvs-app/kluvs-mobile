package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModelHelper
import org.koin.core.component.inject

/** iOS bridge to [ForgotPasswordViewModel]. */
@Suppress("unused")
class ForgotPasswordViewModelHelper : BaseViewModelHelper<ForgotPasswordState>() {

    override val viewModel: ForgotPasswordViewModel by inject()

    fun onEmailFieldChanged(value: String) = viewModel.onEmailFieldChanged(value)

    fun onSendResetLink() = viewModel.onSendResetLink()
}
