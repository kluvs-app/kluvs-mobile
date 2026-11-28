package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.auth.domain.ResetPasswordUseCase
import com.ivangarzab.kluvs.auth.domain.ValidateCredentialsUseCase
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelHelperTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var testScope: CoroutineScope
    private lateinit var helper: ForgotPasswordViewModelHelper
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock<AuthRepository>()
        testScope = CoroutineScope(testDispatcher + Job())
        val viewModel = ForgotPasswordViewModel(
            validateCredentials = ValidateCredentialsUseCase(),
            resetPassword = ResetPasswordUseCase(authRepository),
        )
        startKoin {
            modules(
                module {
                    single<ForgotPasswordViewModel> { viewModel }
                    single<CoroutineScope> { testScope }
                }
            )
        }
        helper = ForgotPasswordViewModelHelper()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun observeStateCallsBackWithTheCurrentState() {
        var received: ForgotPasswordState? = null

        val closeable = helper.observeState { received = it }

        assertEquals(ForgotPasswordState(), received)
        closeable.close()
    }

    @Test
    fun onEmailFieldChangedUpdatesState() {
        val states = mutableListOf<ForgotPasswordState>()
        val closeable = helper.observeState { states.add(it) }

        helper.onEmailFieldChanged("test@example.com")

        assertEquals("test@example.com", states.last().emailField)
        closeable.close()
    }

    @Test
    fun onSendResetLinkWithAnInvalidEmailSetsTheFieldError() {
        val states = mutableListOf<ForgotPasswordState>()
        val closeable = helper.observeState { states.add(it) }
        helper.onEmailFieldChanged("invalid-email")

        helper.onSendResetLink()

        assertNotNull(states.last().emailError)
        closeable.close()
    }

    @Test
    fun onSendResetLinkWithAValidEmailShowsTheSentState() {
        everySuspend { authRepository.resetPasswordForEmail("test@example.com") } returns Result.success(Unit)
        val states = mutableListOf<ForgotPasswordState>()
        val closeable = helper.observeState { states.add(it) }
        helper.onEmailFieldChanged("test@example.com")

        helper.onSendResetLink()

        assertTrue(states.last().isEmailSent)
        closeable.close()
    }
}
