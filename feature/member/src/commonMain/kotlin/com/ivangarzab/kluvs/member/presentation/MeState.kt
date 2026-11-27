package com.ivangarzab.kluvs.member.presentation

import com.ivangarzab.kluvs.model.ReadingLog

data class MeState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val profile: UserProfile? = null,
    val statistics: UserStatistics? = null,
    val shelf: List<ShelfItem> = emptyList(),
    val upNext: UpNextItem? = null,
    val snackbarError: String? = null,
    val readingLog: ReadingLog? = null,
    val isReadingLogLoading: Boolean = false,
    /** The bottom sheet on screen, if any. Opened with `onOpenSheet`, closed by `onDismissSheet` or a successful save. */
    val activeSheet: MeSheet? = null,
    /** The confirmation dialog on screen, if any. */
    val activeDialog: MeDialog? = null,
)

/** Bottom sheets on the Me screen. At most one is open at a time. */
sealed class MeSheet {
    data class UpdateProgress(val sessionId: String) : MeSheet()
    data object ReadingLog : MeSheet()
}

/** Confirmation dialogs on the Me screen. */
sealed class MeDialog {
    data object SignOut : MeDialog()
}
