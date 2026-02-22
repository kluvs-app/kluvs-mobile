package com.ivangarzab.kluvs.clubs.presentation

import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.core.component.KoinComponent
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
class ClubDetailsViewModelHelper : KoinComponent {

    private val viewModel: ClubDetailsViewModel by inject()
    private val coroutineScope: CoroutineScope by inject()

    /**
     * iOS-friendly observation method.
     *
     * Returns a [com.ivangarzab.kluvs.presentation.viewmodels.Closeable] that can be used to cancel the observation.
     */
    fun observeState(callback: (ClubDetailsState) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    fun loadUserClubs(userId: String) = viewModel.loadUserClubs(userId)
    fun loadClubData(clubId: String) = viewModel.loadClubData(clubId)
    fun selectClub(clubId: String) = viewModel.selectClub(clubId)
    fun refresh() = viewModel.refresh()

    // General tab
    fun onUpdateClubName(newName: String) = viewModel.onUpdateClubName(newName)
    fun onDeleteClub() = viewModel.onDeleteClub()

    // Session tab — dates passed as ISO strings to avoid LocalDateTime in Swift
    fun onCreateSession(book: Book, dueDateIso: String?) =
        viewModel.onCreateSession(book, dueDateIso?.toLocalDateTime())
    fun onUpdateSession(book: Book?, dueDateIso: String?) =
        viewModel.onUpdateSession(book, dueDateIso?.toLocalDateTime())
    fun onDeleteSession() = viewModel.onDeleteSession()

    // Discussion operations — dates passed as ISO strings
    fun onCreateDiscussion(title: String, location: String, dateIso: String) =
        viewModel.onCreateDiscussion(title, location, dateIso.toLocalDateTime())
    fun onUpdateDiscussion(discussionId: String, title: String?, location: String?, dateIso: String?) =
        viewModel.onUpdateDiscussion(discussionId, title, location, dateIso?.toLocalDateTime())
    fun onDeleteDiscussion(discussionId: String) = viewModel.onDeleteDiscussion(discussionId)

    // Member operations
    fun onUpdateMemberRole(memberId: String, currentMemberId: String, newRole: Role) =
        viewModel.onUpdateMemberRole(memberId, currentMemberId, newRole)
    fun onRemoveMember(memberId: String, currentMemberId: String) =
        viewModel.onRemoveMember(memberId, currentMemberId)

    // UI event consumption
    fun onConsumeOperationResult() = viewModel.onConsumeOperationResult()

    /** Safely unwraps a sealed [OperationResult] to a plain message string for iOS. */
    fun operationResultMessage(result: OperationResult?): String? = when (result) {
        is OperationResult.Success -> result.message
        is OperationResult.Error -> result.message
        null -> null
    }

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

    private fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this)
}
