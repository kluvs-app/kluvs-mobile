package com.ivangarzab.kluvs.app

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.repositories.JoinRepository
import com.ivangarzab.kluvs.model.AuthProvider
import com.ivangarzab.kluvs.model.User
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [PendingJoinCoordinator].
 *
 * Verifies auto-join only fires on the transition to [NavigationState.Authenticated]
 * while a pending invite token is set, and never on an unrelated auth-state emission.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PendingJoinCoordinatorTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var joinRepository: JoinRepository
    private lateinit var appCoordinator: AppCoordinator
    private lateinit var currentUserFlow: MutableStateFlow<User?>
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testUser = User(
        id = "user-1",
        email = "test@example.com",
        displayName = "Test User",
        avatarUrl = null,
        provider = AuthProvider.EMAIL
    )
    private val otherUser = User(
        id = "user-2",
        email = "other@example.com",
        displayName = "Other User",
        avatarUrl = null,
        provider = AuthProvider.EMAIL
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock<AuthRepository>()
        joinRepository = mock<JoinRepository>()

        currentUserFlow = MutableStateFlow(null)
        every { authRepository.currentUser } returns currentUserFlow
        everySuspend { authRepository.initialize() } returns Result.success(null)

        appCoordinator = AppCoordinator(authRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `auto-joins when a pending token exists and user becomes authenticated`() = runTest {
        everySuspend { joinRepository.joinClub("token-1") } returns Result.success("club-1")
        val coordinator = PendingJoinCoordinator(appCoordinator, joinRepository)
        val results = mutableListOf<AutoJoinResult>()
        val job = launch(testDispatcher) { coordinator.autoJoinResult.collect { results.add(it) } }

        coordinator.setPendingToken("token-1")
        currentUserFlow.value = testUser
        testScheduler.advanceUntilIdle()

        assertEquals(1, results.size)
        val result = results.first()
        assertIs<AutoJoinResult.Success>(result)
        assertEquals("club-1", result.clubId)
        job.cancel()
    }

    @Test
    fun `does not auto-join when already authenticated with no pending token`() = runTest {
        // Simulates app launch with an already-restored session and no pending invite
        currentUserFlow.value = testUser
        val coordinator = PendingJoinCoordinator(appCoordinator, joinRepository)
        val results = mutableListOf<AutoJoinResult>()
        val job = launch(testDispatcher) { coordinator.autoJoinResult.collect { results.add(it) } }

        assertTrue(results.isEmpty())
        verifySuspend(mode = VerifyMode.not) { joinRepository.joinClub(any()) }
        job.cancel()
    }

    @Test
    fun `does not auto-join without a pending token even on a real auth transition`() = runTest {
        val coordinator = PendingJoinCoordinator(appCoordinator, joinRepository)
        val results = mutableListOf<AutoJoinResult>()
        val job = launch(testDispatcher) { coordinator.autoJoinResult.collect { results.add(it) } }

        currentUserFlow.value = testUser

        assertTrue(results.isEmpty())
        verifySuspend(mode = VerifyMode.not) { joinRepository.joinClub(any()) }
        job.cancel()
    }

    @Test
    fun `emits Failure when auto-join fails`() = runTest {
        everySuspend { joinRepository.joinClub("token-1") } returns Result.failure(Exception("boom"))
        val coordinator = PendingJoinCoordinator(appCoordinator, joinRepository)
        val results = mutableListOf<AutoJoinResult>()
        val job = launch(testDispatcher) { coordinator.autoJoinResult.collect { results.add(it) } }

        coordinator.setPendingToken("token-1")
        currentUserFlow.value = testUser
        testScheduler.advanceUntilIdle()

        assertEquals(1, results.size)
        assertIs<AutoJoinResult.Failure>(results.first())
        job.cancel()
    }

    @Test
    fun `pending token is consumed once and does not rejoin on a later auth transition`() = runTest {
        everySuspend { joinRepository.joinClub("token-1") } returns Result.success("club-1")
        val coordinator = PendingJoinCoordinator(appCoordinator, joinRepository)
        val results = mutableListOf<AutoJoinResult>()
        val job = launch(testDispatcher) { coordinator.autoJoinResult.collect { results.add(it) } }

        coordinator.setPendingToken("token-1")
        currentUserFlow.value = testUser
        testScheduler.advanceUntilIdle()
        currentUserFlow.value = null
        currentUserFlow.value = otherUser
        testScheduler.advanceUntilIdle()

        assertEquals(1, results.size)
        job.cancel()
    }
}
