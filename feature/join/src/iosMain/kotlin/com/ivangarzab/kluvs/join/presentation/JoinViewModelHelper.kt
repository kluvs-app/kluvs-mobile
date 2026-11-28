package com.ivangarzab.kluvs.join.presentation

import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModelHelper
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [JoinViewModel] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class JoinViewModelHelper : BaseViewModelHelper<JoinState>() {

    override val viewModel: JoinViewModel by inject()

    fun onTokenChanged(token: String) = viewModel.onTokenChanged(token)
    fun previewInvite() = viewModel.previewInvite()
    fun onJoinClicked() = viewModel.onJoinClicked()
    fun onConsumeNeedsSignIn() = viewModel.onConsumeNeedsSignIn()
    fun onConsumeJoinedClubId() = viewModel.onConsumeJoinedClubId()
    fun onConsumeJoinError() = viewModel.onConsumeJoinError()
}
