package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.auth.domain.ResetPasswordUseCase
import com.ivangarzab.kluvs.auth.domain.ValidateCredentialsUseCase
import com.ivangarzab.kluvs.presentation.effect.NoticeKey
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import com.ivangarzab.kluvs.presentation.effect.SnackbarMessage
import com.ivangarzab.kluvs.presentation.effect.SnackbarTone
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ForgotPasswordViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock<AuthRepository>()
        viewModel = ForgotPasswordViewModel(
            validateCredentials = ValidateCredentialsUseCase(),
            resetPassword = ResetPasswordUseCase(authRepository),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is an empty form`() {
        assertEquals(ForgotPasswordState(), viewModel.state.value)
    }

    @Test
    fun `onEmailFieldChanged updates the email and clears its error`() {
        viewModel.onSendResetLink()  // empty email → error

        viewModel.onEmailFieldChanged("test@example.com")

        assertEquals("test@example.com", viewModel.state.value.emailField)
        assertNull(viewModel.state.value.emailError)
    }

    @Test
    fun `onSendResetLink with an empty email shows the field error and sends nothing`() = runTest {
        viewModel.onSendResetLink()

        assertEquals("Email is required", viewModel.state.value.emailError)
        assertFalse(viewModel.state.value.isEmailSent)
        verifySuspend(VerifyMode.not) { authRepository.resetPasswordForEmail(any()) }
    }

    @Test
    fun `onSendResetLink with an invalid email shows the field error`() {
        viewModel.onEmailFieldChanged("invalid-email")

        viewModel.onSendResetLink()

        assertEquals("Please enter a valid email", viewModel.state.value.emailError)
        assertFalse(viewModel.state.value.isEmailSent)
    }

    @Test
    fun `onSendResetLink success shows the sent state`() = runTest {
        val effects = collectScreenEffects()
        everySuspend { authRepository.resetPasswordForEmail("test@example.com") } returns Result.success(Unit)
        viewModel.onEmailFieldChanged("test@example.com")

        viewModel.onSendResetLink()

        assertTrue(viewModel.state.value.isEmailSent)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(effects.isEmpty())
        verifySuspend { authRepository.resetPasswordForEmail("test@example.com") }
    }

    @Test
    fun `onSendResetLink failure shows a danger snackbar and keeps the form`() = runTest {
        val effects = collectScreenEffects()
        everySuspend { authRepository.resetPasswordForEmail("test@example.com") } returns Result.failure(AuthError.UserNotFound)
        viewModel.onEmailFieldChanged("test@example.com")

        viewModel.onSendResetLink()

        assertFalse(viewModel.state.value.isEmailSent)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals("test@example.com", viewModel.state.value.emailField)
        assertEquals(listOf(snackbar(NoticeKey.AUTH_USER_NOT_FOUND)), effects)
    }

    @Test
    fun `onSendResetLink maps a non-AuthError failure to the unexpected error`() = runTest {
        val effects = collectScreenEffects()
        everySuspend { authRepository.resetPasswordForEmail(any()) } returns Result.failure(Exception("boom"))
        viewModel.onEmailFieldChanged("test@example.com")

        viewModel.onSendResetLink()

        assertEquals(listOf(snackbar(NoticeKey.AUTH_UNEXPECTED_ERROR)), effects)
    }

    @Test
    fun `onSendResetLink ignores a repeat tap while a send is running`() = runTest {
        val pending = CompletableDeferred<Result<Unit>>()
        everySuspend { authRepository.resetPasswordForEmail(any()) } calls { pending.await() }
        viewModel.onEmailFieldChanged("test@example.com")

        viewModel.onSendResetLink()
        assertTrue(viewModel.state.value.isLoading)
        viewModel.onSendResetLink()
        pending.complete(Result.success(Unit))

        verifySuspend(VerifyMode.exactly(1)) { authRepository.resetPasswordForEmail(any()) }
        assertTrue(viewModel.state.value.isEmailSent)
    }

    private fun TestScope.collectScreenEffects(): List<ScreenEffect> {
        val effects = mutableListOf<ScreenEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.screenEffects.toList(effects) }
        return effects
    }

    private fun snackbar(key: NoticeKey) =
        ScreenEffect.ShowSnackbar(SnackbarMessage.Notice(key), SnackbarTone.DANGER)
}
