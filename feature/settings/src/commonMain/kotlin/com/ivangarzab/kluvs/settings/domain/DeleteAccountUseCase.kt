package com.ivangarzab.kluvs.settings.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.MemberRepository

/**
 * UseCase that permanently deletes the currently authenticated user's account.
 *
 * Thin wrapper around [MemberRepository.deleteMember], kept for consistency with the module's
 * other use cases (see [ChangePasswordUseCase]) — the backend handles cascading cleanup, club
 * ownership transfer/deletion, and Auth user removal; this UseCase only delegates and logs. The
 * caller is responsible for signing the user out and navigating away on success (see
 * [com.ivangarzab.kluvs.settings.presentation.SettingsViewModel]).
 */
class DeleteAccountUseCase(
    private val memberRepository: MemberRepository
) {
    suspend operator fun invoke(memberId: String): Result<String> {
        Bark.d("Deleting account (Member ID: $memberId)")
        return memberRepository.deleteMember(memberId)
            .onFailure { error ->
                Bark.e("Failed to delete account (Member ID: $memberId).", error)
            }
    }
}
