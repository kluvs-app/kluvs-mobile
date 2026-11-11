package com.ivangarzab.kluvs.clubs.presentation

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
import com.ivangarzab.kluvs.clubs.domain.GetClubDetailsUseCase
import com.ivangarzab.kluvs.clubs.domain.GetClubMembersUseCase
import com.ivangarzab.kluvs.clubs.domain.GetCurrentMemberIdUseCase
import com.ivangarzab.kluvs.clubs.domain.GetDiscussionNoteUseCase
import com.ivangarzab.kluvs.clubs.domain.GetMemberClubsUseCase
import com.ivangarzab.kluvs.clubs.domain.LeaveClubUseCase
import com.ivangarzab.kluvs.presentation.progress.GetSessionProgressUseCase
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
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.BookRepository
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.DiscussionAttendanceRepository
import com.ivangarzab.kluvs.data.repositories.DiscussionNoteRepository
import com.ivangarzab.kluvs.data.repositories.DiscussionRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.data.repositories.ProgressRepository
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.AttendanceResponse
import com.ivangarzab.kluvs.model.AttendanceRoster
import com.ivangarzab.kluvs.model.AttendanceStatus
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.BookSearchResult
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.ClubMember
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.DiscussionNote
import com.ivangarzab.kluvs.model.JoinPolicy
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
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import dev.mokkery.answering.calls
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
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
    private lateinit var discussionRepository: DiscussionRepository
    private lateinit var getClubDetails: GetClubDetailsUseCase
    private lateinit var getActiveSession: GetActiveSessionUseCase
    private lateinit var getClubMembers: GetClubMembersUseCase
    private lateinit var getMemberClubs: GetMemberClubsUseCase
    private lateinit var getCurrentMemberId: GetCurrentMemberIdUseCase
    private lateinit var bookRepository: BookRepository
    private lateinit var searchBooksUseCase: SearchBooksUseCase
    private lateinit var registerBookUseCase: RegisterBookUseCase
    private lateinit var createClubUseCase: CreateClubUseCase
    private lateinit var updateClubUseCase: UpdateClubUseCase
    private lateinit var updateJoinPolicyUseCase: UpdateJoinPolicyUseCase
    private lateinit var rotateInviteLinkUseCase: RotateInviteLinkUseCase
    private lateinit var deleteClubUseCase: DeleteClubUseCase
    private lateinit var createSessionUseCase: CreateSessionUseCase
    private lateinit var updateSessionUseCase: UpdateSessionUseCase
    private lateinit var deleteSessionUseCase: DeleteSessionUseCase
    private lateinit var createDiscussionUseCase: CreateDiscussionUseCase
    private lateinit var updateDiscussionUseCase: UpdateDiscussionUseCase
    private lateinit var deleteDiscussionUseCase: DeleteDiscussionUseCase
    private lateinit var updateMemberRoleUseCase: UpdateMemberRoleUseCase
    private lateinit var removeMemberUseCase: RemoveMemberUseCase
    private lateinit var leaveClubUseCase: LeaveClubUseCase
    private lateinit var transferOwnershipUseCase: TransferOwnershipUseCase
    private lateinit var progressRepository: ProgressRepository
    private lateinit var getSessionProgressUseCase: GetSessionProgressUseCase
    private lateinit var saveProgressUseCase: SaveProgressUseCase
    private lateinit var finishSessionUseCase: FinishSessionUseCase
    private lateinit var toggleSessionParticipationUseCase: ToggleSessionParticipationUseCase
    private lateinit var discussionAttendanceRepository: DiscussionAttendanceRepository
    private lateinit var getAttendanceRosterUseCase: GetAttendanceRosterUseCase
    private lateinit var setAttendanceUseCase: SetAttendanceUseCase
    private lateinit var clearAttendanceUseCase: ClearAttendanceUseCase
    private lateinit var discussionNoteRepository: DiscussionNoteRepository
    private lateinit var getDiscussionNoteUseCase: GetDiscussionNoteUseCase
    private lateinit var createDiscussionNoteUseCase: CreateDiscussionNoteUseCase
    private lateinit var updateDiscussionNoteUseCase: UpdateDiscussionNoteUseCase
    private lateinit var deleteDiscussionNoteUseCase: DeleteDiscussionNoteUseCase
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
        discussionAttendanceRepository = mock<DiscussionAttendanceRepository>()
        discussionRepository = mock<DiscussionRepository>()
        discussionNoteRepository = mock<DiscussionNoteRepository>()
        bookRepository = mock<BookRepository>()

        // Use REAL UseCases with mocked repositories
        getClubDetails = GetClubDetailsUseCase(clubRepository, formatDateTime)
        getActiveSession = GetActiveSessionUseCase(clubRepository, formatDateTime)
        getClubMembers = GetClubMembersUseCase(clubRepository, avatarRepository)
        getMemberClubs = GetMemberClubsUseCase(memberRepository, clubRepository, avatarRepository)
        getCurrentMemberId = GetCurrentMemberIdUseCase(memberRepository)
        searchBooksUseCase = SearchBooksUseCase(bookRepository)
        registerBookUseCase = RegisterBookUseCase(bookRepository)
        createClubUseCase = CreateClubUseCase(clubRepository, memberRepository)
        updateClubUseCase = UpdateClubUseCase(clubRepository)
        updateJoinPolicyUseCase = UpdateJoinPolicyUseCase(clubRepository)
        rotateInviteLinkUseCase = RotateInviteLinkUseCase(clubRepository)
        deleteClubUseCase = DeleteClubUseCase(clubRepository)
        createSessionUseCase = CreateSessionUseCase(sessionRepository)
        updateSessionUseCase = UpdateSessionUseCase(sessionRepository)
        deleteSessionUseCase = DeleteSessionUseCase(sessionRepository)
        createDiscussionUseCase = CreateDiscussionUseCase(discussionRepository)
        updateDiscussionUseCase = UpdateDiscussionUseCase(discussionRepository)
        deleteDiscussionUseCase = DeleteDiscussionUseCase(discussionRepository)
        updateMemberRoleUseCase = UpdateMemberRoleUseCase(memberRepository)
        removeMemberUseCase = RemoveMemberUseCase(memberRepository)
        leaveClubUseCase = LeaveClubUseCase(memberRepository)
        transferOwnershipUseCase = TransferOwnershipUseCase(memberRepository)
        getSessionProgressUseCase = GetSessionProgressUseCase(progressRepository)
        saveProgressUseCase = SaveProgressUseCase(progressRepository)
        finishSessionUseCase = FinishSessionUseCase(sessionRepository)
        toggleSessionParticipationUseCase = ToggleSessionParticipationUseCase(sessionRepository)
        getAttendanceRosterUseCase = GetAttendanceRosterUseCase(discussionAttendanceRepository)
        setAttendanceUseCase = SetAttendanceUseCase(discussionAttendanceRepository)
        clearAttendanceUseCase = ClearAttendanceUseCase(discussionAttendanceRepository)
        getDiscussionNoteUseCase = GetDiscussionNoteUseCase(discussionNoteRepository)
        createDiscussionNoteUseCase = CreateDiscussionNoteUseCase(discussionNoteRepository)
        updateDiscussionNoteUseCase = UpdateDiscussionNoteUseCase(discussionNoteRepository)
        deleteDiscussionNoteUseCase = DeleteDiscussionNoteUseCase(discussionNoteRepository)

        viewModel = ClubDetailsViewModel(
            getClubDetails, getActiveSession, getClubMembers, getMemberClubs, getCurrentMemberId,
            searchBooksUseCase, registerBookUseCase,
            createClubUseCase,
            updateClubUseCase, updateJoinPolicyUseCase, rotateInviteLinkUseCase,
            deleteClubUseCase, createSessionUseCase,
            updateSessionUseCase, deleteSessionUseCase, createDiscussionUseCase,
            updateDiscussionUseCase, deleteDiscussionUseCase,
            updateMemberRoleUseCase, removeMemberUseCase,
            leaveClubUseCase, transferOwnershipUseCase,
            getSessionProgressUseCase, saveProgressUseCase, finishSessionUseCase,
            toggleSessionParticipationUseCase,
            getAttendanceRosterUseCase, setAttendanceUseCase, clearAttendanceUseCase,
            getDiscussionNoteUseCase, createDiscussionNoteUseCase,
            updateDiscussionNoteUseCase, deleteDiscussionNoteUseCase
        )

        every { avatarRepository.getAvatarUrl(null) } returns null

        // Default resolution for GetCurrentMemberIdUseCase's independent lookup (fired on
        // every loadClubData call once loadUserClubs has set currentUserId) — most tests don't
        // exercise the participation-toggle/role-change plumbing this feeds, so give it an
        // inert default here rather than repeating it in every test. Individual tests can still
        // override with a more specific stub where they care about the resolved member ID.
        everySuspend { memberRepository.getMemberByUserId(any(), any()) } returns Result.success(
            Member(id = "default-member-id", name = "Current User", userId = "u1")
        )
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
    fun `loadClubData resolves currentMemberId even when the club's member roster omits the signed-in user`() = runTest {
        // Regression test: currentMemberId used to be derived by searching this same club's
        // member roster for the signed-in user's userId — a stale/incomplete roster (missing
        // the signed-in user, e.g. a just-joined member not yet reflected in a cached list)
        // silently hid every membership-gated action (participation toggle, change role, remove
        // member) with no error. It's now resolved via GetCurrentMemberIdUseCase, independent
        // of this club's roster entirely.
        val userId = "u1"
        val clubId = "club-123"
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            // Roster deliberately does NOT include a member with userId == "u1".
            members = listOf(ClubMember(role = Role.MEMBER, Member(id = "m2", userId = "u2", name = "Bob", booksRead = 0, clubs = null))),
            activeSession = null, pastSessions = emptyList(), shameList = emptyList()
        )
        // Overrides the generic setup() default so this test can assert on a distinct ID —
        // any() for forceRefresh since GetMemberClubsUseCase always forces it while
        // GetCurrentMemberIdUseCase uses loadClubData's own (here: default false).
        val signedInMember = Member(id = "current-member-id", userId = userId, name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId(userId, any()) } returns Result.success(signedInMember)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)

        viewModel.loadUserClubs(userId)

        val state = viewModel.state.value
        assertEquals("current-member-id", state.currentMemberId)
        assertTrue(state.members.none { it.userId == userId })
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
        everySuspend { clubRepository.getClub(clubId) } returns Result.failure(Exception("Failed to fetch club"))

        viewModel.loadClubData(clubId)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Something went wrong. Please try again.", state.error)
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
        assertEquals("Something went wrong. Please try again.", viewModel.state.value.error)

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
    fun `onDeleteClub sets deletedClubId and removes the club from availableClubs on success`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Doomed Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend { clubRepository.deleteClub(clubId = clubId) } returns Result.success("Club deleted")
        viewModel.loadUserClubs("u1")

        viewModel.onDeleteClub()

        val state = viewModel.state.value
        assertEquals(clubId, state.deletedClubId)
        assertTrue(state.availableClubs.none { it.id == clubId })
        assertIs<OperationResult.Success>(state.operationResult)
    }

    @Test
    fun `onConsumeDeletedClubId clears deletedClubId`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Doomed Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend { clubRepository.deleteClub(clubId = clubId) } returns Result.success("Club deleted")
        viewModel.loadUserClubs("u1")
        viewModel.onDeleteClub()

        viewModel.onConsumeDeletedClubId()

        assertEquals(null, viewModel.state.value.deletedClubId)
    }

    @Test
    fun `onLeaveClub sets leftClubId and removes the club from availableClubs on success`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Some Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.MEMBER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        // state.currentMemberId resolves via the generic getMemberByUserId(any(), any()) stub in
        // setup(), which yields id "default-member-id" — that's the memberId LeaveClubUseCase acts on.
        everySuspend { memberRepository.getMember("default-member-id", forceRefresh = true) } returns Result.success(
            Member(id = "default-member-id", userId = "u1", name = "Current User", booksRead = 0, clubs = listOf(club))
        )
        everySuspend { memberRepository.updateMember(memberId = "default-member-id", clubIds = emptyList()) } returns
            Result.success(Member(id = "default-member-id", userId = "u1", name = "Current User", booksRead = 0, clubs = emptyList()))
        viewModel.loadUserClubs("u1")

        viewModel.onLeaveClub()

        val state = viewModel.state.value
        assertEquals(clubId, state.leftClubId)
        assertTrue(state.availableClubs.none { it.id == clubId })
        assertIs<OperationResult.Success>(state.operationResult)
    }

    @Test
    fun `onLeaveClub sets operationResult Error on failure`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Some Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.MEMBER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend { memberRepository.getMember("default-member-id", forceRefresh = true) } returns
            Result.failure(RuntimeException("Not found"))
        viewModel.loadUserClubs("u1")

        viewModel.onLeaveClub()

        val state = viewModel.state.value
        assertEquals(null, state.leftClubId)
        assertIs<OperationResult.Error>(state.operationResult)
    }

    @Test
    fun `onConsumeLeftClubId clears leftClubId`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Some Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.MEMBER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend { memberRepository.getMember("default-member-id", forceRefresh = true) } returns Result.success(
            Member(id = "default-member-id", userId = "u1", name = "Current User", booksRead = 0, clubs = listOf(club))
        )
        everySuspend { memberRepository.updateMember(memberId = "default-member-id", clubIds = emptyList()) } returns
            Result.success(Member(id = "default-member-id", userId = "u1", name = "Current User", booksRead = 0, clubs = emptyList()))
        viewModel.loadUserClubs("u1")
        viewModel.onLeaveClub()

        viewModel.onConsumeLeftClubId()

        assertEquals(null, viewModel.state.value.leftClubId)
    }

    @Test
    fun `onTransferOwnership sets operationResult Success on success`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Some Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend { clubRepository.getClub(clubId, forceRefresh = true) } returns Result.success(club)
        // currentOwnerId is state.currentMemberId, resolved to "default-member-id" per setup()'s
        // generic getMemberByUserId(any(), any()) stub.
        everySuspend {
            memberRepository.updateMember(memberId = "m2", clubRoles = mapOf(clubId to "owner"))
        } returns Result.success(Member(id = "m2", userId = "u2", name = "Bob", booksRead = 0))
        everySuspend {
            memberRepository.updateMember(memberId = "default-member-id", clubRoles = mapOf(clubId to "admin"))
        } returns Result.success(Member(id = "default-member-id", userId = "u1", name = "Current User", booksRead = 0))
        viewModel.loadUserClubs("u1")

        viewModel.onTransferOwnership("m2")

        assertIs<OperationResult.Success>(viewModel.state.value.operationResult)
    }

    @Test
    fun `onTransferOwnership sets operationResult Error on failure`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Some Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend {
            memberRepository.updateMember(memberId = "m2", clubRoles = mapOf(clubId to "owner"))
        } returns Result.failure(RuntimeException("Network error"))
        viewModel.loadUserClubs("u1")

        viewModel.onTransferOwnership("m2")

        assertIs<OperationResult.Error>(viewModel.state.value.operationResult)
    }

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
    fun `onBookSearchQueryChange updates query only`() = runTest {
        viewModel.onBookSearchQueryChange("hobbit")

        assertEquals("hobbit", viewModel.state.value.bookSearchQuery)
        assertTrue(viewModel.state.value.bookSearchResults.isEmpty())
    }

    @Test
    fun `onSearchBooks with blank query clears results without calling repository`() = runTest {
        viewModel.onSearchBooks("   ")

        assertTrue(viewModel.state.value.bookSearchResults.isEmpty())
        assertFalse(viewModel.state.value.isSearchingBooks)
        verifySuspend(VerifyMode.not) { bookRepository.searchBooks(any(), any()) }
    }

    @Test
    fun `onSearchBooks success populates results`() = runTest {
        val book = Book(id = "1", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = null)
        everySuspend { bookRepository.searchBooks("hobbit", any()) } returns
            Result.success(BookSearchResult(books = listOf(book), total = 1))

        viewModel.onSearchBooks("hobbit")

        assertEquals(listOf(book), viewModel.state.value.bookSearchResults)
        assertFalse(viewModel.state.value.isSearchingBooks)
        assertNull(viewModel.state.value.bookSearchError)
    }

    @Test
    fun `onSearchBooks failure sets error and clears results`() = runTest {
        everySuspend { bookRepository.searchBooks("hobbit", any()) } returns
            Result.failure(Exception("Network error"))

        viewModel.onSearchBooks("hobbit")

        assertTrue(viewModel.state.value.bookSearchResults.isEmpty())
        assertNotNull(viewModel.state.value.bookSearchError)
    }

    @Test
    fun `onSelectBook registers book and stores the registered result`() = runTest {
        val searchResult = Book(id = "", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = null)
        val registered = searchResult.copy(id = "42")
        everySuspend { bookRepository.registerBook(searchResult) } returns Result.success(registered)

        viewModel.onSelectBook(searchResult)

        assertEquals(registered, viewModel.state.value.selectedBook)
        assertTrue(viewModel.state.value.bookSearchResults.isEmpty())
        assertFalse(viewModel.state.value.isRegisteringBook)
    }

    @Test
    fun `onSelectBook failure sets error without setting selectedBook`() = runTest {
        val searchResult = Book(id = "", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = null)
        everySuspend { bookRepository.registerBook(searchResult) } returns Result.failure(Exception("Network error"))

        viewModel.onSelectBook(searchResult)

        assertNull(viewModel.state.value.selectedBook)
        assertNotNull(viewModel.state.value.bookSearchError)
    }

    @Test
    fun `onClearSelectedBook resets selection and query`() = runTest {
        val registered = Book(id = "42", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = null)
        everySuspend { bookRepository.registerBook(any()) } returns Result.success(registered)
        viewModel.onSelectBook(registered.copy(id = ""))

        viewModel.onClearSelectedBook()

        assertNull(viewModel.state.value.selectedBook)
        assertEquals("", viewModel.state.value.bookSearchQuery)
        assertTrue(viewModel.state.value.bookSearchResults.isEmpty())
    }

    @Test
    fun `onResetBookSearch clears all book search state`() = runTest {
        val book = Book(id = "1", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = null)
        everySuspend { bookRepository.searchBooks("hobbit", any()) } returns
            Result.success(BookSearchResult(books = listOf(book), total = 1))
        viewModel.onBookSearchQueryChange("hobbit")
        viewModel.onSearchBooks("hobbit")

        viewModel.onResetBookSearch()

        val state = viewModel.state.value
        assertEquals("", state.bookSearchQuery)
        assertTrue(state.bookSearchResults.isEmpty())
        assertFalse(state.isSearchingBooks)
        assertNull(state.bookSearchError)
        assertNull(state.selectedBook)
        assertFalse(state.isRegisteringBook)
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

    @Test
    fun `onUpdateJoinPolicy sets operationResult Success on success`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val updatedClub = club.copy(joinPolicy = JoinPolicy.INVITE_LINK, inviteToken = "tok-1")
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend { clubRepository.getClub(clubId, forceRefresh = true) } returns Result.success(updatedClub)
        everySuspend {
            clubRepository.updateClub(clubId = clubId, joinPolicy = JoinPolicy.INVITE_LINK)
        } returns Result.success(updatedClub)
        viewModel.loadUserClubs("u1")

        viewModel.onUpdateJoinPolicy(JoinPolicy.INVITE_LINK)

        assertIs<OperationResult.Success>(viewModel.state.value.operationResult)
        assertEquals(JoinPolicy.INVITE_LINK, viewModel.state.value.currentClubDetails?.joinPolicy)
        assertEquals("tok-1", viewModel.state.value.currentClubDetails?.inviteToken)
    }

    @Test
    fun `onUpdateJoinPolicy sets operationResult Error on failure`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        viewModel.loadUserClubs("u1")

        everySuspend {
            clubRepository.updateClub(clubId = clubId, joinPolicy = JoinPolicy.PRIVATE)
        } returns Result.failure(RuntimeException("Network error"))

        viewModel.onUpdateJoinPolicy(JoinPolicy.PRIVATE)

        assertIs<OperationResult.Error>(viewModel.state.value.operationResult)
    }

    @Test
    fun `onUpdateJoinPolicy does nothing when userRole is null`() = runTest {
        viewModel.onUpdateJoinPolicy(JoinPolicy.INVITE_LINK)

        assertNull(viewModel.state.value.operationResult)
    }

    @Test
    fun `onRotateInviteLink sets operationResult Success on success`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val rotatedClub = club.copy(joinPolicy = JoinPolicy.INVITE_LINK, inviteToken = "tok-new")
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend { clubRepository.getClub(clubId, forceRefresh = true) } returns Result.success(rotatedClub)
        everySuspend {
            clubRepository.updateClub(clubId = clubId, joinPolicy = JoinPolicy.PRIVATE)
        } returns Result.success(club.copy(joinPolicy = JoinPolicy.PRIVATE, inviteToken = null))
        everySuspend {
            clubRepository.updateClub(clubId = clubId, joinPolicy = JoinPolicy.INVITE_LINK)
        } returns Result.success(rotatedClub)
        viewModel.loadUserClubs("u1")

        viewModel.onRotateInviteLink()

        assertIs<OperationResult.Success>(viewModel.state.value.operationResult)
        assertEquals("tok-new", viewModel.state.value.currentClubDetails?.inviteToken)
    }

    @Test
    fun `onRotateInviteLink sets a distinct error when reactivation fails after deactivation succeeds`() = runTest {
        val clubId = "club-1"
        val club = Club(
            id = clubId, name = "Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = null, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend {
            clubRepository.updateClub(clubId = clubId, joinPolicy = JoinPolicy.PRIVATE)
        } returns Result.success(club.copy(joinPolicy = JoinPolicy.PRIVATE, inviteToken = null))
        everySuspend {
            clubRepository.updateClub(clubId = clubId, joinPolicy = JoinPolicy.INVITE_LINK)
        } returns Result.failure(RuntimeException("Network error"))
        viewModel.loadUserClubs("u1")

        viewModel.onRotateInviteLink()

        val result = viewModel.state.value.operationResult
        assertIs<OperationResult.Error>(result)
        assertEquals("Invite link deactivated but rotation failed — try again", result.message)
    }

    @Test
    fun `onRotateInviteLink does nothing when userRole is null`() = runTest {
        viewModel.onRotateInviteLink()

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

    // -------------------------------------------------------------------------
    // Attendance operations
    // -------------------------------------------------------------------------

    /** Loads a club with an active session containing one discussion (id "d1"). */
    private suspend fun loadClubWithDiscussion(): String {
        val clubId = "club-1"
        val book = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)
        val discussion = Discussion(
            id = "d1", sessionId = "session-1", title = "Chapter 1",
            date = LocalDateTime(2032, 1, 1, 19, 0), location = "Discord"
        )
        val session = Session(
            id = "session-1", clubId = clubId, book = book,
            dueDate = LocalDateTime(2032, 3, 15, 0, 0),
            discussions = listOf(discussion)
        )
        val club = Club(
            id = clubId, name = "Test Club", serverId = null, discordChannel = null,
            members = emptyList(), activeSession = session, pastSessions = emptyList(),
            shameList = emptyList(), role = Role.OWNER
        )
        val member = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0, clubs = listOf(club))
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        everySuspend { clubRepository.getClub(clubId) } returns Result.success(club)
        everySuspend { clubRepository.getClub(clubId, forceRefresh = true) } returns Result.success(club)
        viewModel.loadUserClubs("u1")
        return clubId
    }

    private fun roster(myStatus: AttendanceStatus?) = AttendanceRoster(
        responses = listOf(AttendanceResponse(memberId = "m1", name = "Alice", status = AttendanceStatus.YES)),
        myStatus = myStatus,
        totalMembers = 3
    )

    @Test
    fun `onLoadAttendanceRoster stores roster in state`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionAttendanceRepository.getRoster("d1") } returns Result.success(roster(null))

        viewModel.onLoadAttendanceRoster("d1")

        assertEquals(roster(null), viewModel.state.value.discussionRosters["d1"])
    }

    @Test
    fun `onLoadAttendanceRoster does not refetch when already cached`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionAttendanceRepository.getRoster("d1") } returns Result.success(roster(null))
        viewModel.onLoadAttendanceRoster("d1")

        viewModel.onLoadAttendanceRoster("d1")

        verifySuspend(VerifyMode.exactly(1)) {
            discussionAttendanceRepository.getRoster("d1")
        }
    }

    @Test
    fun `onSetAttendance optimistically updates myStatus then refreshes roster`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionAttendanceRepository.getRoster("d1") } returns Result.success(roster(null))
        viewModel.onLoadAttendanceRoster("d1")
        everySuspend { discussionAttendanceRepository.setAttendance("d1", AttendanceStatus.YES) } returns
            Result.success(AttendanceStatus.YES)
        everySuspend { discussionAttendanceRepository.getRoster("d1") } returns Result.success(roster(AttendanceStatus.YES))

        viewModel.onSetAttendance("d1", AttendanceStatus.YES)

        assertEquals(AttendanceStatus.YES, viewModel.state.value.discussionRosters["d1"]?.myStatus)
    }

    @Test
    fun `onSetAttendance re-selecting current status clears it`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionAttendanceRepository.getRoster("d1") } returns Result.success(roster(AttendanceStatus.YES))
        viewModel.onLoadAttendanceRoster("d1")
        everySuspend { discussionAttendanceRepository.clearAttendance("d1") } returns Result.success(Unit)
        everySuspend { discussionAttendanceRepository.getRoster("d1") } returns Result.success(roster(null))

        viewModel.onSetAttendance("d1", AttendanceStatus.YES)

        verifySuspend { discussionAttendanceRepository.clearAttendance("d1") }
        assertNull(viewModel.state.value.discussionRosters["d1"]?.myStatus)
    }

    @Test
    fun `onSetAttendance rolls back myStatus on failure`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionAttendanceRepository.getRoster("d1") } returns Result.success(roster(null))
        viewModel.onLoadAttendanceRoster("d1")
        everySuspend { discussionAttendanceRepository.setAttendance("d1", AttendanceStatus.NO) } returns
            Result.failure(RuntimeException("Network error"))

        viewModel.onSetAttendance("d1", AttendanceStatus.NO)

        assertNull(viewModel.state.value.discussionRosters["d1"]?.myStatus)
        assertIs<OperationResult.Error>(viewModel.state.value.operationResult)
    }

    // -------------------------------------------------------------------------
    // Discussion note operations
    // -------------------------------------------------------------------------

    private fun note(content: String = "Great chapter") = DiscussionNote(
        id = "n1", discussionId = "d1", memberId = "m1", content = content
    )

    @Test
    fun `onLoadDiscussionNote stores note in state`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionNoteRepository.getNote("d1") } returns Result.success(note())

        viewModel.onLoadDiscussionNote("d1")

        val info = viewModel.state.value.discussionNotes["d1"]
        assertEquals("n1", info?.noteId)
        assertEquals("Great chapter", info?.content)
    }

    @Test
    fun `onLoadDiscussionNote stores empty entry when no note exists`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionNoteRepository.getNote("d1") } returns Result.success(null)

        viewModel.onLoadDiscussionNote("d1")

        val info = viewModel.state.value.discussionNotes["d1"]
        assertNull(info?.noteId)
        assertEquals("", info?.content)
    }

    @Test
    fun `onLoadDiscussionNote surfaces error on failure`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionNoteRepository.getNote("d1") } returns
            Result.failure(RuntimeException("Network error"))

        viewModel.onLoadDiscussionNote("d1")

        assertNotNull(viewModel.state.value.discussionNotes["d1"]?.error)
    }

    @Test
    fun `onLoadDiscussionNote does not refetch when already cached`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionNoteRepository.getNote("d1") } returns Result.success(note())
        viewModel.onLoadDiscussionNote("d1")

        viewModel.onLoadDiscussionNote("d1")

        verifySuspend(VerifyMode.exactly(1)) { discussionNoteRepository.getNote("d1") }
    }

    @Test
    fun `onSaveDiscussionNote creates a note when none exists yet`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionNoteRepository.getNote("d1") } returns Result.success(null)
        viewModel.onLoadDiscussionNote("d1")
        everySuspend { discussionNoteRepository.createNote("d1", "New thoughts") } returns
            Result.success(note(content = "New thoughts"))

        viewModel.onSaveDiscussionNote("d1", "New thoughts")

        val info = viewModel.state.value.discussionNotes["d1"]
        assertEquals("n1", info?.noteId)
        assertEquals("New thoughts", info?.content)
        assertFalse(info?.isSaving == true)
    }

    @Test
    fun `onSaveDiscussionNote updates an existing note`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionNoteRepository.getNote("d1") } returns Result.success(note())
        viewModel.onLoadDiscussionNote("d1")
        everySuspend { discussionNoteRepository.updateNote("n1", "Updated thoughts") } returns
            Result.success(note(content = "Updated thoughts"))

        viewModel.onSaveDiscussionNote("d1", "Updated thoughts")

        assertEquals("Updated thoughts", viewModel.state.value.discussionNotes["d1"]?.content)
        verifySuspend { discussionNoteRepository.updateNote("n1", "Updated thoughts") }
    }

    @Test
    fun `onSaveDiscussionNote surfaces error and keeps prior content on failure`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionNoteRepository.getNote("d1") } returns Result.success(note())
        viewModel.onLoadDiscussionNote("d1")
        everySuspend { discussionNoteRepository.updateNote("n1", "Updated thoughts") } returns
            Result.failure(RuntimeException("Save failed"))

        viewModel.onSaveDiscussionNote("d1", "Updated thoughts")

        val info = viewModel.state.value.discussionNotes["d1"]
        assertNotNull(info?.error)
        assertEquals("Great chapter", info?.content)
    }

    @Test
    fun `onDeleteDiscussionNote resets entry to empty on success`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionNoteRepository.getNote("d1") } returns Result.success(note())
        viewModel.onLoadDiscussionNote("d1")
        everySuspend { discussionNoteRepository.deleteNote("n1") } returns Result.success(Unit)

        viewModel.onDeleteDiscussionNote("d1")

        val info = viewModel.state.value.discussionNotes["d1"]
        assertNull(info?.noteId)
        assertEquals("", info?.content)
    }

    @Test
    fun `onDeleteDiscussionNote does nothing when no note exists`() = runTest {
        loadClubWithDiscussion()
        everySuspend { discussionNoteRepository.getNote("d1") } returns Result.success(null)
        viewModel.onLoadDiscussionNote("d1")

        viewModel.onDeleteDiscussionNote("d1")

        verifySuspend(VerifyMode.not) { discussionNoteRepository.deleteNote(any()) }
    }

    // -------------------------------------------------------------------------
    // KT-322: club list reloads must not override the user's selection
    // -------------------------------------------------------------------------

    private fun twoClubsMember(userId: String = "u1"): Member {
        val clubA = Club(id = "club-a", name = "Club A", members = emptyList())
        val clubB = Club(id = "club-b", name = "Club B", members = emptyList())
        everySuspend { clubRepository.getClub("club-a") } returns Result.success(clubA)
        everySuspend { clubRepository.getClub("club-b") } returns Result.success(clubB)
        everySuspend { clubRepository.getClub("club-a", null, true) } returns Result.success(clubA)
        everySuspend { clubRepository.getClub("club-b", null, true) } returns Result.success(clubB)
        return Member(id = "m1", userId = userId, name = "Alice", clubs = listOf(clubA, clubB))
    }

    @Test
    fun `loadUserClubs keeps the selected club when it is still in the list`() = runTest {
        val member = twoClubsMember()
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)

        viewModel.loadUserClubs("u1")
        viewModel.selectClub("club-b")
        viewModel.loadUserClubs("u1") // tab re-entry

        assertEquals("club-b", viewModel.state.value.selectedClubId)
        assertEquals("Club B", viewModel.state.value.currentClubDetails?.clubName)
    }

    @Test
    fun `selectClub during an in-flight loadUserClubs wins over the auto-select`() = runTest {
        val member = twoClubsMember()
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } calls {
            delay(1_000)
            Result.success(member)
        }

        viewModel.loadUserClubs("u1")
        viewModel.selectClub("club-b") // user taps B while the list is still loading
        advanceUntilIdle()

        assertEquals("club-b", viewModel.state.value.selectedClubId)
        assertEquals("Club B", viewModel.state.value.currentClubDetails?.clubName)
        assertEquals(2, viewModel.state.value.availableClubs.size)
    }

    @Test
    fun `a newer loadUserClubs cancels the older one so the latest list wins`() = runTest {
        val staleMember = Member(id = "m1", userId = "u1", name = "Alice", clubs = listOf(Club(id = "club-old", name = "Old")))
        val freshMember = twoClubsMember()
        var calls = 0
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } calls {
            if (calls++ == 0) { delay(1_000); Result.success(staleMember) } else Result.success(freshMember)
        }

        viewModel.loadUserClubs("u1")
        viewModel.loadUserClubs("u1")
        advanceUntilIdle()

        assertEquals(listOf("club-a", "club-b"), viewModel.state.value.availableClubs.map { it.id })
        assertEquals("club-a", viewModel.state.value.selectedClubId)
        verifySuspend(VerifyMode.not) { clubRepository.getClub("club-old") }
    }

    @Test
    fun `loadUserClubs falls back to the first club when the selection is gone from the list`() = runTest {
        val member = twoClubsMember()
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        viewModel.loadUserClubs("u1")
        viewModel.selectClub("club-b")

        val onlyA = member.copy(clubs = member.clubs!!.filter { it.id == "club-a" })
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(onlyA)
        viewModel.loadUserClubs("u1")

        assertEquals("club-a", viewModel.state.value.selectedClubId)
        assertEquals("Club A", viewModel.state.value.currentClubDetails?.clubName)
    }

    @Test
    fun `loadUserClubs with forceRefresh reloads the selected club rather than the first one`() = runTest {
        val member = twoClubsMember()
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        viewModel.loadUserClubs("u1")
        viewModel.selectClub("club-b")

        viewModel.loadUserClubs("u1", forceRefresh = true)

        assertEquals("club-b", viewModel.state.value.selectedClubId)
    }

    @Test
    fun `loadUserClubs does not show the loader or an error over a club already on screen`() = runTest {
        val member = twoClubsMember()
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(member)
        viewModel.loadUserClubs("u1")
        viewModel.selectClub("club-b")

        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.failure(Exception("offline"))
        viewModel.loadUserClubs("u1")

        assertEquals("club-b", viewModel.state.value.selectedClubId)
        assertEquals("Club B", viewModel.state.value.currentClubDetails?.clubName)
        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(null, viewModel.state.value.error)
    }

    // -------------------------------------------------------------------------
    // KT-323: a failed forced refresh never removes content from the screen
    // -------------------------------------------------------------------------

    @Test
    fun `a failed forced refresh keeps the club on screen and reports via snackbar`() = runTest {
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(twoClubsMember())
        viewModel.loadUserClubs("u1")
        viewModel.selectClub("club-b")
        assertEquals("Club B", viewModel.state.value.currentClubDetails?.clubName)

        everySuspend { clubRepository.getClub("club-b", null, true) } returns Result.failure(Exception("offline"))
        viewModel.refresh(forceRefresh = true)

        val state = viewModel.state.value
        assertEquals("Club B", state.currentClubDetails?.clubName)
        assertEquals(null, state.error)
        assertEquals(false, state.isLoading)
        assertIs<OperationResult.Error>(state.operationResult)
    }

    @Test
    fun `a failed first load still shows the error page`() = runTest {
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns Result.success(twoClubsMember())
        everySuspend { clubRepository.getClub("club-a") } returns Result.failure(Exception("offline"))

        viewModel.loadUserClubs("u1")

        assertEquals(null, viewModel.state.value.currentClubDetails)
        assertNotNull(viewModel.state.value.error)
    }

    // -------------------------------------------------------------------------
    // KT-323 follow-ups: list row patch and in-progress indicator
    // -------------------------------------------------------------------------

    private fun ownedClub(clubId: String, name: String) = Club(
        id = clubId, name = name, serverId = null, discordChannel = null,
        members = emptyList(), activeSession = null, pastSessions = emptyList(),
        shameList = emptyList(), role = Role.OWNER
    )

    @Test
    fun `renaming the club updates its row in the clubs list`() = runTest {
        val old = ownedClub("club-1", "Old Name")
        val renamed = ownedClub("club-1", "New Name")
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns
            Result.success(Member(id = "m1", userId = "u1", name = "Alice", clubs = listOf(old)))
        everySuspend { clubRepository.getClub("club-1") } returns Result.success(old)
        everySuspend { clubRepository.getClub("club-1", forceRefresh = true) } returns Result.success(renamed)
        everySuspend { clubRepository.updateClub(clubId = "club-1", name = "New Name") } returns Result.success(renamed)
        viewModel.loadUserClubs("u1")

        viewModel.onUpdateClubName("New Name")

        assertEquals(listOf("New Name"), viewModel.state.value.availableClubs.map { it.name })
    }

    @Test
    fun `the in-progress indicator stays on until the post-mutation refetch finishes`() = runTest {
        val old = ownedClub("club-1", "Old Name")
        val renamed = ownedClub("club-1", "New Name")
        everySuspend { memberRepository.getMemberByUserId("u1", forceRefresh = true) } returns
            Result.success(Member(id = "m1", userId = "u1", name = "Alice", clubs = listOf(old)))
        everySuspend { clubRepository.getClub("club-1") } returns Result.success(old)
        everySuspend { clubRepository.getClub("club-1", forceRefresh = true) } calls {
            delay(1_000)
            Result.success(renamed)
        }
        everySuspend { clubRepository.updateClub(clubId = "club-1", name = "New Name") } returns Result.success(renamed)
        viewModel.loadUserClubs("u1")

        viewModel.onUpdateClubName("New Name")

        // The success message is already visible, but the refetch is still running
        assertIs<OperationResult.Success>(viewModel.state.value.operationResult)
        assertEquals(true, viewModel.state.value.isOperationInProgress)

        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isOperationInProgress)
        assertEquals("New Name", viewModel.state.value.currentClubDetails?.clubName)
    }
}
