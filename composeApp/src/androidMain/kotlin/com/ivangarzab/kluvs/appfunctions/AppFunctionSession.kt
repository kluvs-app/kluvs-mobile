package com.ivangarzab.kluvs.appfunctions

import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionPermissionRequiredException
import com.ivangarzab.kluvs.auth.domain.AuthError
import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member

/**
 * Resolves who an AppFunction is acting for.
 *
 * AppFunctions usually run while Kluvs is in the background, where Supabase pauses its session
 * (requests would go out unauthenticated) even though [AuthRepository.currentUser] still holds
 * the user. [AuthRepository.initialize] reloads that paused session and never signs anyone out,
 * so it runs before every request rather than trusting the in-memory user.
 */
internal class AppFunctionSession(
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository,
) {

    /** The signed-in user's auth ID; throws if nobody is signed in. */
    suspend fun requireUserId(): String {
        val user = authRepository.initialize().getOrElse { error ->
            throw if (error == AuthError.NoConnection) {
                AppFunctionAppUnknownException("Couldn't reach Kluvs. Check your internet connection.")
            } else {
                AppFunctionAppUnknownException("Couldn't confirm your Kluvs sign-in. Try again.")
            }
        }
        return user?.id ?: throw AppFunctionPermissionRequiredException("Sign in to Kluvs to use this feature.")
    }

    suspend fun requireMember(forceRefresh: Boolean = false): Member =
        memberRepository.getMemberByUserId(requireUserId(), forceRefresh).getOrThrowAppFunction()
}
