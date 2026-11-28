package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.auth.domain.HandleOAuthCallbackUseCase
import com.ivangarzab.kluvs.auth.domain.InitializeAuthUseCase
import com.ivangarzab.kluvs.auth.domain.ObserveCurrentUserUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithAppleUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithDiscordUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithEmailUseCase
import com.ivangarzab.kluvs.auth.domain.SignInWithGoogleUseCase
import com.ivangarzab.kluvs.auth.domain.SignUpWithEmailUseCase
import com.ivangarzab.kluvs.auth.domain.ValidateCredentialsUseCase

/**
 * Builds an [AuthViewModel] with real UseCases over [authRepository] (usually a mock),
 * so ViewModel tests keep stubbing and verifying the repository directly.
 */
internal fun createAuthViewModel(authRepository: AuthRepository) = AuthViewModel(
    initializeAuth = InitializeAuthUseCase(authRepository),
    observeCurrentUser = ObserveCurrentUserUseCase(authRepository),
    validateCredentials = ValidateCredentialsUseCase(),
    signUpWithEmail = SignUpWithEmailUseCase(authRepository),
    signInWithEmail = SignInWithEmailUseCase(authRepository),
    signInWithDiscordOAuth = SignInWithDiscordUseCase(authRepository),
    signInWithGoogleOAuth = SignInWithGoogleUseCase(authRepository),
    signInWithAppleIdToken = SignInWithAppleUseCase(authRepository),
    completeOAuthSignIn = HandleOAuthCallbackUseCase(authRepository),
)
