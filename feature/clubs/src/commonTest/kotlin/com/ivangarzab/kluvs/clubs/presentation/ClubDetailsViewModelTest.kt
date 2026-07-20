package com.ivangarzab.kluvs.clubs.presentation

import com.ivangarzab.kluvs.clubs.domain.CreateClubUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.CreateSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteClubUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.DeleteSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.FinishSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.GetActiveSessionUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubDetailsUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubMembersUseCase
import com.ivangarzab.kluvs.clubs.domain.GetMemberClubsUseCase
import com.ivangarzab.kluvs.presentation.progress.GetSessionProgressUseCase
import com.ivangarzab.kluvs.clubs.domain.RemoveMemberUseCase
import com.ivangarzab.kluvs.presentation.progress.SaveProgressUseCase
import com.ivangarzab.kluvs.clubs.domain.ToggleSessionParticipationUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateClubUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateDiscussionUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateMemberRoleUseCase
import com.ivangarzab.kluvs.clubs.domain.UpdateSessionUseCase
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.data.repositories.ProgressRepository
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.ClubMember
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.Session
import com.ivangarzab.kluvs.model.SessionMember
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ClubDetailsViewModelTest {

    private lateinit var clubRepository: ClubRepository
    private lateinit var memberRepository: MemberRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var avatarRepository: AvatarRepository
    private lateinit var getClubDetails: GetClubDetailsUseCase
    private lateinit var getActiveSession: GetActiveSessionUseCase
    private lateinit var getClubMembers: GetClubMembersUseCase
    private lateinit var getMemberClubs: GetMemberClubsUseCase
    private lateinit var createClubUseCase: CreateClubUseCase
    private lateinit var updateClubUseCase: UpdateClubUseCase
    private lateinit var deleteClubUseCase: DeleteClubUseCase
    private lateinit var createSessionUseCase: CreateSessionUseCase
    private lateinit var updateSessionUseCase: UpdateSessionUseCase
    private lateinit var deleteSessionUseCase: DeleteSessionUseCase
    private lateinit var createDiscussionUseCase: CreateDiscussionUseCase
    private lateinit var updateDiscussionUseCase: UpdateDiscussionUseCase
    private lateinit var deleteDiscussionUseCase: DeleteDiscussionUseCase
    private lateinit var updateMemberRoleUseCase: UpdateMemberRoleUseCase
    private lateinit var removeMemberUseCase: RemoveMemberUseCase
    private lateinit var progressRepository: ProgressRepository
    private lateinit var getSessionProgressUseCase: GetSessionProgressUseCase
    private lateinit var saveProgressUseCase: SaveProgressUseCase
    private lateinit var finishSessionUseCase: FinishSessionUseCase
    private lateinit var toggleSessionParticipationUseCase: ToggleSessionParticipationUseCase
    private lateinit var viewModel: ClubDetailsViewModel

    private val formatDateTime = FormatDateTimeUseCase()
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        clubRepository = mock<ClubRepository>()
        memberRepository = mock<MemberRepository>()
        sessionRepository = mock<SessionRepository>()
        avatarRepository = mock<AvatarRepository>()
        progressRepository = mock<ProgressRepository>()

        // Use REAL UseCases with mocked repositories
        getClubDetails = GetClubDetailsUseCase(clubRepository, formatDateTime)
        getActiveSession = GetActiveSessionUseCase(clubRepository, formatDateTime)
        getClubMembers = GetClubMembersUseCase(clubRepository, avatarRepository)
        getMemberClubs = GetMemberClubsUseCase(memberRepository, clubRepository, avatarRepository)
        createClubUseCase = CreateClubUseCase(clubRepository, memberRepository)
        updateClubUseCase = UpdateClubUseCase(clubRepository)
        deleteClubUseCase = DeleteClubUseCase(clubRepository)
        createSessionUseCase = CreateSessionUseCase(sessionRepository)
        updateSessionUseCase = UpdateSessionUseCase(sessionRepository)
        deleteSessionUseCase = DeleteSessionUseCase(sessionRepository)
        createDiscussionUseCase = CreateDiscussionUseCase(sessionRepository)
        updateDiscussionUseCase = UpdateDiscussionUseCase(sessionRepository)
        deleteDiscussionUseCase = DeleteDiscussionUseCase(sessionRepository)
        updateMemberRoleUseCase = UpdateMemberRoleUseCase(memberRepository)
        removeMemberUseCase = RemoveMemberUseCase(memberRepository)
        getSessionProgressUseCase = GetSessionProgressUseCase(progressRepository)
        saveProgressUseCase = SaveProgressUseCase(progressRepository)
        finishSessionUseCase = FinishSessionUseCase(sessionRepository)
        toggleSessionParticipationUseCase = ToggleSessionParticipationUseCase(sessionRepository)

        viewModel = ClubDetailsViewModel(
            getClubDetails, getActiveSession, getClubMembers, getMemberClubs,
            createClubUseCase,
            updateClubUseCase, deleteClubUseCase, createSessionUseCase,
            updateSessionUseCase, deleteSessionUseCase, createDiscussionUseCase,
            updateDiscussionUseCase, deleteDiscussionUseCase,
            updateMemberRoleUseCase, removeMemberUseCase,
            getSessionProgressUseCase, saveProgressUseCase, finishSessionUseCase,
            toggleSessionParticipationUseCase
        )

        every { avatarRepository.getAvatarUrl(null) } returns null
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns Result.success(emptyList())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -------------------------------------------------------------------------
    // Existing tests (unchanged)
    // -------------------------------------------------------------------------

    @Test
    fun `initial state is loading with no data`() {
        val state = viewModel.state.value
        assertTrue(state.isLoading)
        assertNull(state.error)
        assertNull(state.currentClubDetails)
        assertNull(state.activeSession)
        assertTrue(state.members.isEmpty())
    }

    @Test
    fun `loadClubData updates state with success data from all UseCases`() = runTest {
        val clubId = "club-123"
        val book = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)
        val futureDiscussion = Discussion(
            id = "d1",
            sessionId = "s1",
            title = "Chapter 1",
            date = LocalDateTime(2026, 1, 15, 19, 0),
            location = "Discord"
        )
        val activeSession = Session(
            id = "session-1",
            clubId = clubId,
            book = book,
            dueDate = LocalDateTime(2026, 3, 15, 0, 0),
            discussions = listOf(futureDiscussion)
        )
        val members = listOf(
            ClubMember(role = Role.OWNER, Member(id = "m1", userId = "u1", name = "Alice", booksRead = 5, clubs = null)),
            ClubMember(role = Role.MEMBER, Member(id = "m2", userId = "u2", name = "Bob", booksRead = 3, clubs = null))
        )
        val club = Club(
            id = clubId,
            name = "Test Club",
            serverId = null,
            discordChannel = null,
            members = members,
            activeSession = activeSession,
            pastSessions = emptyList(),
            shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.loadClubData(clubId)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("Test Club", state.currentClubDetails?.clubName)
        assertEquals(2, state.currentClubDetails?.memberCount)
        assertEquals("session-1", state.activeSession?.sessionId)
        assertEquals(2, state.members.size)
        assertEquals("Alice", state.members[0].name)
    }

    @Test
    fun `loadClubData sets loading true initially then false after completion`() = runTest {
        val clubId = "club-123"
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.loadClubData(clubId)

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `loadClubData handles error from repository`() = runTest {
        val clubId = "club-123"
        val errorMessage = "Failed to fetch club"
        everySuspend { clubRepository.getClub(clubId) } returns Result.failure(Exception(errorMessage))

        viewModel.loadClubData(clubId)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        assertNull(state.currentClubDetails)
        assertNull(state.activeSession)
        assertTrue(state.members.isEmpty())
    }

    @Test
    fun `loadClubData handles club with no active session`() = runTest {
        val clubId = "club-123"
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.loadClubData(clubId)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("Test Club", state.currentClubDetails?.clubName)
        assertNull(state.activeSession)
        assertTrue(state.members.isEmpty())
    }

    @Test
    fun `loadClubData calculates member count correctly`() = runTest {
        val clubId = "club-123"
        val members = listOf(
            ClubMember(role = Role.OWNER, Member(id = "m1", userId = "u1", name = "Alice", booksRead = 5, clubs = null)),
            ClubMember(role = Role.OWNER, Member(id = "m2", userId = "u2", name = "Bob", booksRead = 3, clubs = null)),
            ClubMember(role = Role.OWNER, Member(id = "m3", userId = "u3", name = "Charlie", booksRead = 4, clubs = null)),
        )
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = members, activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.loadClubData(clubId)

        assertEquals(3, viewModel.state.value.currentClubDetails?.memberCount)
    }

    @Test
    fun `refresh reloads data with same clubId`() = runTest {
        val clubId = "club-123"
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.loadClubData(clubId)
        viewModel.refresh()

        val refreshedState = viewModel.state.value
        assertEquals(clubId, refreshedState.currentClubDetails?.clubId)
        assertFalse(refreshedState.isLoading)
    }

    @Test
    fun `refresh does nothing when no clubId has been loaded`() = runTest {
        val initialState = viewModel.state.value

        viewModel.refresh()

        val afterRefreshState = viewModel.state.value
        assertEquals(initialState.isLoading, afterRefreshState.isLoading)
        assertEquals(initialState.currentClubDetails, afterRefreshState.currentClubDetails)
    }

    @Test
    fun `loadClubData clears previous error before loading`() = runTest {
        val clubId = "club-123"
        everySuspend { clubRepository.getClub(clubId) } returns Result.failure(Exception("Error"))

        viewModel.loadClubData(clubId)
        assertEquals("Error", viewModel.state.value.error)

        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.loadClubData(clubId)

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `selectClub updates selectedClubId in state`() = runTest {
        val clubId = "club-456"
        val club = Club(
            id = clubId, name = "New Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.selectClub(clubId)

        assertEquals(clubId, viewModel.state.value.selectedClubId)
    }

    @Test
    fun `selectClub triggers data load for the new club`() = runTest {
        val clubId = "club-789"
        val club = Club(
            id = clubId, name = "Another Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.selectClub(clubId)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Another Club", state.currentClubDetails?.clubName)
    }

    @Test
    fun `selectedClubId is set after loadUserClubs completes`() = runTest {
        val userId = "user-1"
        val clubId = "club-123"
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        val member = Member(id = "m1", userId = userId, name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId(userId, forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.loadUserClubs(userId)

        assertNotNull(viewModel.state.value.selectedClubId)
        assertEquals(clubId, viewModel.state.value.selectedClubId)
    }

    @Test
    fun `selectedClubId persists through loading cycles`() = runTest {
        val clubId = "club-123"
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.selectClub(clubId)
        assertEquals(clubId, viewModel.state.value.selectedClubId)

        viewModel.refresh()

        assertEquals(clubId, viewModel.state.value.selectedClubId)
    }

    @Test
    fun `loadClubData handles discussions timeline correctly`() = runTest {
        val clubId = "club-123"
        val book = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)
        val pastDiscussion = Discussion(
            id = "d1", sessionId = "s1", title = "Chapter 1",
            date = LocalDateTime(2024, 1, 1, 19, 0), location = "Discord"
        )
        val futureDiscussion = Discussion(
            id = "d2", sessionId = "s1", title = "Chapter 2",
            date = LocalDateTime(2032, 2, 1, 19, 0), location = "Discord"
        )
        val activeSession = Session(
            id = "session-1", clubId = clubId, book = book,
            dueDate = LocalDateTime(2026, 3, 15, 0, 0),
            discussions = listOf(pastDiscussion, futureDiscussion)
        )
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = activeSession, pastSessions = emptyList(), shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.loadClubData(clubId)

        val timeline = viewModel.state.value.activeSession?.discussions
        assertEquals(2, timeline?.size)
        assertTrue(timeline?.get(0)?.isPast == true)
        assertTrue(timeline?.get(1)?.isNext == true)
    }

    // -------------------------------------------------------------------------
    // New tests — userRole population
    // -------------------------------------------------------------------------

    @Test
    fun `loadUserClubs stores userRole for first club`() = runTest {
        val userId = "user-1"
        val clubId = "club-123"
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = userId, name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId(userId, forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.loadUserClubs(userId)

        assertEquals(Role.OWNER, viewModel.state.value.userRole)
    }

    @Test
    fun `selectClub updates userRole from availableClubs`() = runTest {
        val userId = "user-1"
        val clubId1 = "club-1"
        val clubId2 = "club-2"
        val club1 = Club(
            id = clubId1, name = "Club One", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val club2 = Club(
            id = clubId2, name = "Club Two", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.MEMBER
        )
        val member = Member(id = "m1", userId = userId, name = "Alice", booksRead = 0, clubs = listOf(club1, club2))
        everySuspend { memberRepository.getMemberByUserId(userId, forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId1) } returns Result.success(club1)
        everySuspend { clubRepository.getClub(clubId2) } returns Result.success(club2)

        viewModel.loadUserClubs(userId)
        viewModel.selectClub(clubId2)

        assertEquals(Role.MEMBER, viewModel.state.value.userRole)
    }

    // -------------------------------------------------------------------------
    // New tests — mutation operations
    // -------------------------------------------------------------------------

    @Test
    fun `onUpdateClubName sets operationResult Success on success`() = runTest {
        val clubId = "club-1"
        val updatedClub = Club(
            id = clubId, name = "New Name", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(updatedClub)
        everySuspend { clubRepository.getClub(clubId, forceRefresh = true) } returns Result.success(updatedClub)
        everySuspend { clubRepository.updateClub(clubId = clubId, name = "New Name") } returns Result.success(updatedClub)

        // Load club first so currentClubId and userRole are set
        val club = Club(
            id = clubId, name = "Old Name", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        viewModel.loadUserClubs("u1")

        viewModel.onUpdateClubName("New Name")

        assertIs<OperationResult.Success>(viewModel.state.value.operationResult)
    }

    @Test
    fun `onUpdateClubName sets operationResult Error on failure`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Old Name", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        viewModel.loadUserClubs("u1")

        everySuspend { clubRepository.updateClub(clubId = clubId, name = "New Name") } returns
            Result.failure(RuntimeException("Network error"))

        viewModel.onUpdateClubName("New Name")

        assertIs<OperationResult.Error>(viewModel.state.value.operationResult)
    }

    @Test
    fun `onConsumeOperationResult clears operationResult`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Old Name", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val updatedClub = club.copy(name = "New Name")
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(updatedClub)
        everySuspend { clubRepository.getClub(clubId, forceRefresh = true) } returns Result.success(updatedClub)
        everySuspend { clubRepository.updateClub(clubId = clubId, name = "New Name") } returns Result.success(updatedClub)
        viewModel.loadUserClubs("u1")

        viewModel.onUpdateClubName("New Name")
        assertNotNull(viewModel.state.value.operationResult)

        viewModel.onConsumeOperationResult()

        assertNull(viewModel.state.value.operationResult)
    }

    @Test
    fun `onDeleteSession does nothing when no active session`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        viewModel.loadUserClubs("u1")

        // No active session — deleteSession should be a no-op
        viewModel.onDeleteSession()

        assertNull(viewModel.state.value.operationResult)
    }

    @Test
    fun `onUpdateClubName does nothing when userRole is null`() = runTest {
        // No loadUserClubs — userRole is null
        viewModel.onUpdateClubName("New Name")

        assertNull(viewModel.state.value.operationResult)
    }

    // -------------------------------------------------------------------------
    // Reading progress & end session
    // -------------------------------------------------------------------------

    /** Loads a club with an active session (incl. participants) as [role]. */
    private suspend fun loadClubWithActiveSession(role: Role = Role.OWNER): String {
        val clubId = "club-1"
        val book = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null, pageCount = 200)
        val session = Session(
            id = "session-1",
            clubId = clubId,
            book = book,
            dueDate = LocalDateTime(2026, 3, 15, 0, 0),
            discussions = emptyList(),
            members = listOf(
                SessionMember(memberId = "m1", memberName = "Alice", isReading = true),
                SessionMember(memberId = "m2", memberName = "Bob", isReading = false)
            )
        )
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = session, pastSessions = emptyList(),
            shameList = emptyList(), role = role
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend { clubRepository.getClub(clubId, forceRefresh = true) } returns Result.success(club)
        viewModel.loadUserClubs("u1")
        return clubId
    }

    private fun ownProgress(status: ProgressStatus = ProgressStatus.IN_PROGRESS) = ReadingProgress(
        id = "progress-1",
        memberId = "m1",
        bookId = "book-1",
        sessionId = "session-1",
        type = ProgressType.PAGE,
        status = status,
        currentPage = 50
    )

    @Test
    fun `loadClubData maps session participants into state`() = runTest {
        loadClubWithActiveSession()

        val participants = viewModel.state.value.activeSession?.participants
        assertEquals(2, participants?.size)
        assertEquals("m1", participants?.get(0)?.memberId)
        assertTrue(participants?.get(0)?.isReading == true)
        assertTrue(participants?.get(1)?.isReading == false)
    }

    @Test
    fun `loadClubData populates own progress for the active session`() = runTest {
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns
            Result.success(listOf(ownProgress()))

        loadClubWithActiveSession()

        val progress = viewModel.state.value.ownProgress
        assertNotNull(progress)
        assertEquals("progress-1", progress.progressId)
        assertEquals(25, progress.percent)
        assertEquals("50 of 200 pages", progress.label)
    }

    @Test
    fun `loadClubData leaves own progress null when fetch fails`() = runTest {
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns
            Result.failure(RuntimeException("Network error"))

        loadClubWithActiveSession()

        assertNull(viewModel.state.value.ownProgress)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `onSaveProgress updates own progress in state immediately`() = runTest {
        loadClubWithActiveSession()
        everySuspend {
            progressRepository.createProgress(any(), any(), any(), any(), any())
        } returns Result.success(ownProgress())

        viewModel.onSaveProgress(ProgressType.PAGE, currentPage = 50, percentComplete = null, markFinished = false)

        val state = viewModel.state.value
        assertEquals("progress-1", state.ownProgress?.progressId)
        assertIs<OperationResult.Success>(state.operationResult)
        assertFalse(state.isOperationInProgress)
    }

    @Test
    fun `onSaveProgress with existing entry routes to update`() = runTest {
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns
            Result.success(listOf(ownProgress()))
        loadClubWithActiveSession()
        everySuspend {
            progressRepository.updateProgress(any(), any(), any(), any(), any())
        } returns Result.success(ownProgress(status = ProgressStatus.COMPLETED))

        viewModel.onSaveProgress(ProgressType.PAGE, currentPage = 200, percentComplete = null, markFinished = true)

        assertEquals("Finished", viewModel.state.value.ownProgress?.label)
    }

    @Test
    fun `onSaveProgress failure surfaces error result`() = runTest {
        loadClubWithActiveSession()
        everySuspend {
            progressRepository.createProgress(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("Save failed"))

        viewModel.onSaveProgress(ProgressType.PAGE, currentPage = 50, percentComplete = null, markFinished = false)

        assertIs<OperationResult.Error>(viewModel.state.value.operationResult)
    }

    @Test
    fun `onEndSession surfaces credited count and refreshes`() = runTest {
        loadClubWithActiveSession(role = Role.OWNER)
        everySuspend { sessionRepository.finishSession("session-1") } returns Result.success(2)

        viewModel.onEndSession()

        val result = viewModel.state.value.operationResult
        assertIs<OperationResult.Success>(result)
        assertEquals("Session ended — 2 members credited", result.message)
    }

    @Test
    fun `onEndSession as ADMIN succeeds`() = runTest {
        loadClubWithActiveSession(role = Role.ADMIN)
        everySuspend { sessionRepository.finishSession("session-1") } returns Result.success(1)

        viewModel.onEndSession()

        val result = viewModel.state.value.operationResult
        assertIs<OperationResult.Success>(result)
        assertEquals("Session ended — 1 member credited", result.message)
    }

    @Test
    fun `onEndSession as MEMBER is rejected`() = runTest {
        loadClubWithActiveSession(role = Role.MEMBER)

        viewModel.onEndSession()

        assertIs<OperationResult.Error>(viewModel.state.value.operationResult)
    }

    @Test
    fun `onEndSession failure surfaces error result`() = runTest {
        loadClubWithActiveSession(role = Role.OWNER)
        everySuspend { sessionRepository.finishSession("session-1") } returns
            Result.failure(RuntimeException("Session already finished"))

        viewModel.onEndSession()

        assertIs<OperationResult.Error>(viewModel.state.value.operationResult)
    }
}
