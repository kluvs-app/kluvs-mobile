package com.ivangarzab.kluvs.settings.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository

/**
 * UseCase that permanently deletes the currently authenticated user's account.
 *
 * Delegates to [MemberRepository.deleteMember] — the backend handles cascading cleanup, club
 * ownership transfer/deletion, and Auth user removal. The caller is responsible for signing the
 * user out and navigating away on success (see
 * [com.ivangarzab.kluvs.settings.presentation.SettingsViewModel]).
 *
 * A failed delete can be ambiguous: the backend may have deleted the account before the response
 * was lost (timeout, dropped connection). So on failure this asks the server whether the account
 * still exists, and reports success if it's gone — a deleted account must always lead to sign-out,
 * never to an error that leaves the user inside the app.
 */
class DeleteAccountUseCase(
    private val memberRepository: MemberRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(memberId: String): Result<Unit> {
        Bark.d("Deleting account (Member ID: $memberId)")
        val error = memberRepository.deleteMember(memberId).exceptionOrNull()
            ?: return Result.success(Unit)

        if (authRepository.currentUserExists().getOrNull() == false) {
            Bark.w("Account deletion reported failure, but the account no longer exists. Treating as deleted (Member ID: $memberId).", error)
            return Result.success(Unit)
        }

        Bark.e("Failed to delete account (Member ID: $memberId).", error)
        return Result.failure(error)
    }
}
