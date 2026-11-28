package com.ivangarzab.kluvs.auth.di

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
import com.ivangarzab.kluvs.auth.presentation.AuthViewModel
import com.ivangarzab.kluvs.auth.presentation.ForgotPasswordViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val authFeatureModule = module {
    // Use Cases
    factoryOf(::InitializeAuthUseCase)
    factoryOf(::ObserveCurrentUserUseCase)
    factoryOf(::ValidateCredentialsUseCase)
    factoryOf(::SignUpWithEmailUseCase)
    factoryOf(::SignInWithEmailUseCase)
    factoryOf(::ResetPasswordUseCase)
    factoryOf(::SignInWithDiscordUseCase)
    factoryOf(::SignInWithGoogleUseCase)
    factoryOf(::SignInWithAppleUseCase)
    factoryOf(::HandleOAuthCallbackUseCase)

    // ViewModels
    factoryOf(::AuthViewModel)
    factoryOf(::ForgotPasswordViewModel)
}
