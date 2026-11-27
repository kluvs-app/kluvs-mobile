package com.ivangarzab.kluvs.auth.presentation

import com.ivangarzab.kluvs.auth.domain.AuthRepository
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
import com.ivangarzab.kluvs.presentation.effect.PlatformEffectHandler
import dev.mokkery.MockMode
import dev.mokkery.mock

/**
 * Builds an [AuthViewModel] with real UseCases over [authRepository] (usually a mock),
 * so ViewModel tests keep stubbing and verifying the repository directly.
 */
internal fun createAuthViewModel(
    authRepository: AuthRepository,
    platformEffectHandler: PlatformEffectHandler = mock(MockMode.autoUnit),
) = AuthViewModel(
    initializeAuth = InitializeAuthUseCase(authRepository),
    observeCurrentUser = ObserveCurrentUserUseCase(authRepository),
    validateCredentials = ValidateCredentialsUseCase(),
    signUpWithEmail = SignUpWithEmailUseCase(authRepository),
    signInWithEmail = SignInWithEmailUseCase(authRepository),
    resetPassword = ResetPasswordUseCase(authRepository),
    signInWithDiscordOAuth = SignInWithDiscordUseCase(authRepository),
    signInWithGoogleOAuth = SignInWithGoogleUseCase(authRepository),
    signInWithAppleIdToken = SignInWithAppleUseCase(authRepository),
    completeOAuthSignIn = HandleOAuthCallbackUseCase(authRepository),
    platformEffectHandler = platformEffectHandler,
)
