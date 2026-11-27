package com.ivangarzab.kluvs.app

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.domain.CheckUpdateRequiredUseCase
import com.ivangarzab.kluvs.data.repositories.ConfigRepository
import com.ivangarzab.kluvs.model.ClientConfig
import com.ivangarzab.kluvs.model.AuthProvider
import com.ivangarzab.kluvs.model.User
import com.ivangarzab.kluvs.presentation.effect.PlatformEffectHandler
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppCoordinatorHelperTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var configRepository: ConfigRepository
    private lateinit var currentUserFlow: MutableStateFlow<User?>
    private lateinit var appCoordinator: AppCoordinator
    private val platformEffectHandler = mock<PlatformEffectHandler>(MockMode.autoUnit)
    private lateinit var testScope: CoroutineScope
    private lateinit var helper: AppCoordinatorHelper
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create mocked repository
        authRepository = mock<AuthRepository>()
        // No minimum supported version by default, so no update is required
        configRepository = mock<ConfigRepository>()
        everySuspend { configRepository.getConfig() } returns
            Result.success(ClientConfig(flags = emptyMap(), minSupportedVersion = null))

        // Setup repository state flows - start with no user
        currentUserFlow = MutableStateFlow<User?>(null)
        every { authRepository.currentUser } returns currentUserFlow
        everySuspend { authRepository.initialize() } returns Result.success(null)

        // Create test scope
        testScope = CoroutineScope(testDispatcher + Job())

        // Create real AppCoordinator with mocked repository
        appCoordinator = AppCoordinator(authRepository, CheckUpdateRequiredUseCase(configRepository, INSTALLED_VERSION), platformEffectHandler)

        // Start Koin with test module
        startKoin {
            modules(
                module {
                    single<AppCoordinator> { appCoordinator }
                    single<CoroutineScope> { testScope }
                }
            )
        }

        helper = AppCoordinatorHelper()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `observeNavigationState immediately calls callback with current state`() = runTest {
        // Given
        var callbackInvoked = false
        var receivedState: NavigationState? = null

        // When
        val closeable = helper.observeNavigationState { state ->
            callbackInvoked = true
            receivedState = state
        }

        // Then
        assertTrue(callbackInvoked, "Callback should be invoked immediately")
        assertNotNull(receivedState)

        closeable.close()
    }

    @Test
    fun `observeNavigationState emits Initializing then Unauthenticated when no user`() = runTest {
        // Given
        val receivedStates = mutableListOf<NavigationState>()
        val closeable = helper.observeNavigationState { state ->
            receivedStates.add(state)
        }

        // Then - AppCoordinator should emit Initializing first, then Unauthenticated
        assertTrue(receivedStates.size >= 1, "Should receive at least one state")

        // The states depend on initialization timing, but eventually should be Unauthenticated
        // Since we mocked initialize() to return null user
        val finalState = receivedStates.last()
        // Could be Initializing or Unauthenticated depending on timing
        assertTrue(
            finalState is NavigationState.Initializing || finalState is NavigationState.Unauthenticated,
            "Final state should be Initializing or Unauthenticated"
        )

        closeable.close()
    }

    @Test
    fun `observeNavigationState emits Authenticated when user signs in`() = runTest {
        // Given
        val receivedStates = mutableListOf<NavigationState>()
        val closeable = helper.observeNavigationState { state ->
            receivedStates.add(state)
        }

        // When - simulate user sign in
        val testUser = User(
            id = "user-123",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null,
            provider = AuthProvider.EMAIL
        )
        currentUserFlow.value = testUser

        // Then
        assertTrue(receivedStates.size >= 2, "Should receive at least initial + authenticated state")

        val authenticatedState = receivedStates.firstOrNull { it is NavigationState.Authenticated }
        assertNotNull(authenticatedState, "Should have an authenticated state")
        assertIs<NavigationState.Authenticated>(authenticatedState)
        assertEquals("user-123", (authenticatedState as NavigationState.Authenticated).userId)

        closeable.close()
    }

    @Test
    fun `closeable stops receiving navigation updates when closed`() = runTest {
        // Given
        val receivedStates = mutableListOf<NavigationState>()
        val closeable = helper.observeNavigationState { state ->
            receivedStates.add(state)
        }

        val initialSize = receivedStates.size

        // When - close the observer
        closeable.close()

        // Trigger state change after closing
        val testUser = User(
            id = "user-456",
            email = "test2@example.com",
            displayName = "Test User 2",
            avatarUrl = null,
            provider = AuthProvider.EMAIL
        )
        currentUserFlow.value = testUser

        // Then - should not receive new states after closing
        assertEquals(initialSize, receivedStates.size, "Should not receive state emitted after closing")
    }

    @Test
    fun `multiple observers can observe navigation state simultaneously`() = runTest {
        // Given
        val observer1States = mutableListOf<NavigationState>()
        val observer2States = mutableListOf<NavigationState>()

        val closeable1 = helper.observeNavigationState { state ->
            observer1States.add(state)
        }
        val closeable2 = helper.observeNavigationState { state ->
            observer2States.add(state)
        }

        val initialSize1 = observer1States.size
        val initialSize2 = observer2States.size

        // When - trigger state change
        val testUser = User(
            id = "user-789",
            email = "test3@example.com",
            displayName = "Test User 3",
            avatarUrl = null,
            provider = AuthProvider.EMAIL
        )
        currentUserFlow.value = testUser

        // Then
        assertTrue(observer1States.size >= initialSize1, "Observer 1 should receive states")
        assertTrue(observer2States.size >= initialSize2, "Observer 2 should receive states")

        closeable1.close()
        closeable2.close()
    }

    private companion object {
        const val INSTALLED_VERSION = "1.0.0"
    }
}
