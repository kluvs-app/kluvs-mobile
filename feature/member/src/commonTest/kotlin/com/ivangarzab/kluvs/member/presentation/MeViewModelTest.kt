package com.ivangarzab.kluvs.member.presentation

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Session
import com.ivangarzab.kluvs.auth.domain.SignOutUseCase
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.database.dao.BookDao
import com.ivangarzab.kluvs.database.dao.ClubDao
import com.ivangarzab.kluvs.database.dao.DiscussionDao
import com.ivangarzab.kluvs.database.dao.MemberDao
import com.ivangarzab.kluvs.database.dao.ServerDao
import com.ivangarzab.kluvs.database.dao.SessionDao
import com.ivangarzab.kluvs.member.domain.GetCurrentUserProfileUseCase
import com.ivangarzab.kluvs.member.domain.GetCurrentlyReadingBooksUseCase
import com.ivangarzab.kluvs.member.domain.GetUserStatisticsUseCase
import com.ivangarzab.kluvs.member.domain.UpdateAvatarUseCase
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MeViewModelTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var clubRepository: ClubRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var avatarRepository: AvatarRepository
    private lateinit var database: KluvsDatabase
    private lateinit var getCurrentUserProfile: GetCurrentUserProfileUseCase
    private lateinit var getUserStatistics: GetUserStatisticsUseCase
    private lateinit var getCurrentlyReadingBooks: GetCurrentlyReadingBooksUseCase
    private lateinit var signOut: SignOutUseCase
    private lateinit var updateAvatarUseCase: UpdateAvatarUseCase
    private lateinit var viewModel: MeViewModel

    private val formatDateTime = FormatDateTimeUseCase()
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        memberRepository = mock<MemberRepository>()
        clubRepository = mock<ClubRepository>()
        authRepository = mock<AuthRepository>()
        avatarRepository = mock<AvatarRepository>()

        // Set up mock DAOs that SignOutUseCase will call
        val clubDao = mock<ClubDao>()
        val serverDao = mock<ServerDao>()
        val memberDao = mock<MemberDao>()
        val sessionDao = mock<SessionDao>()
        val bookDao = mock<BookDao>()
        val discussionDao = mock<DiscussionDao>()

        database = mock<KluvsDatabase>()
        every { database.clubDao() } returns clubDao
        every { database.serverDao() } returns serverDao
        every { database.memberDao() } returns memberDao
        every { database.sessionDao() } returns sessionDao
        every { database.bookDao() } returns bookDao
        every { database.discussionDao() } returns discussionDao

        // Mock the suspend delete methods to return Unit
        everySuspend { clubDao.deleteAll() } returns Unit
        everySuspend { serverDao.deleteAll() } returns Unit
        everySuspend { memberDao.deleteAll() } returns Unit
        everySuspend { memberDao.deleteAllCrossRefs() } returns Unit
        everySuspend { sessionDao.deleteAll() } returns Unit
        everySuspend { bookDao.deleteAll() } returns Unit
        everySuspend { discussionDao.deleteAll() } returns Unit

        // Use REAL UseCases with mocked repositories
        getCurrentUserProfile = GetCurrentUserProfileUseCase(memberRepository, formatDateTime, avatarRepository)
        getUserStatistics = GetUserStatisticsUseCase(memberRepository)
        getCurrentlyReadingBooks = GetCurrentlyReadingBooksUseCase(memberRepository, clubRepository, formatDateTime)
        signOut = SignOutUseCase(authRepository, database)
        updateAvatarUseCase = UpdateAvatarUseCase(avatarRepository, memberRepository)

        viewModel = MeViewModel(getCurrentUserProfile, getUserStatistics, getCurrentlyReadingBooks, signOut, updateAvatarUseCase)

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
        assertTrue(state.currentlyReading.isEmpty())
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

        // Mock club details for currently reading books
        val book1 = Book("book-1", "The Hobbit", "Tolkien", null, 1937, null)
        val book2 = Book("book-2", "Dune", "Herbert", null, 1965, null)

        val session1 = Session(
            id = "s1",
            clubId = "club-1",
            book = book1,
            dueDate = LocalDateTime(2027, 3, 15, 0, 0),
            discussions = listOf(
                Discussion("d1", "s1", "Chapter 1", LocalDateTime(2024, 1, 1, 19, 0), null),  // Clearly past
                Discussion("d2", "s1", "Chapter 2", LocalDateTime(2027, 2, 15, 19, 0), null)  // Clearly future
            )
        )
        val session2 = Session(
            id = "s2",
            clubId = "club-2",
            book = book2,
            dueDate = LocalDateTime(2027, 4, 1, 0, 0),
            discussions = listOf(
                Discussion("d3", "s2", "Part 1", LocalDateTime(2027, 3, 1, 19, 0), null),  // Clearly future
                Discussion("d4", "s2", "Part 2", LocalDateTime(2027, 3, 15, 19, 0), null), // Clearly future
                Discussion("d5", "s2", "Part 3", LocalDateTime(2027, 3, 29, 19, 0), null), // Clearly future
                Discussion("d6", "s2", "Part 4", LocalDateTime(2027, 4, 12, 19, 0), null)  // Clearly future
            )
        )

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
        assertEquals("@alicejohnson", state.profile?.handle)

        // Statistics
        assertEquals(3, state.statistics?.clubsCount)
        assertEquals(12, state.statistics?.booksRead)

        // Currently reading books
        assertEquals(2, state.currentlyReading.size)
        assertEquals("The Hobbit", state.currentlyReading[0].bookTitle)
        assertEquals("Fantasy Readers", state.currentlyReading[0].clubName)
        assertEquals(0.5f, state.currentlyReading[0].progress) // 1 of 2 discussions complete
        assertEquals("Dune", state.currentlyReading[1].bookTitle)
        assertEquals(0.0f, state.currentlyReading[1].progress) // 0 of 4 discussions complete
    }

    @Test
    fun `loadUserData handles error from member repository`() = runTest {
        // Given
        val userId = "user-123"
        val errorMessage = "Failed to fetch member"
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.loadUserData(userId)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.error)
        assertNull(state.profile)
        assertNull(state.statistics)
        assertTrue(state.currentlyReading.isEmpty())
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
        assertTrue(state.currentlyReading.isEmpty())
    }

    @Test
    fun `loadUserData calculates progress based on completed discussions`() = runTest {
        // Given
        val userId = "user-123"
        val member = Member(
            id = "member-1",
            userId = userId,
            name = "Alice",
            booksRead = 5,
            clubs = listOf(
                Club(id = "club-1", name = "Test Club", role = null)
            )
        )

        val book = Book("book-1", "Test Book", "Author", null, 2024, null)
        val session = Session(
            id = "s1",
            clubId = "club-1",
            book = book,
            dueDate = LocalDateTime(2027, 12, 31, 0, 0),
            discussions = listOf(
                Discussion("d1", "s1", "Part 1", LocalDateTime(2024, 1, 1, 19, 0), null),
                Discussion("d2", "s1", "Part 2", LocalDateTime(2024, 2, 1, 19, 0), null),
                Discussion("d3", "s1", "Part 3", LocalDateTime(2024, 3, 1, 19, 0), null),
                Discussion("d4", "s1", "Part 4", LocalDateTime(2027, 5, 1, 19, 0), null)
            )
        )

        val club = Club(id = "club-1", name = "Test Club", activeSession = session)

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        everySuspend { clubRepository.getClub("club-1") } returns Result.success(club)

        // When
        viewModel.loadUserData(userId)

        // Then
        val reading = viewModel.state.value.currentlyReading
        assertEquals(1, reading.size)
        assertEquals(0.75f, reading[0].progress) // 3 of 4 discussions complete
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
        assertEquals("Error", viewModel.state.value.error)

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
        assertEquals("@johndoe", viewModel.state.value.profile?.handle)
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
        assertTrue(state.currentlyReading.isEmpty()) // No active sessions
    }

    @Test
    fun `uploadAvatar succeeds and updates avatar URL in state`() = runTest {
        // Given
        val userId = "user-123"
        val memberId = "member-1"
        val imageData = ByteArray(100) { it.toByte() }
        val storagePath = "$memberId/avatar.png"
        val avatarUrl = "https://storage.example.com/$storagePath"
        val memberWithoutAvatar = Member(id = memberId, name = "Alice", userId = userId, booksRead = 5, clubs = emptyList())
        val memberWithAvatar = memberWithoutAvatar.copy(avatarPath = storagePath)

        // Load initial profile (no avatar)
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(memberWithoutAvatar)
        every { avatarRepository.getAvatarUrl(null) } returns null
        viewModel.loadUserData(userId)

        // Setup avatar upload mocks — getMember is called by the use case to capture the old path
        everySuspend { memberRepository.getMember(memberId) } returns Result.success(memberWithoutAvatar)
        everySuspend { avatarRepository.uploadAvatar(memberId, imageData) } returns Result.success(storagePath)
        everySuspend { memberRepository.updateMember(memberId, avatarPath = storagePath) } returns Result.success(memberWithAvatar)
        every { avatarRepository.getAvatarUrl(storagePath) } returns avatarUrl
        // oldAvatarPath is null, so deleteAvatar is not called

        // When
        viewModel.uploadAvatar(imageData)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isUploadingAvatar)
        assertNull(state.snackbarError)
        assertEquals(avatarUrl, state.profile?.avatarUrl)
    }

    @Test
    fun `uploadAvatar fails when no member ID available`() = runTest {
        // Given - No profile loaded
        val imageData = ByteArray(100)

        // When
        viewModel.uploadAvatar(imageData)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isUploadingAvatar)
        assertEquals("No member ID available", state.snackbarError)
    }

    @Test
    fun `uploadAvatar handles upload failure`() = runTest {
        // Given
        val userId = "user-123"
        val memberId = "member-1"
        val imageData = ByteArray(100)
        val member = Member(id = memberId, name = "Alice", userId = userId, booksRead = 5, clubs = emptyList())

        // Load initial profile
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        viewModel.loadUserData(userId)

        // Setup avatar upload to fail — getMember is called by the use case before the upload attempt
        everySuspend { memberRepository.getMember(memberId) } returns Result.success(member)
        val exception = Exception("Upload failed")
        everySuspend { avatarRepository.uploadAvatar(memberId, imageData) } returns Result.failure(exception)

        // When
        viewModel.uploadAvatar(imageData)

        // Then
        val state = viewModel.state.value
        assertFalse(state.isUploadingAvatar)
        assertEquals("Upload failed", state.snackbarError)
    }

    @Test
    fun `clearAvatarError clears the error state`() = runTest {
        // Given
        val userId = "user-123"
        val memberId = "member-1"
        val imageData = ByteArray(100)
        val member = Member(id = memberId, name = "Alice", userId = userId, booksRead = 5, clubs = emptyList())

        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(member)
        viewModel.loadUserData(userId)

        // getMember is called by the use case before the upload attempt
        everySuspend { memberRepository.getMember(memberId) } returns Result.success(member)
        val exception = Exception("Upload failed")
        everySuspend { avatarRepository.uploadAvatar(memberId, imageData) } returns Result.failure(exception)
        viewModel.uploadAvatar(imageData)

        // Verify error is set
        assertEquals("Upload failed", viewModel.state.value.snackbarError)

        // When
        viewModel.clearAvatarError()

        // Then
        assertNull(viewModel.state.value.snackbarError)
    }
}
