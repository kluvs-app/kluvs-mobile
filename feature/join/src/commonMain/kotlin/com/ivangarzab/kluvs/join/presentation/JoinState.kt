package com.ivangarzab.kluvs.join.presentation

import com.ivangarzab.kluvs.model.ClubPreview

/**
 * UI state for the join-by-invite-token screen.
 */
data class JoinState(
    val tokenInput: String = "",
    val isAuthenticated: Boolean = false,
    val isLoadingPreview: Boolean = false,
    val preview: ClubPreview? = null,
    val previewError: String? = null,
    val isJoining: Boolean = false,
    val joinedClubId: String? = null,
    val joinError: String? = null,
    /**
     * One-shot signal: the user tapped Join while signed out. The platform UI should
     * stash [tokenInput] with the pending-join coordinator, navigate to auth, and call
     * [com.ivangarzab.kluvs.join.presentation.JoinViewModel.onConsumeNeedsSignIn].
     */
    val needsSignIn: Boolean = false
)
