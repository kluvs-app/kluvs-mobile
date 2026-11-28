package com.ivangarzab.kluvs.clubs.presentation

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.model.AttendanceStatus
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.JoinPolicy
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModelHelper
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [ClubDetailsViewModel] for easier
 * use and access on the iOS side.
 *
 * All date/time values are exchanged with iOS as ISO-8601 strings
 * (e.g. "2025-01-15T19:00:00") to avoid exposing [LocalDateTime] to Swift,
 * where kotlinx.datetime types do not bridge cleanly.
 */
@Suppress("unused")
class ClubDetailsViewModelHelper : BaseViewModelHelper<ClubDetailsState>() {

    override val viewModel: ClubDetailsViewModel by inject()

    fun loadUserClubs(userId: String, forceRefresh: Boolean = false) = viewModel.loadUserClubs(userId, forceRefresh)
    fun loadClubData(clubId: String) = viewModel.loadClubData(clubId)
    fun selectClub(clubId: String) = viewModel.selectClub(clubId)
    fun refresh(forceRefresh: Boolean = false) = viewModel.refresh(forceRefresh)

    // Club creation
    fun onCreateClub(userId: String, name: String) = viewModel.onCreateClub(userId, name)
    fun onConsumeCreatedClubId() = viewModel.onConsumeCreatedClubId()

    // General tab
    fun onUpdateClubName(newName: String) = viewModel.onUpdateClubName(newName)
    fun onDeleteClub() = viewModel.onDeleteClub()
    fun onConsumeDeletedClubId() = viewModel.onConsumeDeletedClubId()
    fun onUpdateJoinPolicy(joinPolicy: JoinPolicy) = viewModel.onUpdateJoinPolicy(joinPolicy)
    fun onRotateInviteLink() = viewModel.onRotateInviteLink()
    fun onCopyInviteLink(inviteUrl: String) = viewModel.onCopyInviteLink(inviteUrl)
    fun onShareInviteLink(inviteUrl: String) = viewModel.onShareInviteLink(inviteUrl)

    // Session tab — book search
    fun onBookSearchQueryChange(query: String) = viewModel.onBookSearchQueryChange(query)
    fun onSearchBooks(query: String) = viewModel.onSearchBooks(query)
    fun onSelectBook(book: Book) = viewModel.onSelectBook(book)
    fun onClearSelectedBook() = viewModel.onClearSelectedBook()
    fun onResetBookSearch() = viewModel.onResetBookSearch()

    // Session tab — dates passed as ISO strings to avoid LocalDateTime in Swift
    fun onCreateSession(book: Book, dueDateIso: String?) =
        viewModel.onCreateSession(book, dueDateIso?.toLocalDateTime())
    fun onUpdateSession(book: Book?, dueDateIso: String?) =
        viewModel.onUpdateSession(book, dueDateIso?.toLocalDateTime())
    fun onDeleteSession() = viewModel.onDeleteSession()
    fun onEndSession() = viewModel.onEndSession()
    fun onToggleParticipation(memberId: String, isReading: Boolean) =
        viewModel.onToggleParticipation(memberId, isReading)

    /** Percent is passed as a plain Int (0-100) to avoid Float bridging quirks on the Swift side. */
    fun onSaveProgress(type: ProgressType, currentPage: Int?, percentComplete: Int?, markFinished: Boolean) =
        viewModel.onSaveProgress(type, currentPage, percentComplete?.toFloat(), markFinished)

    // Discussion operations — dates passed as ISO strings
    fun onCreateDiscussion(title: String, location: String, dateIso: String) {
        val date = dateIso.toLocalDateTime() ?: return
        viewModel.onCreateDiscussion(title, location, date)
    }
    fun onUpdateDiscussion(discussionId: String, title: String?, location: String?, dateIso: String?) =
        viewModel.onUpdateDiscussion(discussionId, title, location, dateIso?.toLocalDateTime())
    fun onDeleteDiscussion(discussionId: String) = viewModel.onDeleteDiscussion(discussionId)

    // Attendance operations
    fun onLoadAttendanceRoster(discussionId: String) = viewModel.onLoadAttendanceRoster(discussionId)
    fun onSetAttendance(discussionId: String, status: AttendanceStatus) =
        viewModel.onSetAttendance(discussionId, status)

    // Discussion note operations
    fun onLoadDiscussionNote(discussionId: String) = viewModel.onLoadDiscussionNote(discussionId)
    fun onSaveDiscussionNote(discussionId: String, content: String) =
        viewModel.onSaveDiscussionNote(discussionId, content)
    fun onDeleteDiscussionNote(discussionId: String) = viewModel.onDeleteDiscussionNote(discussionId)

    // Member operations
    fun onUpdateMemberRole(memberId: String, currentMemberId: String, newRole: Role) =
        viewModel.onUpdateMemberRole(memberId, currentMemberId, newRole)
    fun onRemoveMember(memberId: String, currentMemberId: String) =
        viewModel.onRemoveMember(memberId, currentMemberId)
    fun onLeaveClub() = viewModel.onLeaveClub()
    fun onConsumeLeftClubId() = viewModel.onConsumeLeftClubId()
    fun onTransferOwnership(newOwnerId: String) = viewModel.onTransferOwnership(newOwnerId)

    // ── Sheets and dialogs ────────────────────────────────────────────────────

    fun onOpenSheet(sheet: ClubDetailsSheet) = viewModel.onOpenSheet(sheet)
    fun onDismissSheet() = viewModel.onDismissSheet()
    fun onOpenDialog(dialog: ClubDetailsDialog) = viewModel.onOpenDialog(dialog)
    fun onDismissDialog() = viewModel.onDismissDialog()

    // ── Date helpers ──────────────────────────────────────────────────────────

    /** Converts a [LocalDateTime] to an ISO string for iOS date pickers. */
    fun localDateTimeToIso(dateTime: LocalDateTime?): String? = dateTime?.toString()

    /** Returns the ISO string for a specific discussion's date, or null if not found. */
    fun getDiscussionDateIso(discussionId: String): String? =
        viewModel.state.value.activeSession?.discussions
            ?.find { it.id == discussionId }
            ?.rawDate
            ?.toString()

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun String.toLocalDateTime(): LocalDateTime? = runCatching {
        LocalDateTime.parse(this)
    }.onFailure { e ->
        Bark.e("Failed to parse ISO date string '$this'", e)
    }.getOrNull()
}
