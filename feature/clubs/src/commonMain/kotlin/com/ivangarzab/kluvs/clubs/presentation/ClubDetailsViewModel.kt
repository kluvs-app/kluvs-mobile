package com.ivangarzab.kluvs.clubs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.clubs.domain.ClearAttendanceUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateClubUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteClubUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.FinishSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.GetActiveSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.GetAttendanceRosterUseCase
import com.ivangarzab.kluvs.presentation.progress.GetSessionProgressUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubDetailsUseCase
import com.ivangarzab.kluvs.clubs.domain.GetDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.GetMemberClubsUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubMembersUseCase
import com.ivangarzab.kluvs.clubs.domain.RemoveMemberUseCase
import com.ivangarzab.kluvs.presentation.progress.SaveProgressUseCase
import com.ivangarzab.kluvs.clubs.domain.SetAttendanceUseCase
import com.ivangarzab.kluvs.clubs.domain.ToggleSessionParticipationUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateClubUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateMemberRoleUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateSessionUseCase
import com.ivangarzab.kluvs.model.AttendanceStatus
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.Role
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/**
 * The purpose of this [ViewModel] class is to serve the Club screen.
 */
class ClubDetailsViewModel(
    private val getClubDetails: GetClubDetailsUseCase,
    private val getActiveSession: GetActiveSessionUseCase,
    private val getClubMembers: GetClubMembersUseCase,
    private val getMemberClubsUseCase: GetMemberClubsUseCase,
    private val createClubUseCase: CreateClubUseCase,
    private val updateClubUseCase: UpdateClubUseCase,
    private val deleteClubUseCase: DeleteClubUseCase,
    private val createSessionUseCase: CreateSessionUseCase,
    private val updateSessionUseCase: UpdateSessionUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
    private val createDiscussionUseCase: CreateDiscussionUseCase,
    private val updateDiscussionUseCase: UpdateDiscussionUseCase,
    private val deleteDiscussionUseCase: DeleteDiscussionUseCase,
    private val updateMemberRoleUseCase: UpdateMemberRoleUseCase,
    private val removeMemberUseCase: RemoveMemberUseCase,
    private val getSessionProgressUseCase: GetSessionProgressUseCase,
    private val saveProgressUseCase: SaveProgressUseCase,
    private val finishSessionUseCase: FinishSessionUseCase,
    private val toggleSessionParticipationUseCase: ToggleSessionParticipationUseCase,
    private val getAttendanceRosterUseCase: GetAttendanceRosterUseCase,
    private val setAttendanceUseCase: SetAttendanceUseCase,
    private val clearAttendanceUseCase: ClearAttendanceUseCase,
    private val getDiscussionNoteUseCase: GetDiscussionNoteUseCase,
    private val createDiscussionNoteUseCase: CreateDiscussionNoteUseCase,
    private val updateDiscussionNoteUseCase: UpdateDiscussionNoteUseCase,
    private val deleteDiscussionNoteUseCase: DeleteDiscussionNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ClubDetailsState())
    val state: StateFlow<ClubDetailsState> = _state.asStateFlow()

    private var currentClubId: String? = null

    /**
     * Loads the user's clubs and displays the first club.
     * If the user has no clubs, sets state to show empty state.
     */
    fun loadUserClubs(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getMemberClubsUseCase(userId)
                .onSuccess { clubListItems ->
                    _state.update { it.copy(availableClubs = clubListItems) }

                    if (clubListItems.isNotEmpty()) {
                        Bark.i("Loaded ${clubListItems.size} club(s) for user (ID: $userId)")
                        // Load full details for the first club only
                        val firstClub = clubListItems.first()
                        _state.update { it.copy(selectedClubId = firstClub.id, userRole = firstClub.role) }
                        loadClubData(firstClub.id)
                    } else {
                        Bark.i("User has no clubs (ID: $userId)")
                        _state.update {
                            it.copy(isLoading = false)
                        }
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to load member clubs for user (ID: $userId). Please retry.", error)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load clubs"
                        )
                    }
                }
        }
    }

    fun selectClub(clubId: String) {
        val role = _state.value.availableClubs.find { it.id == clubId }?.role
        _state.update { it.copy(selectedClubId = clubId, userRole = role) }
        loadClubData(clubId)
    }

    fun loadClubData(clubId: String, forceRefresh: Boolean = false) {
        currentClubId = clubId

        viewModelScope.launch {
            // Reset state for subsequent calls
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    currentClubDetails = null,
                    activeSession = null,
                    ownProgress = null,
                    members = emptyList()
                )
            }

            // Launch all 3 UseCase calls in parallel
            val deferredDetails = async { getClubDetails(clubId, forceRefresh) }
            val deferredSession = async { getActiveSession(clubId, forceRefresh) }
            val deferredMembers = async { getClubMembers(clubId, forceRefresh) }

            // Await all results
            val detailsResult = deferredDetails.await()
            val sessionResult = deferredSession.await()
            val membersResult = deferredMembers.await()

            // Own progress depends on the session being known; failures are
            // non-blocking (the progress row simply stays empty, like the web app)
            val ownProgress = sessionResult.getOrNull()?.let { session ->
                getSessionProgressUseCase(session.sessionId, session.book.pageCount).getOrNull()
            }

            // Aggregate errors
            val errors = listOfNotNull(
                detailsResult.exceptionOrNull()?.message,
                sessionResult.exceptionOrNull()?.message,
                membersResult.exceptionOrNull()?.message
            )
            val error = when {
                errors.isEmpty() -> null
                errors.distinct().size == 1 -> errors.first() // All errors are identical
                else -> "Multiple errors occurred"
            }
            error?.let { e ->
                Bark.e("Failed to fetch club details (ID: $clubId). Serving cached data if available.", Exception(e))
            } ?: Bark.i("Successfully loaded club details (ID: $clubId)")

            // Update state with all results
            _state.update {
                it.copy(
                    isLoading = false,
                    error = error,
                    selectedClubId = clubId,
                    currentClubDetails = detailsResult.getOrNull(),
                    activeSession = sessionResult.getOrNull(),
                    ownProgress = ownProgress,
                    members = membersResult.getOrNull() ?: emptyList()
                )
            }
        }
    }

    fun refresh(forceRefresh: Boolean = false) {
        Bark.d("Refreshing club data (forceRefresh=$forceRefresh)")
        currentClubId?.let { loadClubData(it, forceRefresh) }
    }

    /**
     * Creates a new club and adds it to [ClubDetailsState.availableClubs]. Sets
     * [ClubDetailsState.createdClubId] so the UI can navigate into it — the caller
     * must consume it via [onConsumeCreatedClubId] once handled.
     */
    fun onCreateClub(userId: String, name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isOperationInProgress = true) }
            createClubUseCase(CreateClubUseCase.Params(userId, name))
                .onSuccess { newClub ->
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            availableClubs = it.availableClubs + newClub,
                            createdClubId = newClub.id,
                            operationResult = OperationResult.Success("Club created")
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Create club. ${error.message}", error)
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            operationResult = OperationResult.Error(
                                error.message ?: "An unexpected error occurred"
                            )
                        )
                    }
                }
        }
    }

    fun onConsumeCreatedClubId() {
        _state.update { it.copy(createdClubId = null) }
    }

    // -------------------------------------------------------------------------
    // General tab operations
    // -------------------------------------------------------------------------

    fun onUpdateClubName(newName: String) {
        val role = _state.value.userRole ?: return
        val clubId = currentClubId ?: return
        launchMutation("Club name updated") {
            updateClubUseCase(UpdateClubUseCase.Params(clubId, newName), role)
        }
    }

    fun onDeleteClub() {
        val role = _state.value.userRole ?: return
        val clubId = currentClubId ?: return
        launchMutation("Club deleted") {
            deleteClubUseCase(DeleteClubUseCase.Params(clubId), role)
        }
    }

    // -------------------------------------------------------------------------
    // Session tab operations
    // -------------------------------------------------------------------------

    fun onCreateSession(book: Book, dueDate: LocalDateTime?) {
        val role = _state.value.userRole ?: return
        val clubId = currentClubId ?: return
        launchMutation("Session created") {
            createSessionUseCase(CreateSessionUseCase.Params(clubId, book, dueDate), role)
        }
    }

    fun onUpdateSession(book: Book?, dueDate: LocalDateTime?) {
        val role = _state.value.userRole ?: return
        val sessionId = _state.value.activeSession?.sessionId ?: return
        launchMutation("Session updated") {
            updateSessionUseCase(UpdateSessionUseCase.Params(sessionId, book, dueDate), role)
        }
    }

    fun onDeleteSession() {
        val role = _state.value.userRole ?: return
        val sessionId = _state.value.activeSession?.sessionId ?: return
        launchMutation("Session deleted") {
            deleteSessionUseCase(DeleteSessionUseCase.Params(sessionId), role)
        }
    }

    /**
     * Saves the signed-in member's own reading progress on the active session.
     *
     * Unlike the other mutations, this does not refresh the whole club — the
     * saved progress is applied to state immediately (progress is not part of
     * the club payload).
     */
    fun onSaveProgress(
        type: ProgressType,
        currentPage: Int?,
        percentComplete: Float?,
        markFinished: Boolean
    ) {
        val session = _state.value.activeSession ?: return
        viewModelScope.launch {
            _state.update { it.copy(isOperationInProgress = true) }
            saveProgressUseCase(
                SaveProgressUseCase.Params(
                    progressId = _state.value.ownProgress?.progressId,
                    bookId = session.bookId,
                    sessionId = session.sessionId,
                    pageCount = session.book.pageCount,
                    type = type,
                    currentPage = currentPage,
                    percentComplete = percentComplete,
                    markFinished = markFinished
                )
            )
                .onSuccess { updated ->
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            ownProgress = updated,
                            operationResult = OperationResult.Success("Progress updated")
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Progress update. ${error.message}", error)
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            operationResult = OperationResult.Error(
                                error.message ?: "An unexpected error occurred"
                            )
                        )
                    }
                }
        }
    }

    /**
     * Ends the active session (admin/owner only). Surfaces the number of
     * credited readers in the success message and refreshes the club into
     * its no-active-session state.
     */
    fun onEndSession() {
        val role = _state.value.userRole ?: return
        val sessionId = _state.value.activeSession?.sessionId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isOperationInProgress = true) }
            finishSessionUseCase(FinishSessionUseCase.Params(sessionId), role)
                .onSuccess { credited ->
                    val message = when (credited) {
                        null -> "Session ended"
                        1 -> "Session ended — 1 member credited"
                        else -> "Session ended — $credited members credited"
                    }
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            operationResult = OperationResult.Success(message)
                        )
                    }
                    refresh(forceRefresh = true)
                }
                .onFailure { error ->
                    Bark.e("Operation failed: End session. ${error.message}", error)
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            operationResult = OperationResult.Error(
                                error.message ?: "An unexpected error occurred"
                            )
                        )
                    }
                }
        }
    }

    /**
     * Opts the given member in/out of the active session's reading ("Join this
     * Read" / "Opt out" on the Overview tab). Self-serve — any member may call
     * this for themselves.
     *
     * Unlike the other session mutations, this does not trigger a full club
     * refresh — [launchMutation]'s reload resets [ClubDetailsState.isLoading],
     * which tears down and rebuilds the whole tab pager, causing a visible
     * flash/limbo state for what should be a lightweight, frequent toggle.
     * Instead, the participant list is patched directly, same as [onSaveProgress].
     */
    fun onToggleParticipation(memberId: String, isReading: Boolean) {
        val session = _state.value.activeSession ?: return
        viewModelScope.launch {
            _state.update { it.copy(isOperationInProgress = true) }
            toggleSessionParticipationUseCase(
                ToggleSessionParticipationUseCase.Params(session.sessionId, memberId, isReading)
            )
                .onSuccess {
                    val updatedParticipants = session.participants.filterNot { it.memberId == memberId } +
                        SessionParticipantInfo(memberId, isReading)
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            activeSession = it.activeSession?.copy(participants = updatedParticipants),
                            operationResult = OperationResult.Success(
                                if (isReading) "Joined this read" else "Opted out"
                            )
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Toggle participation. ${error.message}", error)
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            operationResult = OperationResult.Error(
                                error.message ?: "An unexpected error occurred"
                            )
                        )
                    }
                }
        }
    }

    // -------------------------------------------------------------------------
    // Discussion operations
    // -------------------------------------------------------------------------

    fun onCreateDiscussion(title: String, location: String, date: LocalDateTime) {
        val role = _state.value.userRole ?: return
        val sessionId = _state.value.activeSession?.sessionId ?: return
        launchMutation("Discussion created") {
            createDiscussionUseCase(CreateDiscussionUseCase.Params(sessionId, title, location, date), role)
        }
    }

    fun onUpdateDiscussion(discussionId: String, title: String?, location: String?, date: LocalDateTime?) {
        val role = _state.value.userRole ?: return
        _state.value.activeSession ?: return
        launchMutation("Discussion updated") {
            updateDiscussionUseCase(UpdateDiscussionUseCase.Params(discussionId, title, location, date), role)
        }
    }

    fun onDeleteDiscussion(discussionId: String) {
        val role = _state.value.userRole ?: return
        _state.value.activeSession ?: return
        launchMutation("Discussion deleted") {
            deleteDiscussionUseCase(DeleteDiscussionUseCase.Params(discussionId), role)
        }
    }

    // -------------------------------------------------------------------------
    // Attendance operations
    // -------------------------------------------------------------------------

    /**
     * Loads the attendance roster for a discussion, if not already cached.
     *
     * Called lazily as timeline rows are shown (one roster per discussion),
     * mirroring the web app's per-row fetch-on-mount. Does not set
     * [ClubDetailsState.isOperationInProgress] — this fires for every visible
     * row and would spam the global progress indicator.
     */
    fun onLoadAttendanceRoster(discussionId: String) {
        if (_state.value.discussionRosters.containsKey(discussionId)) return
        viewModelScope.launch {
            getAttendanceRosterUseCase(discussionId)
                .onSuccess { roster ->
                    _state.update {
                        it.copy(discussionRosters = it.discussionRosters + (discussionId to roster))
                    }
                }
        }
    }

    /**
     * Sets or clears the signed-in member's RSVP for a discussion. Tapping the
     * already-selected status clears it, mirroring web's [AttendanceControl].
     *
     * The roster's [com.ivangarzab.kluvs.model.AttendanceRoster.myStatus] is
     * patched optimistically for instant pill feedback, then the roster is
     * re-fetched on success so response counts stay authoritative — unlike
     * `myStatus`, the full response list is never cached client-side (see
     * [com.ivangarzab.kluvs.data.repositories.DiscussionAttendanceRepository]),
     * so it can't be patched locally. On failure, `myStatus` is rolled back.
     *
     * Self-serve — does not use [launchMutation]/full refresh, same reasoning
     * as [onToggleParticipation].
     */
    fun onSetAttendance(discussionId: String, status: AttendanceStatus) {
        val roster = _state.value.discussionRosters[discussionId] ?: return
        val previousStatus = roster.myStatus
        val isClearing = previousStatus == status

        _state.update {
            it.copy(
                discussionRosters = it.discussionRosters + (discussionId to roster.copy(
                    myStatus = if (isClearing) null else status
                ))
            )
        }

        viewModelScope.launch {
            val result = if (isClearing) {
                clearAttendanceUseCase(discussionId).map { }
            } else {
                setAttendanceUseCase(SetAttendanceUseCase.Params(discussionId, status)).map { }
            }

            result
                .onSuccess {
                    getAttendanceRosterUseCase(discussionId).onSuccess { refreshedRoster ->
                        _state.update {
                            it.copy(discussionRosters = it.discussionRosters + (discussionId to refreshedRoster))
                        }
                    }
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Set attendance. ${error.message}", error)
                    _state.update {
                        it.copy(
                            discussionRosters = it.discussionRosters + (discussionId to roster.copy(myStatus = previousStatus)),
                            operationResult = OperationResult.Error(
                                error.message ?: "An unexpected error occurred"
                            )
                        )
                    }
                }
        }
    }

    // -------------------------------------------------------------------------
    // Discussion note operations
    // -------------------------------------------------------------------------

    /**
     * Loads the signed-in member's note for a discussion, if not already loaded.
     *
     * Lazy-once, same as [onLoadAttendanceRoster] — called when a note sheet opens.
     * A null [DiscussionNoteInfo.noteId] in the resulting entry means no note
     * exists yet, so the sheet should open straight into create/edit mode.
     */
    fun onLoadDiscussionNote(discussionId: String) {
        if (_state.value.discussionNotes.containsKey(discussionId)) return
        viewModelScope.launch {
            getDiscussionNoteUseCase(discussionId)
                .onSuccess { note ->
                    val info = DiscussionNoteInfo(noteId = note?.id, content = note?.content ?: "")
                    _state.update {
                        it.copy(discussionNotes = it.discussionNotes + (discussionId to info))
                    }
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Load discussion note. ${error.message}", error)
                    _state.update {
                        it.copy(
                            discussionNotes = it.discussionNotes + (discussionId to DiscussionNoteInfo(
                                error = error.message ?: "An unexpected error occurred"
                            ))
                        )
                    }
                }
        }
    }

    /**
     * Creates or updates the signed-in member's note for a discussion, depending on
     * whether a note already exists for it. Patches only that discussion's map
     * entry — mirrors [onSetAttendance]'s reasoning for not using [launchMutation].
     */
    fun onSaveDiscussionNote(discussionId: String, content: String) {
        val current = _state.value.discussionNotes[discussionId] ?: DiscussionNoteInfo()
        _state.update {
            it.copy(discussionNotes = it.discussionNotes + (discussionId to current.copy(isSaving = true, error = null)))
        }

        viewModelScope.launch {
            val result = if (current.noteId == null) {
                createDiscussionNoteUseCase(CreateDiscussionNoteUseCase.Params(discussionId, content))
            } else {
                updateDiscussionNoteUseCase(UpdateDiscussionNoteUseCase.Params(current.noteId, content))
            }

            result
                .onSuccess { note ->
                    _state.update {
                        it.copy(
                            discussionNotes = it.discussionNotes + (discussionId to DiscussionNoteInfo(
                                noteId = note.id,
                                content = note.content
                            ))
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Save discussion note. ${error.message}", error)
                    _state.update {
                        it.copy(
                            discussionNotes = it.discussionNotes + (discussionId to current.copy(
                                isSaving = false,
                                error = error.message ?: "An unexpected error occurred"
                            ))
                        )
                    }
                }
        }
    }

    /** Deletes the signed-in member's note for a discussion and resets its map entry to empty. */
    fun onDeleteDiscussionNote(discussionId: String) {
        val noteId = _state.value.discussionNotes[discussionId]?.noteId ?: return
        viewModelScope.launch {
            deleteDiscussionNoteUseCase(DeleteDiscussionNoteUseCase.Params(noteId))
                .onSuccess {
                    _state.update {
                        it.copy(discussionNotes = it.discussionNotes + (discussionId to DiscussionNoteInfo()))
                    }
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Delete discussion note. ${error.message}", error)
                    _state.update {
                        it.copy(
                            discussionNotes = it.discussionNotes + (discussionId to it.discussionNotes.getValue(discussionId).copy(
                                error = error.message ?: "An unexpected error occurred"
                            ))
                        )
                    }
                }
        }
    }

    // -------------------------------------------------------------------------
    // Member operations
    // -------------------------------------------------------------------------

    fun onUpdateMemberRole(memberId: String, currentMemberId: String, newRole: Role) {
        val role = _state.value.userRole ?: return
        val clubId = currentClubId ?: return
        launchMutation("Member role updated") {
            updateMemberRoleUseCase(UpdateMemberRoleUseCase.Params(memberId, clubId, currentMemberId, newRole), role)
        }
    }

    fun onRemoveMember(memberId: String, currentMemberId: String) {
        val role = _state.value.userRole ?: return
        val clubId = currentClubId ?: return
        launchMutation("Member removed") {
            removeMemberUseCase(RemoveMemberUseCase.Params(memberId, clubId, currentMemberId), role)
        }
    }

    // -------------------------------------------------------------------------
    // UI event consumption
    // -------------------------------------------------------------------------

    fun onConsumeOperationResult() {
        _state.update { it.copy(operationResult = null) }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Shared mutation runner: sets loading flags, invokes [block], and handles
     * success/failure by updating [operationResult] and triggering a refresh.
     */
    private fun launchMutation(successMessage: String, block: suspend () -> Result<*>) {
        viewModelScope.launch {
            _state.update { it.copy(isOperationInProgress = true) }
            val result = block()
            result
                .onSuccess {
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            operationResult = OperationResult.Success(successMessage)
                        )
                    }
                    refresh(forceRefresh = true)
                }
                .onFailure { error ->
                    Bark.e("Operation failed: $successMessage. ${error.message}", error)
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            operationResult = OperationResult.Error(
                                error.message ?: "An unexpected error occurred"
                            )
                        )
                    }
                }
        }
    }
}
