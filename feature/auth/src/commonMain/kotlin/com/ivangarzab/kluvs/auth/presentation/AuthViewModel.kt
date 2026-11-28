package com.ivangarzab.kluvs.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.auth.domain.HandleOAuthCallbackUseCase
import com.ivangarzab.kluvs.auth.domain.InitializeAuthUseCase
import com.ivangarzab.kluvs.auth.domain.ObserveCurrentUserUseCase
import com.ivangarzab.kluvs.auth.domain.ResetPasswordUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithAppleUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithDiscordUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithEmailUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithGoogleUseCase
import com.ivangarzab.kluvs.auth.domain.SignUpWithEmailUseCase
import com.ivangarzab.kluvs.auth.domain.ValidateCredentialsUseCase
import com.ivangarzab.kluvs.model.User
import com.ivangarzab.kluvs.presentation.effect.EffectChannel
import com.ivangarzab.kluvs.presentation.effect.PlatformEffect
import com.ivangarzab.kluvs.presentation.effect.PlatformEffectHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The purpose of this [ViewModel] class is to handle authentication logic for the
 * presentation layer.
 */
class AuthViewModel(
    private val initializeAuth: InitializeAuthUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val validateCredentials: ValidateCredentialsUseCase,
    private val signUpWithEmail: SignUpWithEmailUseCase,
    private val signInWithEmail: SignInWithEmailUseCase,
    private val resetPassword: ResetPasswordUseCase,
    private val signInWithDiscordOAuth: SignInWithDiscordUseCase,
    private val signInWithGoogleOAuth: SignInWithGoogleUseCase,
    private val signInWithAppleIdToken: SignInWithAppleUseCase,
    private val completeOAuthSignIn: HandleOAuthCallbackUseCase,
    platformEffectHandler: PlatformEffectHandler,
) : ViewModel() {

    private val platformEffects = EffectChannel<PlatformEffect>().apply { handleWith(viewModelScope, platformEffectHandler::handle) }

    private val _state = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordUiState())
    val forgotPasswordState: StateFlow<ForgotPasswordUiState> = _forgotPasswordState.asStateFlow()

    init {
        Bark.d("AuthViewModel initializing")
        viewModelScope.launch {
            initializeAuth()
            observeCurrentUser().collect { user: User? ->
                _state.update {
                    user?.let {
                        Bark.i("Authentication state: Authenticated (User: ${it.email})")
                        AuthState.Authenticated(it)
                    } ?: {
                        Bark.d("Authentication state: Unauthenticated")
                        AuthState.Unauthenticated
                    }()
                }
            }
        }
    }

    fun onEmailFieldChanged(value: String) {
        _uiState.update { it.copy(emailField = value, emailError = null) }
    }

    fun onPasswordFieldChanged(value: String) {
        _uiState.update { it.copy(passwordField = value, passwordError = null) }
    }

    fun onConfirmPasswordFieldChanged(value: String) {
        _uiState.update { it.copy(confirmPasswordField = value, confirmPasswordError = null) }
    }

    fun validateAndSignIn() {
        val form = _uiState.value
        val errors = validateCredentials(email = form.emailField, password = form.passwordField)
        if (errors.hasErrors) {
            _uiState.update { it.copy(emailError = errors.email, passwordError = errors.password) }
            return  // Don't proceed with API call
        }

        signIn()
    }

    fun validateAndSignUp() {
        val form = _uiState.value
        val errors = validateCredentials(
            email = form.emailField,
            password = form.passwordField,
            confirmPassword = form.confirmPasswordField
        )
        if (errors.hasErrors) {
            _uiState.update {
                it.copy(
                    emailError = errors.email,
                    passwordError = errors.password,
                    confirmPasswordError = errors.confirmPassword
                )
            }
            return  // Don't proceed with API call
        }

        signUp()
    }

    private fun signUp() = viewModelScope.launch {
        Bark.d("Email sign up initiated")
        _state.update { AuthState.Loading }
        signUpWithEmail(
            email = uiState.value.emailField,
            password = uiState.value.passwordField
        ).onSuccess { user ->
            Bark.i("User signed up and authenticated (Email: ${user.email})")
            _state.update { AuthState.Authenticated(user) }
            clearForm()
        }.onFailure { error ->
            if (error is AuthError.EmailConfirmationRequired) {
                Bark.i("Sign up succeeded, pending email confirmation")
                _state.update { AuthState.EmailConfirmationPending(uiState.value.emailField) }
                clearForm()
            } else {
                Bark.e("Sign up failed. Check email format and try again.", error)
                _state.update {
                    AuthState.Error(error as? AuthError ?: AuthError.UnexpectedError)
                }
            }
        }
    }

    private fun signIn() = viewModelScope.launch {
        Bark.d("Email sign in initiated")
        _state.update { AuthState.Loading }
        signInWithEmail(
            email = uiState.value.emailField,
            password = uiState.value.passwordField
        ).onSuccess { user ->
            Bark.i("User signed in (Email: ${user.email})")
            _state.update { AuthState.Authenticated(user) }
            clearForm()
        }.onFailure { error ->
            Bark.e("Sign in failed. Verify credentials and retry.", error)
            _state.update {
                AuthState.Error(error as? AuthError ?: AuthError.UnexpectedError)
            }
        }
    }

    fun onForgotPasswordEmailChanged(value: String) {
        _forgotPasswordState.update { it.copy(emailField = value, emailError = null, generalError = null) }
    }

    fun sendPasswordResetEmail() {
        val emailError = validateCredentials(email = _forgotPasswordState.value.emailField).email
        if (emailError != null) {
            _forgotPasswordState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            Bark.d("Password reset email requested")
            _forgotPasswordState.update { it.copy(isLoading = true, generalError = null) }
            resetPassword(_forgotPasswordState.value.emailField)
                .onSuccess {
                    Bark.i("Password reset email sent")
                    _forgotPasswordState.update { it.copy(isLoading = false, isEmailSent = true) }
                }
                .onFailure { error ->
                    Bark.e("Password reset email failed to send. User may need to retry.", error)
                    _forgotPasswordState.update {
                        it.copy(
                            isLoading = false,
                            generalError = error as? AuthError ?: AuthError.UnexpectedError
                        )
                    }
                }
        }
    }

    /** Opens [sheet], replacing any sheet already open. */
    fun onOpenSheet(sheet: AuthSheet) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    /**
     * Closes the open sheet: Done, Cancel, the close button, swipe down, back, or a tap outside.
     * Closing Forgot Password clears it so it opens empty next time.
     */
    fun onDismissSheet() {
        if (_uiState.value.activeSheet == AuthSheet.ForgotPassword) {
            _forgotPasswordState.update { ForgotPasswordUiState() }
        }
        _uiState.update { it.copy(activeSheet = null) }
    }

    /**
     * Initiates Discord OAuth sign-in flow: opens the OAuth URL in the browser, then waits
     * for [handleOAuthCallback].
     */
    fun signInWithDiscord() = viewModelScope.launch {
        Bark.d("Discord OAuth initiated")
        _state.update { AuthState.Loading }
        signInWithDiscordOAuth()
            .onSuccess { url ->
                Bark.d("Discord OAuth URL generated")
                _state.update { AuthState.Unauthenticated }
                platformEffects.send(PlatformEffect.OpenUrl(url))
            }
            .onFailure { error ->
                Bark.e("Discord OAuth failed. Sign in via Discord unavailable.", error)
                _state.update {
                    AuthState.Error(error as? AuthError ?: AuthError.UnexpectedError)
                }
            }
    }

    /**
     * Initiates Google OAuth sign-in flow: opens the OAuth URL in the browser, then waits
     * for [handleOAuthCallback].
     */
    fun signInWithGoogle() = viewModelScope.launch {
        Bark.d("Google OAuth initiated")
        _state.update { AuthState.Loading }
        signInWithGoogleOAuth()
            .onSuccess { url ->
                Bark.d("Google OAuth URL generated")
                _state.update { AuthState.Unauthenticated }
                platformEffects.send(PlatformEffect.OpenUrl(url))
            }
            .onFailure { error ->
                Bark.e("Google OAuth failed. Sign in via Google unavailable.", error)
                _state.update {
                    AuthState.Error(error as? AuthError ?: AuthError.UnexpectedError)
                }
            }
    }

    /**
     * Resets state to [AuthState.Unauthenticated].
     * Used when the email confirmation sheet has been dismissed.
     */
    fun dismissEmailConfirmationSheet() {
        _state.update { AuthState.Unauthenticated }
    }

    /**
     * Handles OAuth callback from deep link.
     * Should be called when the app receives an OAuth redirect.
     */
    fun handleOAuthCallback(callbackUrl: String) = viewModelScope.launch {
        Bark.d("OAuth callback processing started")
        _state.update { AuthState.Loading }
        completeOAuthSignIn(callbackUrl)
            .onSuccess { user ->
                Bark.i("User authenticated via OAuth (Email: ${user.email})")
                _state.update { AuthState.Authenticated(user) }
                clearForm()
            }
            .onFailure { error ->
                Bark.e("OAuth callback processing failed. User will need to retry OAuth sign in.", error)
                _state.update {
                    AuthState.Error(error as? AuthError ?: AuthError.UnexpectedError)
                }
            }
    }

    /**
     * Signs in with Apple using a native ID token.
     * Called from iOS after native Apple Sign In returns the identity token.
     */
    fun signInWithApple(idToken: String) = viewModelScope.launch {
        Bark.d("Apple native sign in initiated")
        _state.update { AuthState.Loading }
        signInWithAppleIdToken(idToken)
            .onSuccess { user ->
                Bark.i("User authenticated via Apple ID (Email: ${user.email})")
                _state.update { AuthState.Authenticated(user) }
                clearForm()
            }
            .onFailure { error ->
                Bark.e("Apple sign in failed. Please retry.", error)
                _state.update {
                    AuthState.Error(error as? AuthError ?: AuthError.UnexpectedError)
                }
            }
    }

    private fun clearForm() {
        _uiState.update { AuthUiState() }
    }
}

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val error: AuthError) : AuthState()
    /** Email sign up succeeded but requires email confirmation before a session is created */
    data class EmailConfirmationPending(val email: String) : AuthState()
}