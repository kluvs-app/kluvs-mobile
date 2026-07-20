package com.ivangarzab.kluvs.member.presentation

import com.ivangarzab.kluvs.model.ReadingLog

data class MeState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val profile: UserProfile? = null,
    val statistics: UserStatistics? = null,
    val shelf: List<ShelfItem> = emptyList(),
    val upNext: UpNextItem? = null,
    val showLogoutConfirmation: Boolean = false,
    val snackbarError: String? = null,
    val isUploadingAvatar: Boolean = false,
    val readingLog: ReadingLog? = null,
    val isReadingLogLoading: Boolean = false,
    val showReadingLog: Boolean = false,
)
