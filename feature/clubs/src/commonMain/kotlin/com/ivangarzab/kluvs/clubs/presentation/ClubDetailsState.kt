package com.ivangarzab.kluvs.clubs.presentation

import com.ivangarzab.kluvs.model.AttendanceRoster
import com.ivangarzab.kluvs.model.Book
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
    /**
     * The signed-in user's own member ID, resolved independently of [members] (see
     * [com.ivangarzab.kluvs.clubs.domain.GetCurrentMemberIdUseCase]) — a stale/failed roster
     * fetch no longer silently hides membership-gated actions that don't actually need the
     * roster at all (opt-in/out, change role, remove member).
     */
    val currentMemberId: String? = null,
    val userRole: Role? = null,
    val isOperationInProgress: Boolean = false,
    /** ID of a just-created club, consumed by the UI to trigger navigation into it. */
    val createdClubId: String? = null,
    /** ID of a just-deleted club, consumed by the UI to navigate back out of its detail screen. */
    val deletedClubId: String? = null,
    /** ID of a club the signed-in member just left, consumed by the UI to navigate back out of its detail screen. */
    val leftClubId: String? = null,
    /** Attendance rosters keyed by discussion ID, populated lazily as timeline rows are shown. */
    val discussionRosters: Map<String, AttendanceRoster> = emptyMap(),
    /** The signed-in member's notes keyed by discussion ID, populated lazily when a note sheet is opened. */
    val discussionNotes: Map<String, DiscussionNoteInfo> = emptyMap(),
    /** Book search state for the Create Session sheet's book-selection field. */
    val bookSearchQuery: String = "",
    val bookSearchResults: List<Book> = emptyList(),
    val isSearchingBooks: Boolean = false,
    val bookSearchError: String? = null,
    /** The book chosen (and registered, with a real server-assigned ID) for the session being created. */
    val selectedBook: Book? = null,
    val isRegisteringBook: Boolean = false,
    /** The bottom sheet on screen, if any. Opened with `onOpenSheet`, closed by `onDismissSheet` or a successful save. */
    val activeSheet: ClubDetailsSheet? = null,
    /** The confirmation dialog on screen, if any. It can sit on top of [activeSheet]. */
    val activeDialog: ClubDetailsDialog? = null
)

/** Bottom sheets on the Clubs list and club detail screens. At most one is open at a time. */
sealed class ClubDetailsSheet {
    data object CreateClub : ClubDetailsSheet()
    data object JoinClub : ClubDetailsSheet()
    data object EditClub : ClubDetailsSheet()
    data object ShareClub : ClubDetailsSheet()
    data object CreateSession : ClubDetailsSheet()
    data object EditSession : ClubDetailsSheet()
    data object UpdateProgress : ClubDetailsSheet()
    data object CreateDiscussion : ClubDetailsSheet()
    data class EditDiscussion(val discussionId: String) : ClubDetailsSheet()
    data class DiscussionNote(val discussionId: String) : ClubDetailsSheet()
    data class ChangeRole(val memberId: String) : ClubDetailsSheet()
}

/** Confirmation dialogs on the club detail screen. At most one is open at a time. */
sealed class ClubDetailsDialog {
    data object DeleteClub : ClubDetailsDialog()
    data object LeaveClub : ClubDetailsDialog()
    data object EndSession : ClubDetailsDialog()
    data class DeleteDiscussion(val discussionId: String) : ClubDetailsDialog()
    data class DeleteDiscussionNote(val discussionId: String) : ClubDetailsDialog()
    data class RemoveMember(val memberId: String) : ClubDetailsDialog()
    data class TransferOwnership(val memberId: String) : ClubDetailsDialog()
}