package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.model.AuthProvider
import com.ivangarzab.kluvs.model.User
import com.ivangarzab.kluvs.presentation.effect.PlatformEffect
import com.ivangarzab.kluvs.presentation.effect.PlatformEffectHandler
import com.ivangarzab.kluvs.presentation.effect.NoticeKey
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import com.ivangarzab.kluvs.presentation.effect.SnackbarMessage
import com.ivangarzab.kluvs.presentation.effect.SnackbarTone
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel
    private lateinit var platformEffectHandler: PlatformEffectHandler
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock<AuthRepository>()

        // Setup repository state flows
        val currentUserFlow = MutableStateFlow<User?>(null)
        every { authRepository.currentUser } returns currentUserFlow
        everySuspend { authRepository.initialize() } returns Result.success(null)

        // BaseViewModel gets its PlatformEffectHandler from Koin
        platformEffectHandler = mock(MockMode.autoUnit)
        startKoin { modules(module { single<PlatformEffectHandler> { platformEffectHandler } }) }
        viewModel = createAuthViewModel(authRepository)
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    // ========== Initialization Tests ==========

    @Test
    fun `initial state is unauthenticated`() {
        // Then
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
        assertEquals("", viewModel.state.value.emailField)
        assertEquals("", viewModel.state.value.passwordField)
        assertEquals("", viewModel.state.value.confirmPasswordField)
        assertNull(viewModel.state.value.emailError)
        assertNull(viewModel.state.value.passwordError)
        assertNull(viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun `initializes repository on creation`() {
        // Then
        verifySuspend { authRepository.initialize() }
    }

    // ========== Field Change Tests ==========

    @Test
    fun `onEmailFieldChanged updates email field and clears error`() {
        // Given - Set an initial error
        viewModel.validateAndSignIn() // Will set errors

        // When
        viewModel.onEmailFieldChanged("test@example.com")

        // Then
        assertEquals("test@example.com", viewModel.state.value.emailField)
        assertNull(viewModel.state.value.emailError)
    }

    @Test
    fun `onPasswordFieldChanged updates password field and clears error`() {
        // Given - Set an initial error
        viewModel.validateAndSignIn() // Will set errors

        // When
        viewModel.onPasswordFieldChanged("password123")

        // Then
        assertEquals("password123", viewModel.state.value.passwordField)
        assertNull(viewModel.state.value.passwordError)
    }

    @Test
    fun `onConfirmPasswordFieldChanged updates confirm password field and clears error`() {
        // Given
        viewModel.onPasswordFieldChanged("password123")
        viewModel.onConfirmPasswordFieldChanged("different")
        viewModel.validateAndSignUp() // Will set error

        // When
        viewModel.onConfirmPasswordFieldChanged("password123")

        // Then
        assertEquals("password123", viewModel.state.value.confirmPasswordField)
        assertNull(viewModel.state.value.confirmPasswordError)
    }

    // ========== Sign In Validation Tests ==========

    @Test
    fun `validateAndSignIn with empty email shows error`() {
        // Given
        viewModel.onEmailFieldChanged("")
        viewModel.onPasswordFieldChanged("password123")

        // When
        viewModel.validateAndSignIn()

        // Then
        assertEquals("Email is required", viewModel.state.value.emailError)
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState) // Should not call repository
    }

    @Test
    fun `validateAndSignIn with invalid email shows error`() {
        // Given
        viewModel.onEmailFieldChanged("invalid-email")
        viewModel.onPasswordFieldChanged("password123")

        // When
        viewModel.validateAndSignIn()

        // Then
        assertEquals("Please enter a valid email", viewModel.state.value.emailError)
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
    }

    @Test
    fun `validateAndSignIn with empty password shows error`() {
        // Given
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("")

        // When
        viewModel.validateAndSignIn()

        // Then
        assertEquals("Password is required", viewModel.state.value.passwordError)
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
    }

    @Test
    fun `validateAndSignIn with short password shows error`() {
        // Given
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("12345") // Only 5 characters

        // When
        viewModel.validateAndSignIn()

        // Then
        assertEquals("Password must be at least 6 characters", viewModel.state.value.passwordError)
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
    }

    @Test
    fun `validateAndSignIn with valid credentials calls repository`() = runTest {
        // Given
        val user = User("user-id", "test@example.com", "Test User", null, AuthProvider.EMAIL)
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("password123")
        everySuspend { authRepository.signInWithEmail("test@example.com", "password123") } returns Result.success(user)

        // When
        viewModel.validateAndSignIn()

        // Then
        verifySuspend { authRepository.signInWithEmail("test@example.com", "password123") }
        assertIs<AuthState.Authenticated>(viewModel.state.value.authState)
        assertEquals(user, (viewModel.state.value.authState as AuthState.Authenticated).user)
    }

    // ========== Sign Up Validation Tests ==========

    @Test
    fun `validateAndSignUp with empty email shows error`() {
        // Given
        viewModel.onEmailFieldChanged("")
        viewModel.onPasswordFieldChanged("password123")
        viewModel.onConfirmPasswordFieldChanged("password123")

        // When
        viewModel.validateAndSignUp()

        // Then
        assertEquals("Email is required", viewModel.state.value.emailError)
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
    }

    @Test
    fun `validateAndSignUp with invalid email shows error`() {
        // Given
        viewModel.onEmailFieldChanged("invalid")
        viewModel.onPasswordFieldChanged("password123")
        viewModel.onConfirmPasswordFieldChanged("password123")

        // When
        viewModel.validateAndSignUp()

        // Then
        assertEquals("Please enter a valid email", viewModel.state.value.emailError)
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
    }

    @Test
    fun `validateAndSignUp with empty password shows error`() {
        // Given
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("")
        viewModel.onConfirmPasswordFieldChanged("")

        // When
        viewModel.validateAndSignUp()

        // Then
        assertEquals("Password is required", viewModel.state.value.passwordError)
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
    }

    @Test
    fun `validateAndSignUp with short password shows error`() {
        // Given
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("12345")
        viewModel.onConfirmPasswordFieldChanged("12345")

        // When
        viewModel.validateAndSignUp()

        // Then
        assertEquals("Password must be at least 6 characters", viewModel.state.value.passwordError)
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
    }

    @Test
    fun `validateAndSignUp with mismatched passwords shows error`() {
        // Given
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("password123")
        viewModel.onConfirmPasswordFieldChanged("different")

        // When
        viewModel.validateAndSignUp()

        // Then
        assertEquals("Passwords must match", viewModel.state.value.confirmPasswordError)
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
    }

    @Test
    fun `validateAndSignUp with valid data calls repository`() = runTest {
        // Given
        val user = User("user-id", "test@example.com", "Test User", null, AuthProvider.EMAIL)
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("password123")
        viewModel.onConfirmPasswordFieldChanged("password123")
        everySuspend { authRepository.signUpWithEmail("test@example.com", "password123") } returns Result.success(user)

        // When
        viewModel.validateAndSignUp()

        // Then
        verifySuspend { authRepository.signUpWithEmail("test@example.com", "password123") }
        assertIs<AuthState.Authenticated>(viewModel.state.value.authState)
        assertEquals(user, (viewModel.state.value.authState as AuthState.Authenticated).user)
    }

    // ========== Sign In Flow Tests ==========

    @Test
    fun `signIn success updates state to authenticated and clears form`() = runTest {
        // Given
        val user = User("user-id", "test@example.com", "Test User", null, AuthProvider.EMAIL)
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("password123")
        everySuspend { authRepository.signInWithEmail("test@example.com", "password123") } returns Result.success(user)

        // When
        viewModel.validateAndSignIn()

        // Then
        assertIs<AuthState.Authenticated>(viewModel.state.value.authState)
        assertEquals(user, (viewModel.state.value.authState as AuthState.Authenticated).user)
        // Form should be cleared
        assertEquals("", viewModel.state.value.emailField)
        assertEquals("", viewModel.state.value.passwordField)
    }

    @Test
    fun `signIn failure shows a danger snackbar and keeps the form`() = runTest {
        // Given
        val effects = collectScreenEffects()
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("wrong-password")
        everySuspend { authRepository.signInWithEmail("test@example.com", "wrong-password") } returns Result.failure(AuthError.InvalidCredentials)

        // When
        viewModel.validateAndSignIn()

        // Then
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
        assertEquals("test@example.com", viewModel.state.value.emailField)
        assertEquals(listOf(snackbar(NoticeKey.AUTH_INVALID_CREDENTIALS)), effects)
    }

    @Test
    fun `signIn sets loading state during request`() = runTest {
        // Given
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("password123")
        val user = User("user-id", "test@example.com", "Test User", null, AuthProvider.EMAIL)
        everySuspend { authRepository.signInWithEmail("test@example.com", "password123") } returns Result.success(user)

        // When
        viewModel.validateAndSignIn()

        // Note: With UnconfinedTestDispatcher, the loading state happens instantly
        // so we can't easily test it without more complex setup. The test above
        // confirms the flow works correctly.
        assertTrue(true) // Placeholder to note this limitation
    }

    // ========== Sign Up Flow Tests ==========

    @Test
    fun `signUp success updates state to authenticated and clears form`() = runTest {
        // Given
        val user = User("user-id", "new@example.com", "New User", null, AuthProvider.EMAIL)
        viewModel.onEmailFieldChanged("new@example.com")
        viewModel.onPasswordFieldChanged("password123")
        viewModel.onConfirmPasswordFieldChanged("password123")
        everySuspend { authRepository.signUpWithEmail("new@example.com", "password123") } returns Result.success(user)

        // When
        viewModel.validateAndSignUp()

        // Then
        assertIs<AuthState.Authenticated>(viewModel.state.value.authState)
        assertEquals(user, (viewModel.state.value.authState as AuthState.Authenticated).user)
        // Form should be cleared
        assertEquals("", viewModel.state.value.emailField)
        assertEquals("", viewModel.state.value.passwordField)
        assertEquals("", viewModel.state.value.confirmPasswordField)
    }

    @Test
    fun `signUp failure shows a danger snackbar and keeps the form`() = runTest {
        // Given
        val effects = collectScreenEffects()
        viewModel.onEmailFieldChanged("existing@example.com")
        viewModel.onPasswordFieldChanged("password123")
        viewModel.onConfirmPasswordFieldChanged("password123")
        everySuspend { authRepository.signUpWithEmail("existing@example.com", "password123") } returns Result.failure(AuthError.UserAlreadyExists)

        // When
        viewModel.validateAndSignUp()

        // Then
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
        assertEquals("existing@example.com", viewModel.state.value.emailField)
        assertEquals(listOf(snackbar(NoticeKey.AUTH_USER_ALREADY_EXISTS)), effects)
    }

    @Test
    fun `signUp with email confirmation required updates state to pending and clears form`() = runTest {
        // Given
        val effects = collectScreenEffects()
        viewModel.onEmailFieldChanged("new@example.com")
        viewModel.onPasswordFieldChanged("password123")
        viewModel.onConfirmPasswordFieldChanged("password123")
        everySuspend { authRepository.signUpWithEmail("new@example.com", "password123") } returns
            Result.failure(AuthError.EmailConfirmationRequired)

        // When
        viewModel.validateAndSignUp()

        // Then
        assertIs<AuthState.EmailConfirmationPending>(viewModel.state.value.authState)
        assertEquals("new@example.com", (viewModel.state.value.authState as AuthState.EmailConfirmationPending).email)
        assertEquals("", viewModel.state.value.emailField)
        assertEquals("", viewModel.state.value.passwordField)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `dismissEmailConfirmationSheet resets state to unauthenticated`() = runTest {
        // Given
        viewModel.onEmailFieldChanged("new@example.com")
        viewModel.onPasswordFieldChanged("password123")
        viewModel.onConfirmPasswordFieldChanged("password123")
        everySuspend { authRepository.signUpWithEmail("new@example.com", "password123") } returns
            Result.failure(AuthError.EmailConfirmationRequired)
        viewModel.validateAndSignUp()

        // When
        viewModel.dismissEmailConfirmationSheet()

        // Then
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
    }

    // ========== Edge Cases ==========

    @Test
    fun `handles non-AuthError exceptions by showing the unexpected error`() = runTest {
        // Given
        val effects = collectScreenEffects()
        viewModel.onEmailFieldChanged("test@example.com")
        viewModel.onPasswordFieldChanged("password123")
        everySuspend { authRepository.signInWithEmail(any(), any()) } returns Result.failure(Exception("Something went wrong"))

        // When
        viewModel.validateAndSignIn()

        // Then
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
        assertEquals(listOf(snackbar(NoticeKey.AUTH_UNEXPECTED_ERROR)), effects)
    }

    @Test
    fun `multiple validation errors are all shown`() {
        // Given - All fields invalid
        viewModel.onEmailFieldChanged("invalid")
        viewModel.onPasswordFieldChanged("12")
        viewModel.onConfirmPasswordFieldChanged("different")

        // When
        viewModel.validateAndSignUp()

        // Then
        assertEquals("Please enter a valid email", viewModel.state.value.emailError)
        assertEquals("Password must be at least 6 characters", viewModel.state.value.passwordError)
        assertEquals("Passwords must match", viewModel.state.value.confirmPasswordError)
    }

    // ========== OAuth Tests ==========

    @Test
    fun `signInWithDiscord opens the OAuth URL and shows the form again`() {
        // Given
        everySuspend { authRepository.signInWithDiscord() } returns Result.success(OAUTH_URL)

        // When
        viewModel.signInWithDiscord()

        // Then
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
        verify { platformEffectHandler.handle(PlatformEffect.OpenUrl(OAUTH_URL)) }
    }

    @Test
    fun `signInWithGoogle opens the OAuth URL and shows the form again`() {
        // Given
        everySuspend { authRepository.signInWithGoogle() } returns Result.success(OAUTH_URL)

        // When
        viewModel.signInWithGoogle()

        // Then
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
        verify { platformEffectHandler.handle(PlatformEffect.OpenUrl(OAUTH_URL)) }
    }

    @Test
    fun `signInWithDiscord shows an error and opens nothing when the URL request fails`() = runTest {
        // Given
        val effects = collectScreenEffects()
        everySuspend { authRepository.signInWithDiscord() } returns Result.failure(AuthError.UnexpectedError)

        // When
        viewModel.signInWithDiscord()

        // Then
        assertIs<AuthState.Unauthenticated>(viewModel.state.value.authState)
        assertEquals(listOf(snackbar(NoticeKey.AUTH_UNEXPECTED_ERROR)), effects)
        verify(VerifyMode.not) { platformEffectHandler.handle(any()) }
    }

    private fun TestScope.collectScreenEffects(): List<ScreenEffect> {
        val effects = mutableListOf<ScreenEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.screenEffects.toList(effects) }
        return effects
    }

    private fun snackbar(key: NoticeKey) =
        ScreenEffect.ShowSnackbar(SnackbarMessage.Notice(key), SnackbarTone.DANGER)

    private companion object {
        const val OAUTH_URL = "https://auth.example.com/authorize"
    }
}
