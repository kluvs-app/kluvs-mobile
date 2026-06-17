package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role

/**
 * UseCase for changing a member's role within a club.
 *
 * Restrictions:
 * - Requires [Role.ADMIN] or [Role.OWNER] authorization.
 * - The OWNER role cannot be assigned; only ADMIN or MEMBER are valid targets.
 * - The acting user cannot change their own role.
 */
class UpdateMemberRoleUseCase(
    private val memberRepository: MemberRepository
) : BaseAdminUseCase<UpdateMemberRoleUseCase.Params, Member>() {

    override val requiredRoles = ADMIN_AND_ABOVE

    data class Params(
        val memberId: String,
        val clubId: String,
        val currentMemberId: String,
        val newRole: Role
    )

    override suspend fun execute(params: Params): Result<Member> {
        if (params.memberId == params.currentMemberId) {
            return Result.failure(IllegalArgumentException("Cannot change your own role"))
        }
        if (params.newRole == Role.OWNER) {
            return Result.failure(IllegalArgumentException("Cannot assign the OWNER role"))
        }
        Bark.d("Updating member role (Member ID: ${params.memberId}, Club ID: ${params.clubId}, New Role: ${params.newRole})")
        return memberRepository.updateMember(
            memberId = params.memberId,
            clubRoles = mapOf(params.clubId to params.newRole.name.lowercase())
        )
            .onSuccess { Bark.i("Member role updated (Member ID: ${params.memberId}, Club ID: ${params.clubId}, Role: ${params.newRole})") }
            .onFailure { Bark.e("Failed to update member role (Member ID: ${params.memberId}, Club ID: ${params.clubId}). Retry.", it) }
    }
}
