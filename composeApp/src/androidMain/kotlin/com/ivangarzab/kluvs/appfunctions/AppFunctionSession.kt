package com.ivangarzab.kluvs.appfunctions

import androidx.appfunctions.AppFunctionDeniedException
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionPermissionRequiredException
import com.ivangarzab.kluvs.auth.domain.AuthRepository
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role

/**
 * Resolves who an AppFunction is acting for. AppFunctions can run in a cold process where
 * no screen (and so no AppCoordinator) has restored the auth session yet, so this restores
 * it on demand instead of trusting [AuthRepository.currentUser] alone.
 */
internal class AppFunctionSession(
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository,
) {

    /** The signed-in user's auth ID; throws if nobody is signed in. */
    suspend fun requireUserId(): String {
        authRepository.currentUser.value?.let { return it.id }
        return authRepository.initialize().getOrNull()?.id
            ?: throw AppFunctionPermissionRequiredException("Sign in to Kluvs to use this feature.")
    }

    suspend fun requireMember(forceRefresh: Boolean = false): Member =
        memberRepository.getMemberByUserId(requireUserId(), forceRefresh).getOrThrowAppFunction()

    suspend fun requireMemberId(): String = requireMember().id

    /**
     * The signed-in member's role in [clubId]. Force-refreshes like GetMemberClubsUseCase does,
     * since a stale cached role would wrongly allow or block role-gated actions.
     */
    suspend fun requireClubRole(clubId: String): Role {
        val club = requireMember(forceRefresh = true).clubs?.find { it.id == clubId }
            ?: throw AppFunctionElementNotFoundException("You aren't a member of a club with ID $clubId.")
        return club.role
            ?: throw AppFunctionDeniedException("Couldn't confirm your role in this club.")
    }
}
