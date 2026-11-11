package com.ivangarzab.kluvs.member.presentation

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.data.repositories.ProgressRepository
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.BookSummary
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.ClubPreview
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingLog
import com.ivangarzab.kluvs.model.ReadingLogEntry
import com.ivangarzab.kluvs.model.ReadingProgress
import com.ivangarzab.kluvs.model.Session
import com.ivangarzab.kluvs.auth.domain.SignOutUseCase
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.database.dao.BookDao
import com.ivangarzab.kluvs.database.dao.ClubDao
import com.ivangarzab.kluvs.database.dao.DiscussionAttendanceDao
import com.ivangarzab.kluvs.database.dao.DiscussionDao
import com.ivangarzab.kluvs.database.dao.DiscussionNoteDao
import com.ivangarzab.kluvs.database.dao.LikeDao
import com.ivangarzab.kluvs.database.dao.MemberDao
import com.ivangarzab.kluvs.database.dao.ProgressDao
import com.ivangarzab.kluvs.database.dao.ServerDao
import com.ivangarzab.kluvs.database.dao.SessionDao
import com.ivangarzab.kluvs.database.dao.ShelfDao
import com.ivangarzab.kluvs.member.domain.GetCurrentUserProfileUseCase
import com.ivangarzab.kluvs.member.domain.GetOnYourShelfUseCase
import com.ivangarzab.kluvs.member.domain.GetReadingLogUseCase
import com.ivangarzab.kluvs.member.domain.GetUserStatisticsUseCase
import com.ivangarzab.kluvs.presentation.progress.GetSessionProgressUseCase
import com.ivangarzab.kluvs.presentation.progress.SaveProgressUseCase
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
import dev.mokkery.answering.calls
import kotlinx.coroutines.delay
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MeViewModelTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var clubRepository: ClubRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var avatarRepository: AvatarRepository
    private lateinit var database: KluvsDatabase
    private lateinit var getCurrentUserProfile: GetCurrentUserProfileUseCase
    private lateinit var getUserStatistics: GetUserStatisticsUseCase
    private lateinit var getOnYourShelf: GetOnYourShelfUseCase
    private lateinit var getReadingLog: GetReadingLogUseCase
    private lateinit var saveProgressUseCase: SaveProgressUseCase
    private lateinit var signOut: SignOutUseCase
    private lateinit var viewModel: MeViewModel

    private val formatDateTime = FormatDateTimeUseCase()
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        memberRepository = mock<MemberRepository>()
        clubRepository = mock<ClubRepository>()
        sessionRepository = mock<SessionRepository>()
        progressRepository = mock<ProgressRepository>()
        authRepository = mock<AuthRepository>()
        avatarRepository = mock<AvatarRepository>()

        // Set up mock DAOs that SignOutUseCase will call
        val clubDao = mock<ClubDao>()
        val serverDao = mock<ServerDao>()
        val memberDao = mock<MemberDao>()
        val sessionDao = mock<SessionDao>()
        val bookDao = mock<BookDao>()
        val discussionDao = mock<DiscussionDao>()
        val shelfDao = mock<ShelfDao>()
        val likeDao = mock<LikeDao>()
        val progressDao = mock<ProgressDao>()
        val discussionNoteDao = mock<DiscussionNoteDao>()
        val discussionAttendanceDao = mock<DiscussionAttendanceDao>()

        database = mock<KluvsDatabase>()
        every { database.clubDao() } returns clubDao
        every { database.serverDao() } returns serverDao
        every { database.memberDao() } returns memberDao
        every { database.sessionDao() } returns sessionDao
        every { database.bookDao() } returns bookDao
        every { database.discussionDao() } returns discussionDao
        every { database.shelfDao() } returns shelfDao
        every { database.likeDao() } returns likeDao
        every { database.progressDao() } returns progressDao
        every { database.discussionNoteDao() } returns discussionNoteDao
        every { database.discussionAttendanceDao() } returns discussionAttendanceDao

        // Mock the suspend delete methods to return Unit
        everySuspend { clubDao.deleteAll() } returns Unit
        everySuspend { serverDao.deleteAll() } returns Unit
        everySuspend { memberDao.deleteAll() } returns Unit
        everySuspend { memberDao.deleteAllCrossRefs() } returns Unit
        everySuspend { sessionDao.deleteAll() } returns Unit
        everySuspend { bookDao.deleteAll() } returns Unit
        everySuspend { discussionDao.deleteAll() } returns Unit
        everySuspend { shelfDao.deleteAll() } returns Unit
        everySuspend { likeDao.deleteAll() } returns Unit
        everySuspend { progressDao.deleteAll() } returns Unit
        everySuspend { discussionNoteDao.deleteAll() } returns Unit
        everySuspend { discussionAttendanceDao.deleteAll() } returns Unit

        // Use REAL UseCases with mocked repositories
        getCurrentUserProfile = GetCurrentUserProfileUseCase(memberRepository, formatDateTime, avatarRepository)
        getUserStatistics = GetUserStatisticsUseCase(memberRepository)
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns Result.success(emptyList())
        getOnYourShelf = GetOnYourShelfUseCase(memberRepository, clubRepository, GetSessionProgressUseCase(progressRepository), formatDateTime)
        getReadingLog = GetReadingLogUseCase(sessionRepository)
        saveProgressUseCase = SaveProgressUseCase(progressRepository)
        signOut = SignOutUseCase(authRepository, database)

        viewModel = MeViewModel(
            getCurrentUserProfile,
            getUserStatistics,
            getOnYourShelf,
            getReadingLog,
            saveProgressUseCase,
            signOut
        )

        every { avatarRepository.getAvatarUrl(null) } returns null

    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading with no data`() {
        // Then
        val state = viewModel.state.value
        assertTrue(state.isLoading)
        assertNull(state.error)
        assertNull(state.profile)
        assertNull(state.statistics)
        assertTrue(state.shelf.isEmpty())
    }

    @Test
    fun `loadUserData updates state with success data from all UseCases`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-1",
            userId = userId,
            name = "Alice Johnson",
            booksRead = 12,
            clubs = listOf(
                Club(id = "club-1", name = "Fantasy Readers", role = null),
                Club(id = "club-2", name = "Sci-Fi Club", role = null),
                Club(id = "club-3", name = "Mystery Book Club", role = null)
            )
        )

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        val book1 = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)
        val book2 = Book("book-2", "Dune", "Herbert", null, 1965, null)

        val session1 = Session(id = "s1", clubId = "club-1", book = book1, dueDate = LocalDateTime(2027, 3, 15, 0, 0), discussions = emptyList())
        val session2 = Session(id = "s2", clubId = "club-2", book = book2, dueDate = LocalDateTime(2027, 4, 1, 0, 0), discussions = emptyList())

        val club1 = Club(id = "club-1", name = "Fantasy Readers", activeSession = session1)
        val club2 = Club(id = "club-2", name = "Sci-Fi Club", activeSession = session2)
        val club3 = Club(id = "club-3", name = "Mystery Book Club")

        everySuspend { clubRepository.getClub("club-1") } returns Result.success(club1)
        everySuspend { clubRepository.getClub("club-2") } returns Result.success(club2)
        everySuspend { clubRepository.getClub("club-3") } returns Result.success(club3)

        // When
        viewModel.loadUserData(userId)
        testScheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)

        // Profile
        assertEquals("member-1", state.profile?.memberId)
        assertEquals("Alice Johnson", state.profile?.name)
        assertEquals("alicejohnson", state.profile?.handle)

        // Statistics
        assertEquals(3, state.statistics?.clubsCount)
        assertEquals(12, state.statistics?.booksRead)

        // Shelf — keeps the order of member.clubs as returned by the backend (no client-side sorting)
        assertEquals(2, state.shelf.size)
        assertEquals("The Hobbit", state.shelf[0].bookTitle)
        assertEquals("Fantasy Readers", state.shelf[0].clubName)
        assertEquals("Dune", state.shelf[1].bookTitle)
        assertEquals("Sci-Fi Club", state.shelf[1].clubName)
    }

    @Test
    fun `loadUserData handles error from member repository`() = runTest {
        // Given
        val userId = "user-123"
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.failure(Exception("Failed to fetch member"))

        // When
        viewModel.loadUserData(userId)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Something went wrong. Please try again.", state.error)
        assertNull(state.profile)
        assertNull(state.statistics)
        assertTrue(state.shelf.isEmpty())
    }

    @Test
    fun `loadUserData handles member with no clubs`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-1",
            userId = userId,
            name = "New User",
            booksRead = 0,
            clubs = emptyList()
        )

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // When
        viewModel.loadUserData(userId)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("New User", state.profile?.name)
        assertEquals(0, state.statistics?.clubsCount)
        assertTrue(state.shelf.isEmpty())
    }

    @Test
    fun `refresh reloads data with same userId`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-1",
            userId = userId,
            name = "Alice",
            booksRead = 5,
            clubs = emptyList()
        )

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // Load initial data
        viewModel.loadUserData(userId)

        // When
        viewModel.refresh()

        // Then - State should be refreshed
        val refreshedState = viewModel.state.value
        assertEquals("member-1", refreshedState.profile?.memberId)
        assertFalse(refreshedState.isLoading)
    }

    @Test
    fun `refresh does nothing when no userId has been loaded`() = runTest {
        // Given - No data loaded yet
        val initialState = viewModel.state.value

        // When
        viewModel.refresh()

        // Then - State should remain unchanged (still in initial loading state)
        val afterRefreshState = viewModel.state.value
        assertEquals(initialState.isLoading, afterRefreshState.isLoading)
        assertEquals(initialState.profile, afterRefreshState.profile)
    }

    @Test
    fun `loadUserData clears previous error before loading`() = runTest {
        // Given
        val userId = "user-123"
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.failure(Exception("Error"))

        // Load data with error
        viewModel.loadUserData(userId)
        assertEquals("Something went wrong. Please try again.", viewModel.state.value.error)

        // Given - Now succeed
        val member = Member(
            id = "member-1",
            name = "Alice",
            booksRead = 5,
            userId = userId,
            clubs = emptyList()
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // When - Load again
        viewModel.loadUserData(userId)

        // Then - Error should be cleared
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `loadUserData generates handle from member name`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-1",
            name = "John Doe",
            booksRead = 2,
            userId = userId,
            clubs = emptyList()
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)

        // When
        viewModel.loadUserData(userId)

        // Then
        assertEquals("johndoe", viewModel.state.value.profile?.handle)
    }

    @Test
    fun `loadUserData handles clubs with no active session`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-1",
            userId = userId,
            name = "Alice",
            booksRead = 5,
            clubs = listOf(
                Club(id = "club-1", name = "Inactive Club", role = null)
            )
        )

        val club = Club(id = "club-1", name = "Inactive Club")

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("club-1") } returns Result.success(club)

        // When
        viewModel.loadUserData(userId)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("Alice", state.profile?.name)
        assertEquals(1, state.statistics?.clubsCount)
        assertTrue(state.shelf.isEmpty()) // No active sessions
    }

    @Test
    fun `onSaveProgress updates only the matching shelf item`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(id = "member-1", userId = userId, name = "Alice", booksRead = 5, clubs = listOf(Club(id = "club-1", name = "Club")))
        val book = Book("book-1", "Test Book", "Author", null, 2024, null, pageCount = 200)
        val session = Session(id = "s1", clubId = "club-1", book = book, dueDate = null, discussions = emptyList())
        val club = Club(id = "club-1", name = "Club", activeSession = session)

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("club-1") } returns Result.success(club)
        viewModel.loadUserData(userId)

        val updatedProgress = ReadingProgress(
            id = "p1",
            memberId = "7",
            bookId = "book-1",
            sessionId = "s1",
            type = ProgressType.PAGE,
            status = ProgressStatus.IN_PROGRESS,
            currentPage = 100
        )
        everySuspend { progressRepository.createProgress(any(), any(), any(), any(), any()) } returns Result.success(updatedProgress)

        // When
        viewModel.onSaveProgress("s1", ProgressType.PAGE, 100, null, false)

        // Then
        val item = viewModel.state.value.shelf.first { it.sessionId == "s1" }
        assertEquals("p1", item.ownProgress?.progressId)
        assertEquals("100 of 200 pages", item.ownProgress?.label)
    }

    @Test
    fun `onSaveProgress does nothing for an unknown session`() = runTest {
        // When
        viewModel.onSaveProgress("unknown-session", ProgressType.PAGE, 10, null, false)

        // Then - no crash, no shelf item to update
        assertTrue(viewModel.state.value.shelf.isEmpty())
    }

    @Test
    fun `onReadingLogClicked shows the sheet and loads the log once`() = runTest {
        // Given
        val log = ReadingLog(
            active = listOf(ReadingLogEntry(sessionId = "s1", book = BookSummary(id = "b1", title = "Dune", author = "Herbert"), club = ClubPreview(id = "c1", name = "Club"))),
            finished = emptyList()
        )
        everySuspend { sessionRepository.getReadingLog() } returns Result.success(log)

        // When
        viewModel.onReadingLogClicked()

        // Then
        val state = viewModel.state.value
        assertTrue(state.showReadingLog)
        assertEquals(log, state.readingLog)
        assertFalse(state.isReadingLogLoading)
    }

    @Test
    fun `onReadingLogDismissed hides the sheet`() = runTest {
        everySuspend { sessionRepository.getReadingLog() } returns Result.success(ReadingLog(emptyList(), emptyList()))
        viewModel.onReadingLogClicked()

        viewModel.onReadingLogDismissed()

        assertFalse(viewModel.state.value.showReadingLog)
    }

    @Test
    fun `clearSnackbarError clears the error state`() = runTest {
        // Given: a reading log failure sets snackbarError
        val exception = Exception("Failed to load log")
        everySuspend { sessionRepository.getReadingLog() } returns Result.failure(exception)
        viewModel.onReadingLogClicked()
        assertEquals("Failed to load log", viewModel.state.value.snackbarError)

        // When
        viewModel.clearSnackbarError()

        // Then
        assertNull(viewModel.state.value.snackbarError)
    }

    // -------------------------------------------------------------------------
    // KT-322: overlapping loads
    // -------------------------------------------------------------------------

    @Test
    fun `overlapping loadUserData calls let the latest one win`() = runTest {
        val stale = Member(id = "member-1", userId = "u1", name = "Stale Name", clubs = emptyList())
        val fresh = Member(id = "member-1", userId = "u1", name = "Fresh Name", clubs = emptyList())
        var calls = 0
        everySuspend { memberRepository.getMemberByUserId("u1", any()) } calls {
            if (calls++ == 0) { delay(1_000); Result.success(stale) } else Result.success(fresh)
        }

        viewModel.loadUserData("u1")               // slow, cached load from tab entry
        viewModel.loadUserData("u1", forceRefresh = true) // pull-to-refresh right after
        testScheduler.advanceUntilIdle()

        assertEquals("Fresh Name", viewModel.state.value.profile?.name)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    // -------------------------------------------------------------------------
    // KT-323: a failed refresh never removes content from the screen
    // -------------------------------------------------------------------------

    @Test
    fun `a failed refresh keeps the profile on screen and reports via snackbar`() = runTest {
        val member = Member(id = "member-1", userId = "u1", name = "Alice", clubs = emptyList())
        everySuspend { memberRepository.getMemberByUserId("u1", any()) } returns Result.success(member)
        viewModel.loadUserData("u1")
        assertEquals("Alice", viewModel.state.value.profile?.name)

        everySuspend { memberRepository.getMemberByUserId("u1", any()) } returns Result.failure(Exception("offline"))
        viewModel.refresh(forceRefresh = true)

        val state = viewModel.state.value
        assertEquals("Alice", state.profile?.name)
        assertNull(state.error)
        assertNotNull(state.snackbarError)
        assertFalse(state.isLoading)
    }
}
