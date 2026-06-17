package com.ivangarzab.kluvs.clubs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteClubUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.GetActiveSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubDetailsUseCase
import com.ivangarzab.kluvs.clubs.domain.GetMemberClubsUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubMembersUseCase
import com.ivangarzab.kluvs.clubs.domain.RemoveMemberUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateClubUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateMemberRoleUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateSessionUseCase
import com.ivangarzab.kluvs.model.Book
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
    private val updateClubUseCase: UpdateClubUseCase,
    private val deleteClubUseCase: DeleteClubUseCase,
    private val createSessionUseCase: CreateSessionUseCase,
    private val updateSessionUseCase: UpdateSessionUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
    private val createDiscussionUseCase: CreateDiscussionUseCase,
    private val updateDiscussionUseCase: UpdateDiscussionUseCase,
    private val deleteDiscussionUseCase: DeleteDiscussionUseCase,
    private val updateMemberRoleUseCase: UpdateMemberRoleUseCase,
    private val removeMemberUseCase: RemoveMemberUseCase
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
                    members = membersResult.getOrNull() ?: emptyList()
                )
            }
        }
    }

    fun refresh(forceRefresh: Boolean = false) {
        Bark.d("Refreshing club data (forceRefresh=$forceRefresh)")
        currentClubId?.let { loadClubData(it, forceRefresh) }
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
        val sessionId = _state.value.activeSession?.sessionId ?: return
        launchMutation("Discussion updated") {
            updateDiscussionUseCase(UpdateDiscussionUseCase.Params(sessionId, discussionId, title, location, date), role)
        }
    }

    fun onDeleteDiscussion(discussionId: String) {
        val role = _state.value.userRole ?: return
        val sessionId = _state.value.activeSession?.sessionId ?: return
        launchMutation("Discussion deleted") {
            deleteDiscussionUseCase(DeleteDiscussionUseCase.Params(sessionId, discussionId), role)
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
