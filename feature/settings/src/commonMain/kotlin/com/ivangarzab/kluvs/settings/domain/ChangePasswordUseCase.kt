package com.ivangarzab.kluvs.settings.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.AuthRepository

/**
 * UseCase that changes the currently authenticated user's password.
 *
 * Field-level validation (blank, minimum length, confirmation match) is done by
 * [com.ivangarzab.kluvs.settings.presentation.SettingsViewModel] before this is invoked — this
 * UseCase only delegates to [AuthRepository].
 */
class ChangePasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(newPassword: String): Result<Unit> {
        Bark.d("Changing password")
        return authRepository.changePassword(newPassword)
            .onFailure { error ->
                Bark.e("Failed to change password.", error)
            }
    }
}
