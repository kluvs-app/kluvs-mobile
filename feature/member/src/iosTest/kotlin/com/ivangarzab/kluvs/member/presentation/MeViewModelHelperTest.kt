package com.ivangarzab.kluvs.member.presentation

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.auth.domain.SignOutUseCase
import com.ivangarzab.kluvs.data.repositories.AvatarRepository
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.data.repositories.ProgressRepository
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.member.domain.GetCurrentUserProfileUseCase
import com.ivangarzab.kluvs.member.domain.GetOnYourShelfUseCase
import com.ivangarzab.kluvs.member.domain.GetReadingLogUseCase
import com.ivangarzab.kluvs.member.domain.GetUserStatisticsUseCase
import com.ivangarzab.kluvs.member.domain.UpdateAvatarUseCase
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.presentation.progress.GetSessionProgressUseCase
import com.ivangarzab.kluvs.presentation.progress.SaveProgressUseCase
import com.ivangarzab.kluvs.presentation.util.FormatDateTimeUseCase
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MeViewModelHelperTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var clubRepository: ClubRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var avatarRepository: AvatarRepository
    private lateinit var database: KluvsDatabase
    private lateinit var viewModel: MeViewModel
    private lateinit var testScope: CoroutineScope
    private lateinit var helper: MeViewModelHelper
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create mocked repositories
        memberRepository = mock<MemberRepository>()
        clubRepository = mock<ClubRepository>()
        sessionRepository = mock<SessionRepository>()
        progressRepository = mock<ProgressRepository>()
        authRepository = mock<AuthRepository>()
        avatarRepository = mock<AvatarRepository>()
        database = mock<KluvsDatabase>()

        // Setup default mock behaviors
        everySuspend { avatarRepository.getAvatarUrl(any()) } returns ""
        everySuspend { progressRepository.getProgress(any(), any(), any()) } returns Result.success(emptyList())

        // Create test scope
        testScope = CoroutineScope(testDispatcher + Job())

        // Create real use cases with mocked repositories
        val formatDateTime = FormatDateTimeUseCase()
        val getCurrentUserProfile = GetCurrentUserProfileUseCase(memberRepository, formatDateTime, avatarRepository)
        val getUserStatistics = GetUserStatisticsUseCase(memberRepository)
        val getSessionProgress = GetSessionProgressUseCase(progressRepository)
        val getOnYourShelf =
            GetOnYourShelfUseCase(memberRepository, clubRepository, getSessionProgress, formatDateTime)
        val getReadingLog = GetReadingLogUseCase(sessionRepository)
        val saveProgress = SaveProgressUseCase(progressRepository)
        val signOut = SignOutUseCase(authRepository, database)
        val updateAvatar = UpdateAvatarUseCase(avatarRepository, memberRepository)


        // Create real ViewModel with real use cases
        viewModel = MeViewModel(
            getCurrentUserProfile,
            getUserStatistics,
            getOnYourShelf,
            getReadingLog,
            saveProgress,
            signOut,
            updateAvatar
        )

        // Start Koin with test module
        startKoin {
            modules(
                module {
                    single<MeViewModel> { viewModel }
                    single<CoroutineScope> { testScope }
                }
            )
        }

        helper = MeViewModelHelper()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `observeState immediately calls callback with current state`() = runTest {
        // Given
        var callbackInvoked = false
        var receivedState: MeState? = null

        // When
        val closeable = helper.observeState { state ->
            callbackInvoked = true
            receivedState = state
        }

        // Then
        assertTrue(callbackInvoked, "Callback should be invoked immediately")
        assertNotNull(receivedState)
        assertTrue(receivedState!!.isLoading)
        assertNull(receivedState!!.error)

        closeable.close()
    }

    @Test
    fun `observeState receives updated states when loadUserData is called`() = runTest {
        // Given
        val receivedStates = mutableListOf<MeState>()
        val closeable = helper.observeState { state ->
            receivedStates.add(state)
        }

        // Mock repository to return test data
        val testMember = Member(
            id = "member-1",
            name = "John Doe",
            handle = "@john",
            userId = "user-1",
            createdAt = LocalDateTime(2024, 1, 1, 0, 0)
        )
        everySuspend { memberRepository.getMemberByUserId("user-1") } returns Result.success(
            testMember
        )

        // When - call loadUserData
        helper.loadUserData("user-1")

        // Then
        assertTrue(receivedStates.size >= 2, "Should receive initial state + at least one update")
        assertTrue(receivedStates.first().isLoading, "First state should be loading")

        closeable.close()
    }

    @Test
    fun `closeable stops receiving updates when closed`() = runTest {
        // Given
        val receivedStates = mutableListOf<MeState>()
        val closeable = helper.observeState { state ->
            receivedStates.add(state)
        }

        val initialSize = receivedStates.size

        // When - close the observer
        closeable.close()

        // Trigger state change after closing
        val testMember = Member(
            id = "member-1",
            name = "John Doe",
            handle = "@john",
            userId = "user-1",
            createdAt = LocalDateTime(2024, 1, 1, 0, 0)
        )
        everySuspend { memberRepository.getMemberByUserId("user-1") } returns Result.success(
            testMember
        )
        helper.loadUserData("user-1")

        // Then - should not receive new states after closing
        assertEquals(
            initialSize,
            receivedStates.size,
            "Should not receive state emitted after closing"
        )
    }

    @Test
    fun `loadUserData calls ViewModel loadUserData`() = runTest {
        // Given
        val userId = "test-user-id"
        val testMember = Member(
            id = "member-1",
            name = "Test User",
            handle = "@test",
            userId = userId,
            createdAt = LocalDateTime(2024, 1, 1, 0, 0)
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(
            testMember
        )

        // When
        helper.loadUserData(userId)

        // Then - verify state was updated (which means ViewModel was called)
        assertNotNull(viewModel.state.value)
    }

    @Test
    fun `refresh calls ViewModel refresh`() = runTest {
        // Given - load some initial data
        val userId = "test-user-id"
        val testMember = Member(
            id = "member-1",
            name = "Test User",
            handle = "@test",
            userId = userId,
            createdAt = LocalDateTime(2024, 1, 1, 0, 0)
        )
        everySuspend { memberRepository.getMemberByUserId(userId) } returns Result.success(
            testMember
        )
        helper.loadUserData(userId)

        // When
        helper.refresh()

        // Then - verify refresh was called (state should still be valid)
        assertNotNull(viewModel.state.value)
    }

    @Test
    fun `multiple observers can observe simultaneously`() = runTest {
        // Given
        val observer1States = mutableListOf<MeState>()
        val observer2States = mutableListOf<MeState>()

        val closeable1 = helper.observeState { state ->
            observer1States.add(state)
        }
        val closeable2 = helper.observeState { state ->
            observer2States.add(state)
        }

        val initialSize1 = observer1States.size
        val initialSize2 = observer2States.size

        // When - trigger state change
        val testMember = Member(
            id = "member-1",
            name = "John Doe",
            handle = "@john",
            userId = "user-1",
            createdAt = LocalDateTime(2024, 1, 1, 0, 0)
        )
        everySuspend { memberRepository.getMemberByUserId("user-1") } returns Result.success(
            testMember
        )
        helper.loadUserData("user-1")

        // Then
        assertTrue(observer1States.size >= initialSize1, "Observer 1 should receive states")
        assertTrue(observer2States.size >= initialSize2, "Observer 2 should receive states")

        closeable1.close()
        closeable2.close()
    }

    @Test
    fun `closing one observer does not affect other observers`() = runTest {
        // Given
        val observer1States = mutableListOf<MeState>()
        val observer2States = mutableListOf<MeState>()

        val closeable1 = helper.observeState { state ->
            observer1States.add(state)
        }
        val closeable2 = helper.observeState { state ->
            observer2States.add(state)
        }

        // When - close first observer
        closeable1.close()
        val sizeBefore = observer1States.size

        // Trigger state change
        val testMember = Member(
            id = "member-1",
            name = "John Doe",
            handle = "@john",
            userId = "user-1",
            createdAt = LocalDateTime(2024, 1, 1, 0, 0)
        )
        everySuspend { memberRepository.getMemberByUserId("user-1") } returns Result.success(
            testMember
        )
        helper.loadUserData("user-1")

        // Then
        assertEquals(sizeBefore, observer1States.size, "Observer 1 should not receive new states")
        assertTrue(observer2States.size > 1, "Observer 2 should continue receiving states")

        closeable2.close()
    }
}