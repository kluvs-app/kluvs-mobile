package com.ivangarzab.kluvs.auth.presentation

import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.auth.domain.HandleOAuthCallbackUseCase
import com.ivangarzab.kluvs.auth.domain.InitializeAuthUseCase
import com.ivangarzab.kluvs.auth.domain.ObserveCurrentUserUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithAppleUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithDiscordUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithEmailUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithGoogleUseCase
import com.ivangarzab.kluvs.auth.domain.SignUpWithEmailUseCase
import com.ivangarzab.kluvs.auth.domain.ValidateCredentialsUseCase
import com.ivangarzab.kluvs.model.User
import com.ivangarzab.kluvs.presentation.effect.PlatformEffect
import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

/**
 * Drives the Sign in and Sign up screens: the email form, OAuth and native Apple sign-in.
 * A failure returns the screen to [AuthState.Unauthenticated] with the form as it was, and shows
 * a danger snackbar.
 */
class AuthViewModel(
    private val initializeAuth: InitializeAuthUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val validateCredentials: ValidateCredentialsUseCase,
    private val signUpWithEmail: SignUpWithEmailUseCase,
    private val signInWithEmail: SignInWithEmailUseCase,
    private val signInWithDiscordOAuth: SignInWithDiscordUseCase,
    private val signInWithGoogleOAuth: SignInWithGoogleUseCase,
    private val signInWithAppleIdToken: SignInWithAppleUseCase,
    private val completeOAuthSignIn: HandleOAuthCallbackUseCase,
) : BaseViewModel<AuthScreenState>(AuthScreenState()) {

    init {
        Bark.d("AuthViewModel initializing")
        viewModelScope.launch {
            initializeAuth()
            observeCurrentUser().collect { user: User? ->
                val authState = if (user != null) {
                    Bark.i("Authentication state: Authenticated (User: ${user.email})")
                    AuthState.Authenticated(user)
                } else {
                    Bark.d("Authentication state: Unauthenticated")
                    AuthState.Unauthenticated
                }
                setAuthState(authState)
            }
        }
    }

    fun onEmailFieldChanged(value: String) {
        updateState { it.copy(emailField = value, emailError = null) }
    }

    fun onPasswordFieldChanged(value: String) {
        updateState { it.copy(passwordField = value, passwordError = null) }
    }

    fun onConfirmPasswordFieldChanged(value: String) {
        updateState { it.copy(confirmPasswordField = value, confirmPasswordError = null) }
    }

    fun validateAndSignIn() {
        val errors = validateCredentials(email = currentState.emailField, password = currentState.passwordField)
        if (errors.hasErrors) {
            updateState { it.copy(emailError = errors.email, passwordError = errors.password) }
            return  // Don't proceed with API call
        }

        signIn()
    }

    fun validateAndSignUp() {
        val errors = validateCredentials(
            email = currentState.emailField,
            password = currentState.passwordField,
            confirmPassword = currentState.confirmPasswordField
        )
        if (errors.hasErrors) {
            updateState {
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
        setAuthState(AuthState.Loading)
        signUpWithEmail(
            email = currentState.emailField,
            password = currentState.passwordField
        ).onSuccess { user ->
            Bark.i("User signed up and authenticated (Email: ${user.email})")
            onSignedIn(user)
        }.onFailure { error ->
            if (error is AuthError.EmailConfirmationRequired) {
                Bark.i("Sign up succeeded, pending email confirmation")
                val email = currentState.emailField
                updateState { AuthScreenState(authState = AuthState.EmailConfirmationPending(email)) }
            } else {
                Bark.e("Sign up failed. Check email format and try again.", error)
                onFailure(error)
            }
        }
    }

    private fun signIn() = viewModelScope.launch {
        Bark.d("Email sign in initiated")
        setAuthState(AuthState.Loading)
        signInWithEmail(
            email = currentState.emailField,
            password = currentState.passwordField
        ).onSuccess { user ->
            Bark.i("User signed in (Email: ${user.email})")
            onSignedIn(user)
        }.onFailure { error ->
            Bark.e("Sign in failed. Verify credentials and retry.", error)
            onFailure(error)
        }
    }

    /**
     * Initiates Discord OAuth sign-in flow: opens the OAuth URL in the browser, then waits
     * for [handleOAuthCallback].
     */
    fun signInWithDiscord() = viewModelScope.launch {
        Bark.d("Discord OAuth initiated")
        setAuthState(AuthState.Loading)
        signInWithDiscordOAuth()
            .onSuccess { url ->
                Bark.d("Discord OAuth URL generated")
                setAuthState(AuthState.Unauthenticated)
                send(PlatformEffect.OpenUrl(url))
            }
            .onFailure { error ->
                Bark.e("Discord OAuth failed. Sign in via Discord unavailable.", error)
                onFailure(error)
            }
    }

    /**
     * Initiates Google OAuth sign-in flow: opens the OAuth URL in the browser, then waits
     * for [handleOAuthCallback].
     */
    fun signInWithGoogle() = viewModelScope.launch {
        Bark.d("Google OAuth initiated")
        setAuthState(AuthState.Loading)
        signInWithGoogleOAuth()
            .onSuccess { url ->
                Bark.d("Google OAuth URL generated")
                setAuthState(AuthState.Unauthenticated)
                send(PlatformEffect.OpenUrl(url))
            }
            .onFailure { error ->
                Bark.e("Google OAuth failed. Sign in via Google unavailable.", error)
                onFailure(error)
            }
    }

    /**
     * Resets state to [AuthState.Unauthenticated].
     * Used when the email confirmation sheet has been dismissed.
     */
    fun dismissEmailConfirmationSheet() {
        setAuthState(AuthState.Unauthenticated)
    }

    /**
     * Handles OAuth callback from deep link.
     * Should be called when the app receives an OAuth redirect.
     */
    fun handleOAuthCallback(callbackUrl: String) = viewModelScope.launch {
        Bark.d("OAuth callback processing started")
        setAuthState(AuthState.Loading)
        completeOAuthSignIn(callbackUrl)
            .onSuccess { user ->
                Bark.i("User authenticated via OAuth (Email: ${user.email})")
                onSignedIn(user)
            }
            .onFailure { error ->
                Bark.e("OAuth callback processing failed. User will need to retry OAuth sign in.", error)
                onFailure(error)
            }
    }

    /**
     * Signs in with Apple using a native ID token.
     * Called from iOS after native Apple Sign In returns the identity token.
     */
    fun signInWithApple(idToken: String) = viewModelScope.launch {
        Bark.d("Apple native sign in initiated")
        setAuthState(AuthState.Loading)
        signInWithAppleIdToken(idToken)
            .onSuccess { user ->
                Bark.i("User authenticated via Apple ID (Email: ${user.email})")
                onSignedIn(user)
            }
            .onFailure { error ->
                Bark.e("Apple sign in failed. Please retry.", error)
                onFailure(error)
            }
    }

    private fun setAuthState(authState: AuthState) {
        updateState { it.copy(authState = authState) }
    }

    /** Signed in: clears the form. */
    private fun onSignedIn(user: User) {
        updateState { AuthScreenState(authState = AuthState.Authenticated(user)) }
    }

    /** Back to the form as the user left it, with the failure as a danger snackbar. */
    private fun onFailure(error: Throwable) {
        setAuthState(AuthState.Unauthenticated)
        send(error.toAuthErrorSnackbar())
    }
}
