package com.ivangarzab.kluvs.presentation.progress

import com.ivangarzab.kluvs.model.ProgressType

/**
 * UI model for the signed-in member's own reading progress on a session.
 *
 * [percent] and [label] are pre-computed for direct display, mirroring the
 * web app's ProgressRow ("X of Y pages", "N% complete", "Finished").
 *
 * Shared between the Clubs and Me screens — both track progress against a session.
 */
data class OwnProgressInfo(
    val progressId: String,
    val type: ProgressType,
    val currentPage: Int?,
    val percentComplete: Float?,
    val isCompleted: Boolean,
    val percent: Int,
    val label: String
)
