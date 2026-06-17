package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UpdateMemberRoleUseCaseTest {

    private val memberRepository = mock<MemberRepository>()
    private val useCase = UpdateMemberRoleUseCase(memberRepository)

    private val stubMember = Member(id = "m2", userId = "u2", name = "Bob", booksRead = 0, clubs = null)
    private val params = UpdateMemberRoleUseCase.Params(
        memberId = "m2",
        clubId = "club-1",
        currentMemberId = "m1",
        newRole = Role.ADMIN
    )

    @Test
    fun `invoke succeeds when OWNER promotes member to ADMIN`() = runTest {
        everySuspend { memberRepository.updateMember(memberId = "m2", clubRoles = mapOf("club-1" to "admin")) } returns Result.success(stubMember)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke succeeds when ADMIN promotes member to ADMIN`() = runTest {
        everySuspend { memberRepository.updateMember(memberId = "m2", clubRoles = mapOf("club-1" to "admin")) } returns Result.success(stubMember)

        val result = useCase(params, Role.ADMIN)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke fails with UnauthorizedException when user is MEMBER`() = runTest {
        val result = useCase(params, Role.MEMBER)

        assertTrue(result.isFailure)
        assertIs<UnauthorizedException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke fails when trying to assign OWNER role`() = runTest {
        val ownerParams = UpdateMemberRoleUseCase.Params(
            memberId = "m2",
            clubId = "club-1",
            currentMemberId = "m1",
            newRole = Role.OWNER
        )

        val result = useCase(ownerParams, Role.OWNER)

        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke fails when trying to change own role`() = runTest {
        val selfParams = UpdateMemberRoleUseCase.Params(
            memberId = "m1",
            clubId = "club-1",
            currentMemberId = "m1",
            newRole = Role.ADMIN
        )

        val result = useCase(selfParams, Role.OWNER)

        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend { memberRepository.updateMember(memberId = "m2", clubRoles = mapOf("club-1" to "admin")) } returns
            Result.failure(RuntimeException("Network error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
