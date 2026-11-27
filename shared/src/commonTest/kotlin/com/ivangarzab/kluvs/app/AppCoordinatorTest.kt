package com.ivangarzab.kluvs.app

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.domain.CheckUpdateRequiredUseCase
import com.ivangarzab.kluvs.data.repositories.ConfigRepository
import com.ivangarzab.kluvs.model.ClientConfig
import com.ivangarzab.kluvs.model.AuthProvider
import com.ivangarzab.kluvs.model.User
import com.ivangarzab.kluvs.presentation.effect.PlatformEffect
import com.ivangarzab.kluvs.presentation.effect.PlatformEffectHandler
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for AppCoordinator.
 *
 * Verifies the app-level navigation logic based on authentication state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppCoordinatorTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var configRepository: ConfigRepository
    private lateinit var appCoordinator: AppCoordinator
    private val platformEffectHandler = mock<PlatformEffectHandler>(MockMode.autoUnit)
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock<AuthRepository>()
        // No minimum supported version by default, so no update is required
        configRepository = mock<ConfigRepository>()
        everySuspend { configRepository.getConfig() } returns
            Result.success(ClientConfig(flags = emptyMap(), minSupportedVersion = null))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `transitions to Unauthenticated when no user exists`() = runTest {
        // Given
        val currentUserFlow = MutableStateFlow<User?>(null)
        every { authRepository.currentUser } returns currentUserFlow
        everySuspend { authRepository.initialize() } returns Result.success(null)

        // When
        appCoordinator = AppCoordinator(authRepository, CheckUpdateRequiredUseCase(configRepository, INSTALLED_VERSION), platformEffectHandler)

        // Wait for initialization to complete
        testScheduler.advanceUntilIdle()

        // Then - should transition to Unauthenticated
        assertIs<NavigationState.Unauthenticated>(appCoordinator.navigationState.value)
    }

    @Test
    fun `transitions to Authenticated when user exists after initialization`() = runTest {
        // Given
        val testUser = User(
            id = "user-123",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null,
            provider = AuthProvider.EMAIL
        )
        val currentUserFlow = MutableStateFlow<User?>(testUser)
        every { authRepository.currentUser } returns currentUserFlow
        everySuspend { authRepository.initialize() } returns Result.success(testUser)

        // When
        appCoordinator = AppCoordinator(authRepository, CheckUpdateRequiredUseCase(configRepository, INSTALLED_VERSION), platformEffectHandler)

        // Wait for initialization to complete
        testScheduler.advanceUntilIdle()

        // Then - should transition to Authenticated
        val state = appCoordinator.navigationState.value
        assertIs<NavigationState.Authenticated>(state)
        assertEquals("user-123", state.userId)
    }

    @Test
    fun `transitions to Authenticated when user signs in after starting unauthenticated`() = runTest {
        // Given - start with no user
        val currentUserFlow = MutableStateFlow<User?>(null)
        every { authRepository.currentUser } returns currentUserFlow
        everySuspend { authRepository.initialize() } returns Result.success(null)

        appCoordinator = AppCoordinator(authRepository, CheckUpdateRequiredUseCase(configRepository, INSTALLED_VERSION), platformEffectHandler)

        // Wait for initialization
        testScheduler.advanceUntilIdle()

        // Verify starting in Unauthenticated
        assertIs<NavigationState.Unauthenticated>(appCoordinator.navigationState.value)

        // When - user signs in
        val testUser = User(
            id = "user-456",
            email = "new@example.com",
            displayName = "New User",
            avatarUrl = null,
            provider = AuthProvider.GOOGLE
        )
        currentUserFlow.value = testUser

        // Then - should transition to Authenticated
        val state = appCoordinator.navigationState.value
        assertIs<NavigationState.Authenticated>(state)
        assertEquals("user-456", state.userId)
    }

    @Test
    fun `transitions to Unauthenticated when user signs out`() = runTest {
        // Given - start with authenticated user
        val testUser = User(
            id = "user-789",
            email = "signout@example.com",
            displayName = "Sign Out User",
            avatarUrl = null,
            provider = AuthProvider.DISCORD
        )
        val currentUserFlow = MutableStateFlow<User?>(testUser)
        every { authRepository.currentUser } returns currentUserFlow
        everySuspend { authRepository.initialize() } returns Result.success(testUser)

        appCoordinator = AppCoordinator(authRepository, CheckUpdateRequiredUseCase(configRepository, INSTALLED_VERSION), platformEffectHandler)

        // Wait for initialization
        testScheduler.advanceUntilIdle()

        // Verify starting in Authenticated
        assertIs<NavigationState.Authenticated>(appCoordinator.navigationState.value)

        // When - user signs out
        currentUserFlow.value = null

        // Then - should transition to Unauthenticated
        assertIs<NavigationState.Unauthenticated>(appCoordinator.navigationState.value)
    }

    @Test
    fun `updates userId when different user signs in`() = runTest {
        // Given - start with first user
        val user1 = User(
            id = "user-1",
            email = "first@example.com",
            displayName = "First User",
            avatarUrl = null,
            provider = AuthProvider.EMAIL
        )
        val currentUserFlow = MutableStateFlow<User?>(user1)
        every { authRepository.currentUser } returns currentUserFlow
        everySuspend { authRepository.initialize() } returns Result.success(user1)

        appCoordinator = AppCoordinator(authRepository, CheckUpdateRequiredUseCase(configRepository, INSTALLED_VERSION), platformEffectHandler)

        // Wait for initialization
        testScheduler.advanceUntilIdle()

        // Verify first user
        val state1 = appCoordinator.navigationState.value
        assertIs<NavigationState.Authenticated>(state1)
        assertEquals("user-1", state1.userId)

        // When - different user signs in
        val user2 = User(
            id = "user-2",
            email = "second@example.com",
            displayName = "Second User",
            avatarUrl = null,
            provider = AuthProvider.EMAIL
        )
        currentUserFlow.value = user2

        // Then - should update to new userId
        val state2 = appCoordinator.navigationState.value
        assertIs<NavigationState.Authenticated>(state2)
        assertEquals("user-2", state2.userId)
    }

    @Test
    fun `transitions to UpdateRequired when installed version is below minimum`() = runTest {
        // Given
        every { authRepository.currentUser } returns MutableStateFlow<User?>(null)
        everySuspend { authRepository.initialize() } returns Result.success(null)
        everySuspend { configRepository.getConfig() } returns
            Result.success(ClientConfig(flags = emptyMap(), minSupportedVersion = "2.0.0"))

        // When
        appCoordinator = AppCoordinator(authRepository, CheckUpdateRequiredUseCase(configRepository, INSTALLED_VERSION), platformEffectHandler)
        testScheduler.advanceUntilIdle()

        // Then
        assertIs<NavigationState.UpdateRequired>(appCoordinator.navigationState.value)
    }

    @Test
    fun `UpdateRequired is kept when auth state changes`() = runTest {
        // Given
        val currentUserFlow = MutableStateFlow<User?>(null)
        every { authRepository.currentUser } returns currentUserFlow
        everySuspend { authRepository.initialize() } returns Result.success(null)
        everySuspend { configRepository.getConfig() } returns
            Result.success(ClientConfig(flags = emptyMap(), minSupportedVersion = "2.0.0"))

        appCoordinator = AppCoordinator(authRepository, CheckUpdateRequiredUseCase(configRepository, INSTALLED_VERSION), platformEffectHandler)
        testScheduler.advanceUntilIdle()

        // When - a restored session arrives after the check
        currentUserFlow.value = User(
            id = "user-123",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null,
            provider = AuthProvider.EMAIL
        )

        // Then - the update screen still wins
        assertIs<NavigationState.UpdateRequired>(appCoordinator.navigationState.value)
    }

    @Test
    fun `starts normally when the config request fails`() = runTest {
        // Given
        every { authRepository.currentUser } returns MutableStateFlow<User?>(null)
        everySuspend { authRepository.initialize() } returns Result.success(null)
        everySuspend { configRepository.getConfig() } returns Result.failure(Exception("Offline"))

        // When
        appCoordinator = AppCoordinator(authRepository, CheckUpdateRequiredUseCase(configRepository, INSTALLED_VERSION), platformEffectHandler)
        testScheduler.advanceUntilIdle()

        // Then
        assertIs<NavigationState.Unauthenticated>(appCoordinator.navigationState.value)
    }

    @Test
    fun `onUpdateApp opens the store listing`() = runTest {
        // Given
        every { authRepository.currentUser } returns MutableStateFlow<User?>(null)
        everySuspend { authRepository.initialize() } returns Result.success(null)
        appCoordinator = AppCoordinator(authRepository, CheckUpdateRequiredUseCase(configRepository, INSTALLED_VERSION), platformEffectHandler)

        // When
        appCoordinator.onUpdateApp()

        // Then
        verify { platformEffectHandler.handle(PlatformEffect.OpenStoreListing) }
    }

    private companion object {
        const val INSTALLED_VERSION = "1.0.0"
    }
}
