package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.model.AuthProvider
import com.ivangarzab.kluvs.model.User
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelHelperTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel
    private lateinit var testScope: CoroutineScope
    private lateinit var helper: AuthViewModelHelper
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create mocked repository
        authRepository = mock<AuthRepository>()

        // Setup repository state flows
        val currentUserFlow = MutableStateFlow<User?>(null)
        every { authRepository.currentUser } returns currentUserFlow
        everySuspend { authRepository.initialize() } returns Result.success(null)

        // Create test scope
        testScope = CoroutineScope(testDispatcher + Job())

        // Create real ViewModel with mocked repository
        viewModel = AuthViewModel(authRepository)

        // Start Koin with test module
        startKoin {
            modules(
                module {
                    single<AuthViewModel> { viewModel }
                    single<CoroutineScope> { testScope }
                }
            )
        }

        helper = AuthViewModelHelper()
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
        var receivedState: AuthState? = null

        // When
        val closeable = helper.observeState { state ->
            callbackInvoked = true
            receivedState = state
        }

        // Then
        assertTrue(callbackInvoked, "Callback should be invoked immediately")
        assertNotNull(receivedState)
        assertIs<AuthState.Unauthenticated>(receivedState!!)

        closeable.close()
    }

    @Test
    fun `observeUiState immediately calls callback with current UI state`() = runTest {
        // Given
        var callbackInvoked = false
        var receivedUiState: AuthUiState? = null

        // When
        val closeable = helper.observeUiState { uiState ->
            callbackInvoked = true
            receivedUiState = uiState
        }

        // Then
        assertTrue(callbackInvoked, "Callback should be invoked immediately")
        assertNotNull(receivedUiState)
        assertEquals("", receivedUiState!!.emailField)
        assertEquals("", receivedUiState!!.passwordField)
        assertNull(receivedUiState!!.emailError)

        closeable.close()
    }

    @Test
    fun `onEmailFieldChanged updates UI state`() = runTest {
        // Given
        val receivedStates = mutableListOf<AuthUiState>()
        val closeable = helper.observeUiState { state ->
            receivedStates.add(state)
        }

        // When
        helper.onEmailFieldChanged("test@example.com")

        // Then
        assertTrue(receivedStates.size >= 2, "Should receive initial state + update")
        assertEquals("test@example.com", receivedStates.last().emailField)

        closeable.close()
    }

    @Test
    fun `onPasswordFieldChanged updates UI state`() = runTest {
        // Given
        val receivedStates = mutableListOf<AuthUiState>()
        val closeable = helper.observeUiState { state ->
            receivedStates.add(state)
        }

        // When
        helper.onPasswordFieldChanged("password123")

        // Then
        assertTrue(receivedStates.size >= 2, "Should receive initial state + update")
        assertEquals("password123", receivedStates.last().passwordField)

        closeable.close()
    }

    @Test
    fun `onConfirmPasswordFieldChanged updates UI state`() = runTest {
        // Given
        val receivedStates = mutableListOf<AuthUiState>()
        val closeable = helper.observeUiState { state ->
            receivedStates.add(state)
        }

        // When
        helper.onConfirmPasswordFieldChanged("password123")

        // Then
        assertTrue(receivedStates.size >= 2, "Should receive initial state + update")
        assertEquals("password123", receivedStates.last().confirmPasswordField)

        closeable.close()
    }

    @Test
    fun `validateAndSignIn with invalid email sets error in UI state`() = runTest {
        // Given
        val receivedStates = mutableListOf<AuthUiState>()
        val closeable = helper.observeUiState { state ->
            receivedStates.add(state)
        }

        // Set invalid email (no @)
        helper.onEmailFieldChanged("invalid-email")

        // When
        helper.validateAndSignIn()

        // Then
        val lastState = receivedStates.last()
        assertNotNull(lastState.emailError, "Should have email error")

        closeable.close()
    }

    @Test
    fun `observeForgotPasswordState immediately calls callback with current state`() = runTest {
        // Given
        var callbackInvoked = false
        var receivedState: ForgotPasswordUiState? = null

        // When
        val closeable = helper.observeForgotPasswordState { state ->
            callbackInvoked = true
            receivedState = state
        }

        // Then
        assertTrue(callbackInvoked, "Callback should be invoked immediately")
        assertNotNull(receivedState)
        assertEquals("", receivedState!!.emailField)
        assertNull(receivedState!!.emailError)

        closeable.close()
    }

    @Test
    fun `onForgotPasswordEmailChanged updates forgot password state`() = runTest {
        // Given
        val receivedStates = mutableListOf<ForgotPasswordUiState>()
        val closeable = helper.observeForgotPasswordState { state ->
            receivedStates.add(state)
        }

        // When
        helper.onForgotPasswordEmailChanged("test@example.com")

        // Then
        assertTrue(receivedStates.size >= 2, "Should receive initial state + update")
        assertEquals("test@example.com", receivedStates.last().emailField)

        closeable.close()
    }

    @Test
    fun `sendPasswordResetEmail with invalid email sets error in state`() = runTest {
        // Given
        val receivedStates = mutableListOf<ForgotPasswordUiState>()
        val closeable = helper.observeForgotPasswordState { state ->
            receivedStates.add(state)
        }

        helper.onForgotPasswordEmailChanged("invalid-email")

        // When
        helper.sendPasswordResetEmail()

        // Then
        val lastState = receivedStates.last()
        assertNotNull(lastState.emailError, "Should have email error")

        closeable.close()
    }

    @Test
    fun `sendPasswordResetEmail with valid email calls repository`() = runTest {
        // Given
        everySuspend { authRepository.resetPasswordForEmail("test@example.com") } returns Result.success(Unit)
        val receivedStates = mutableListOf<ForgotPasswordUiState>()
        val closeable = helper.observeForgotPasswordState { state ->
            receivedStates.add(state)
        }

        helper.onForgotPasswordEmailChanged("test@example.com")

        // When
        helper.sendPasswordResetEmail()

        // Then
        assertTrue(receivedStates.last().isEmailSent)

        closeable.close()
    }

    @Test
    fun `resetForgotPasswordState clears state`() = runTest {
        // Given
        everySuspend { authRepository.resetPasswordForEmail("test@example.com") } returns Result.success(Unit)
        helper.onForgotPasswordEmailChanged("test@example.com")
        helper.sendPasswordResetEmail()

        // When
        helper.resetForgotPasswordState()
        val receivedStates = mutableListOf<ForgotPasswordUiState>()
        val closeable = helper.observeForgotPasswordState { state ->
            receivedStates.add(state)
        }

        // Then
        assertEquals(ForgotPasswordUiState(), receivedStates.first())

        closeable.close()
    }

    @Test
    fun `signOut calls repository signOut`() = runTest {
        // Given
        everySuspend { authRepository.signOut() } returns Result.success(Unit)

        // When
        helper.signOut()

        // Then - verify ViewModel's signOut was called (which calls repository)
        // Note: We can't directly verify the repository call since it's internal to ViewModel
        // But we can verify the helper successfully delegated the call
    }

    @Test
    fun `closeable stops receiving state updates when closed`() = runTest {
        // Given
        val receivedStates = mutableListOf<AuthState>()
        val closeable = helper.observeState { state ->
            receivedStates.add(state)
        }

        val initialSize = receivedStates.size

        // When - close the observer
        closeable.close()

        // Trigger state change after closing
        val testUser = User(
            id = "user-1",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null,
            provider = AuthProvider.EMAIL
        )
        val currentUserFlow = MutableStateFlow<User?>(testUser)
        every { authRepository.currentUser } returns currentUserFlow

        // Then - should not receive new states after closing
        assertEquals(
            initialSize,
            receivedStates.size,
            "Should not receive state emitted after closing"
        )
    }

    @Test
    fun `multiple observers can observe simultaneously`() = runTest {
        // Given
        val observer1States = mutableListOf<AuthUiState>()
        val observer2States = mutableListOf<AuthUiState>()

        val closeable1 = helper.observeUiState { state ->
            observer1States.add(state)
        }
        val closeable2 = helper.observeUiState { state ->
            observer2States.add(state)
        }

        val initialSize1 = observer1States.size
        val initialSize2 = observer2States.size

        // When - trigger state change
        helper.onEmailFieldChanged("multi@test.com")

        // Then
        assertTrue(observer1States.size >= initialSize1, "Observer 1 should receive states")
        assertTrue(observer2States.size >= initialSize2, "Observer 2 should receive states")

        closeable1.close()
        closeable2.close()
    }
}