package com.ivangarzab.kluvs.member.presentation

import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModelHelper
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [MeViewModelHelper] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class MeViewModelHelper : BaseViewModelHelper<MeState>() {

    override val viewModel: MeViewModel by inject()

    fun loadUserData(userId: String) = viewModel.loadUserData(userId)

    fun refresh(forceRefresh: Boolean = false) = viewModel.refresh(forceRefresh)

    fun confirmLogout() = viewModel.onSignOutDialogConfirmed()

    /** [percentComplete] is a plain Int (0-100) — converted to Float internally, mirroring ClubDetailsViewModelHelper. */
    fun onSaveProgress(sessionId: String, type: ProgressType, currentPage: Int?, percentComplete: Int?, markFinished: Boolean) =
        viewModel.onSaveProgress(sessionId, type, currentPage, percentComplete?.toFloat(), markFinished)

    fun onOpenSheet(sheet: MeSheet) = viewModel.onOpenSheet(sheet)

    fun onDismissSheet() = viewModel.onDismissSheet()

    fun onOpenDialog(dialog: MeDialog) = viewModel.onOpenDialog(dialog)

    fun onDismissDialog() = viewModel.onDismissDialog()
}