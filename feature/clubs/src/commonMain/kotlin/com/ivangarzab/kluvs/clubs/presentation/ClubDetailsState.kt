package com.ivangarzab.kluvs.clubs.presentation

import com.ivangarzab.kluvs.model.AttendanceRoster
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.presentation.progress.OwnProgressInfo

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
    val createdClubId: String? = null,
    /** Attendance rosters keyed by discussion ID, populated lazily as timeline rows are shown. */
    val discussionRosters: Map<String, AttendanceRoster> = emptyMap(),
    /** The signed-in member's notes keyed by discussion ID, populated lazily when a note sheet is opened. */
    val discussionNotes: Map<String, DiscussionNoteInfo> = emptyMap()
)