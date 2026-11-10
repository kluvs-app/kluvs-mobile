package com.ivangarzab.kluvs.clubs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.error.toAppError
import com.ivangarzab.kluvs.presentation.error.toUserMessage
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
import com.ivangarzab.kluvs.clubs.domain.GetCurrentMemberIdUseCase
import com.ivangarzab.kluvs.clubs.domain.GetDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.GetMemberClubsUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubMembersUseCase
import com.ivangarzab.kluvs.clubs.domain.LeaveClubUseCase
import com.ivangarzab.kluvs.clubs.domain.RegisterBookUseCase
import com.ivangarzab.kluvs.clubs.domain.RemoveMemberUseCase
import com.ivangarzab.kluvs.clubs.domain.RotateInviteLinkUseCase
import com.ivangarzab.kluvs.clubs.domain.SearchBooksUseCase
import com.ivangarzab.kluvs.presentation.progress.SaveProgressUseCase
import com.ivangarzab.kluvs.clubs.domain.SetAttendanceUseCase
import com.ivangarzab.kluvs.clubs.domain.ToggleSessionParticipationUseCase
import com.ivangarzab.kluvs.clubs.domain.TransferOwnershipUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateClubUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateJoinPolicyUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateMemberRoleUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateSessionUseCase
import com.ivangarzab.kluvs.model.AttendanceStatus
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.JoinPolicy
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.Role
import kotlinx.coroutines.Job
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
    private val getCurrentMemberId: GetCurrentMemberIdUseCase,
    private val searchBooksUseCase: SearchBooksUseCase,
    private val registerBookUseCase: RegisterBookUseCase,
    private val createClubUseCase: CreateClubUseCase,
    private val updateClubUseCase: UpdateClubUseCase,
    private val updateJoinPolicyUseCase: UpdateJoinPolicyUseCase,
    private val rotateInviteLinkUseCase: RotateInviteLinkUseCase,
    private val deleteClubUseCase: DeleteClubUseCase,
    private val createSessionUseCase: CreateSessionUseCase,
    private val updateSessionUseCase: UpdateSessionUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
    private val createDiscussionUseCase: CreateDiscussionUseCase,
    private val updateDiscussionUseCase: UpdateDiscussionUseCase,
    private val deleteDiscussionUseCase: DeleteDiscussionUseCase,
    private val updateMemberRoleUseCase: UpdateMemberRoleUseCase,
    private val removeMemberUseCase: RemoveMemberUseCase,
    private val leaveClubUseCase: LeaveClubUseCase,
    private val transferOwnershipUseCase: TransferOwnershipUseCase,
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
    private var currentUserId: String? = null
    private var loadClubDataJob: Job? = null
    private var loadUserClubsJob: Job? = null

    /**
     * Loads the user's clubs. Auto-selects the first club only when nothing is selected
     * yet (or the selection is no longer in the list); otherwise the current selection is
     * kept and just the list (and, on [forceRefresh], the selected club) is refreshed.
     *
     * This runs on every Clubs tab entry on both platforms, so it must never yank the user
     * out of the club they're looking at.
     */
    fun loadUserClubs(userId: String, forceRefresh: Boolean = false) {
        currentUserId = userId

        // Cancel the previous list load so a slow, stale completion can't overwrite a newer
        // list or re-select a club behind the user's back.
        loadUserClubsJob?.cancel()
        loadUserClubsJob = viewModelScope.launch {
            // Only take over the screen with a loader when there's no club selected yet; a
            // background list refresh keeps whatever the user is looking at on screen.
            val hadSelection = currentClubId != null
            if (!hadSelection) _state.update { it.copy(isLoading = true, error = null) }

            getMemberClubsUseCase(userId, forceRefresh)
                .onSuccess { clubListItems ->
                    _state.update { it.copy(availableClubs = clubListItems) }

                    if (clubListItems.isEmpty()) {
                        Bark.i("User has no clubs (ID: $userId)")
                        currentClubId = null
                        _state.update { it.copy(isLoading = false, selectedClubId = null) }
                        return@onSuccess
                    }
                    Bark.i("Loaded ${clubListItems.size} club(s) for user (ID: $userId)")

                    // `currentClubId` is read again here, not from `hadSelection`: the user may
                    // have tapped a club while this load was in flight, and that choice wins.
                    val selected = currentClubId?.let { id -> clubListItems.find { it.id == id } }
                    if (selected != null) {
                        Bark.d("Keeping selected club (ID: ${selected.id}) across club list reload")
                        _state.update { it.copy(selectedClubId = selected.id, userRole = selected.role) }
                        if (forceRefresh) loadClubData(selected.id, forceRefresh = true)
                    } else {
                        val firstClub = clubListItems.first()
                        _state.update { it.copy(selectedClubId = firstClub.id, userRole = firstClub.role) }
                        loadClubData(firstClub.id)
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to load member clubs for user (ID: $userId). Please retry.", error)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            // Don't replace a club that's already on screen with an error page
                            error = if (hadSelection) it.error else error.toAppError().toUserMessage()
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

        // Cancel any still-in-flight load for a previously-selected club — without this, a
        // slower fetch for club A can resolve after the user has already navigated into club
        // B and overwrite B's state with A's data (the exact "wrong club" bug this guards
        // against). The `currentClubId != clubId` check below is defense-in-depth for the
        // narrow window where cancellation races with completion.
        loadClubDataJob?.cancel()
        loadClubDataJob = viewModelScope.launch {
            // A forced refresh of the club already on screen keeps its content up while the
            // refresh indicator spins; if the refresh then fails (e.g. offline) the content stays
            // and the failure is reported through a snackbar instead of an error page.
            // Any other load (first load, switching clubs) starts from a clean slate.
            val keepContent = forceRefresh &&
                _state.value.currentClubDetails != null &&
                _state.value.selectedClubId == clubId
            _state.update {
                if (keepContent) {
                    it.copy(isLoading = true, error = null)
                } else {
                    it.copy(
                        isLoading = true,
                        error = null,
                        currentClubDetails = null,
                        activeSession = null,
                        ownProgress = null,
                        members = emptyList(),
                        currentMemberId = null
                    )
                }
            }

            // Launch all UseCase calls in parallel
            val deferredDetails = async { getClubDetails(clubId, forceRefresh) }
            val deferredSession = async { getActiveSession(clubId, forceRefresh) }
            val deferredMembers = async { getClubMembers(clubId, forceRefresh) }
            // Independent of the club roster fetch above on purpose — see
            // GetCurrentMemberIdUseCase's doc for why this can't just be read off `members`.
            val deferredMemberId = currentUserId?.let { uid -> async { getCurrentMemberId(uid, forceRefresh) } }

            // Await all results
            val detailsResult = deferredDetails.await()
            val sessionResult = deferredSession.await()
            val membersResult = deferredMembers.await()

            // Own progress depends on the session being known; failures are
            // non-blocking (the progress row simply stays empty, like the web app)
            val ownProgress = sessionResult.getOrNull()?.let { session ->
                getSessionProgressUseCase(session.sessionId, session.book.pageCount).getOrNull()
            }

            // Like ownProgress, a failure here is non-blocking — membership-gated actions
            // simply stay hidden rather than the whole club screen erroring out over it.
            val currentMemberId = deferredMemberId?.await()?.getOrNull()

            // Aggregate errors
            val errors = listOfNotNull(
                detailsResult.exceptionOrNull()?.toAppError()?.toUserMessage(),
                sessionResult.exceptionOrNull()?.toAppError()?.toUserMessage(),
                membersResult.exceptionOrNull()?.toAppError()?.toUserMessage()
            )
            val error = when {
                errors.isEmpty() -> null
                errors.distinct().size == 1 -> errors.first() // All errors are identical
                else -> "Multiple things went wrong. Please try again."
            }
            error?.let { e ->
                Bark.e("Failed to fetch club details (ID: $clubId). ${if (keepContent) "Keeping what's on screen." else "Showing error."}", Exception(e))
            } ?: Bark.i("Successfully loaded club details (ID: $clubId)")

            if (currentClubId != clubId) return@launch

            // Re-derive the signed-in member's role from the freshly-fetched roster rather than
            // trusting the role captured at selectClub()-time — a role change (e.g. ownership
            // transfer) updates `members` here but wouldn't otherwise be reflected until the user
            // navigated away and back, since `userRole`/`availableClubs` aren't touched elsewhere
            // on refresh.
            val members = membersResult.getOrNull() ?: if (keepContent) _state.value.members else emptyList()
            val refreshedRole = currentMemberId?.let { id -> members.find { it.memberId == id }?.role }

            // Update state with all results. When keeping content, each piece only changes if its
            // fetch succeeded (a successful "no active session" is still a change).
            _state.update {
                it.copy(
                    isLoading = false,
                    error = if (keepContent) null else error,
                    operationResult = if (keepContent && error != null) OperationResult.Error(error) else it.operationResult,
                    selectedClubId = clubId,
                    currentClubDetails = detailsResult.getOrNull() ?: if (keepContent) it.currentClubDetails else null,
                    activeSession = if (sessionResult.isSuccess) sessionResult.getOrNull() else if (keepContent) it.activeSession else null,
                    ownProgress = if (sessionResult.isSuccess) ownProgress else if (keepContent) it.ownProgress else null,
                    members = members,
                    currentMemberId = currentMemberId ?: if (keepContent) it.currentMemberId else null,
                    userRole = refreshedRole ?: it.userRole,
                    availableClubs = refreshedRole?.let { role ->
                        it.availableClubs.map { club -> if (club.id == clubId) club.copy(role = role) else club }
                    } ?: it.availableClubs
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

    /**
     * Deletes the current club. Unlike other mutations, this doesn't go through
     * [launchMutation] — refreshing a club that was just deleted would just error out.
     * Instead it signals [ClubDetailsState.deletedClubId] so the UI navigates back out of
     * the now-gone club's detail screen.
     */
    fun onDeleteClub() {
        val role = _state.value.userRole ?: return
        val clubId = currentClubId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isOperationInProgress = true) }
            deleteClubUseCase(DeleteClubUseCase.Params(clubId), role)
                .onSuccess {
                    currentClubId = null
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            availableClubs = it.availableClubs.filterNot { club -> club.id == clubId },
                            deletedClubId = clubId,
                            selectedClubId = null,
                            operationResult = OperationResult.Success("Club deleted")
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Delete club. ${error.message}", error)
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

    fun onConsumeDeletedClubId() {
        _state.update { it.copy(deletedClubId = null) }
    }

    fun onConsumeLeftClubId() {
        _state.update { it.copy(leftClubId = null) }
    }

    /**
     * Toggles the club's join policy (invite link on/off). Owner-only.
     *
     * [joinPolicy]/[com.ivangarzab.kluvs.clubs.presentation.ClubDetails.inviteToken] are
     * never cached locally, so this triggers a force refresh on success like any other
     * mutation via [launchMutation] — the share sheet should also force-refresh on open
     * to avoid showing stale (absent) invite data from a cache hit.
     */
    fun onUpdateJoinPolicy(joinPolicy: JoinPolicy) {
        val role = _state.value.userRole ?: return
        val clubId = currentClubId ?: return
        launchMutation("Join policy updated") {
            updateJoinPolicyUseCase(UpdateJoinPolicyUseCase.Params(clubId, joinPolicy), role)
        }
    }

    /** Rotates the club's active invite token. Owner-only. See [RotateInviteLinkUseCase]. */
    fun onRotateInviteLink() {
        val role = _state.value.userRole ?: return
        val clubId = currentClubId ?: return
        launchMutation("Invite link rotated") {
            rotateInviteLinkUseCase(RotateInviteLinkUseCase.Params(clubId), role)
        }
    }

    // -------------------------------------------------------------------------
    // Session tab operations
    // -------------------------------------------------------------------------

    fun onBookSearchQueryChange(query: String) {
        _state.update { it.copy(bookSearchQuery = query) }
    }

    /** Searches for books matching [query]; a blank query just clears any prior results. */
    fun onSearchBooks(query: String) {
        if (query.isBlank()) {
            _state.update { it.copy(bookSearchResults = emptyList(), bookSearchError = null, isSearchingBooks = false) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSearchingBooks = true, bookSearchError = null) }
            searchBooksUseCase(query)
                .onSuccess { result ->
                    _state.update { it.copy(isSearchingBooks = false, bookSearchResults = result.books) }
                }
                .onFailure { error ->
                    Bark.e("Book search failed. Please retry.", error)
                    _state.update {
                        it.copy(
                            isSearchingBooks = false,
                            bookSearchResults = emptyList(),
                            bookSearchError = error.toAppError().toUserMessage()
                        )
                    }
                }
        }
    }

    /**
     * Selects a book from search results and registers it with the backend to obtain a
     * real, server-assigned ID — registration is unconditional (idempotent upsert) on every
     * selection, matching web's `BookSearchInput`, not just for not-yet-registered books.
     */
    fun onSelectBook(book: Book) {
        viewModelScope.launch {
            _state.update { it.copy(isRegisteringBook = true) }
            registerBookUseCase(book)
                .onSuccess { registered ->
                    _state.update {
                        it.copy(
                            isRegisteringBook = false,
                            selectedBook = registered,
                            bookSearchResults = emptyList(),
                            bookSearchQuery = registered.title
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Book registration failed. Please retry.", error)
                    _state.update {
                        it.copy(isRegisteringBook = false, bookSearchError = error.toAppError().toUserMessage())
                    }
                }
        }
    }

    /** Clears the selected book, returning the Create Session sheet's book field to search mode. */
    fun onClearSelectedBook() {
        _state.update { it.copy(selectedBook = null, bookSearchQuery = "", bookSearchResults = emptyList()) }
    }

    /** Resets book-search state; call when the Create Session sheet opens and when it closes. */
    fun onResetBookSearch() {
        _state.update {
            it.copy(
                bookSearchQuery = "",
                bookSearchResults = emptyList(),
                isSearchingBooks = false,
                bookSearchError = null,
                selectedBook = null,
                isRegisteringBook = false
            )
        }
    }

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

    /**
     * Removes the signed-in member from the current club. Unlike other mutations, this doesn't
     * go through [launchMutation] — refreshing a club just left would just error out. Instead it
     * signals [ClubDetailsState.leftClubId] so the UI navigates back out of the now-departed
     * club's detail screen, mirroring [onDeleteClub].
     */
    fun onLeaveClub() {
        val role = _state.value.userRole ?: return
        val clubId = currentClubId ?: return
        val memberId = _state.value.currentMemberId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isOperationInProgress = true) }
            leaveClubUseCase(LeaveClubUseCase.Params(memberId, clubId), role)
                .onSuccess {
                    currentClubId = null
                    _state.update {
                        it.copy(
                            isOperationInProgress = false,
                            availableClubs = it.availableClubs.filterNot { club -> club.id == clubId },
                            leftClubId = clubId,
                            selectedClubId = null,
                            operationResult = OperationResult.Success("Left club")
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Operation failed: Leave club. ${error.message}", error)
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

    fun onTransferOwnership(newOwnerId: String) {
        val role = _state.value.userRole ?: return
        val clubId = currentClubId ?: return
        val currentMemberId = _state.value.currentMemberId ?: return
        launchMutation("Ownership transferred") {
            transferOwnershipUseCase(TransferOwnershipUseCase.Params(currentMemberId, newOwnerId, clubId), role)
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
