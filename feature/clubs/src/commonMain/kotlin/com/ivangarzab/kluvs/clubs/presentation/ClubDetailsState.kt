package com.ivangarzab.kluvs.clubs.presentation

import com.ivangarzab.kluvs.model.Role

data class ClubDetailsState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val availableClubs: List<ClubListItem> = emptyList(),
    val selectedClubId: String? = null,
    val currentClubDetails: ClubDetails? = null,
    val activeSession: ActiveSessionDetails? = null,
    val members: List<MemberListItemInfo> = emptyList(),
    val userRole: Role? = null,
    val isOperationInProgress: Boolean = false,
    val operationResult: OperationResult? = null
)