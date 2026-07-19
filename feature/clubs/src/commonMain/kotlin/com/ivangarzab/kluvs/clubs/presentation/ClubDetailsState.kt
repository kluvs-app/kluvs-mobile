package com.ivangarzab.kluvs.clubs.presentation

import com.ivangarzab.kluvs.model.Role

data class ClubDetailsState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val availableClubs: List<ClubListItem> = emptyList(),
    val selectedClubId: String? = null,
    val currentClubDetails: ClubDetails? = null,
    val activeSession: ActiveSessionDetails? = null,
    val ownProgress: OwnProgressInfo? = null,
    val members: List<MemberListItemInfo> = emptyList(),
    val userRole: Role? = null,
    val isOperationInProgress: Boolean = false,
    val operationResult: OperationResult? = null,
    /** ID of a just-created club, consumed by the UI to trigger navigation into it. */
    val createdClubId: String? = null
)