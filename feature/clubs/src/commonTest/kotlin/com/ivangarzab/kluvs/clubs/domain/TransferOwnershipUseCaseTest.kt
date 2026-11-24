package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.UserFacingException
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TransferOwnershipUseCaseTest {

    private val memberRepository = mock<MemberRepository>()
    private val useCase = TransferOwnershipUseCase(memberRepository)

    private val demotedOwner = Member(id = "m1", userId = "u1", name = "Alice", booksRead = 0)
    private val promotedMember = Member(id = "m2", userId = "u2", name = "Bob", booksRead = 0)

    private val params = TransferOwnershipUseCase.Params(
        currentOwnerId = "m1",
        newOwnerId = "m2",
        clubId = "club-1"
    )

    @Test
    fun `invoke succeeds when OWNER transfers to another member`() = runTest {
        everySuspend {
            memberRepository.updateMember(memberId = "m2", clubRoles = mapOf("club-1" to "owner"))
        } returns Result.success(promotedMember)
        everySuspend {
            memberRepository.updateMember(memberId = "m1", clubRoles = mapOf("club-1" to "admin"))
        } returns Result.success(demotedOwner)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke promotes new owner before demoting previous owner`() = runTest {
        everySuspend {
            memberRepository.updateMember(memberId = "m2", clubRoles = mapOf("club-1" to "owner"))
        } returns Result.success(promotedMember)
        everySuspend {
            memberRepository.updateMember(memberId = "m1", clubRoles = mapOf("club-1" to "admin"))
        } returns Result.success(demotedOwner)

        useCase(params, Role.OWNER)

        verifySuspend {
            memberRepository.updateMember(memberId = "m2", clubRoles = mapOf("club-1" to "owner"))
        }
        verifySuspend {
            memberRepository.updateMember(memberId = "m1", clubRoles = mapOf("club-1" to "admin"))
        }
    }

    @Test
    fun `invoke fails with UnauthorizedException when user is ADMIN`() = runTest {
        val result = useCase(params, Role.ADMIN)

        assertTrue(result.isFailure)
        assertIs<UnauthorizedException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke fails with UnauthorizedException when user is MEMBER`() = runTest {
        val result = useCase(params, Role.MEMBER)

        assertTrue(result.isFailure)
        assertIs<UnauthorizedException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke fails when transferring to self`() = runTest {
        val selfParams = TransferOwnershipUseCase.Params(
            currentOwnerId = "m1",
            newOwnerId = "m1",
            clubId = "club-1"
        )

        val result = useCase(selfParams, Role.OWNER)

        assertTrue(result.isFailure)
        assertIs<UserFacingException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke does not demote previous owner when promotion fails`() = runTest {
        everySuspend {
            memberRepository.updateMember(memberId = "m2", clubRoles = mapOf("club-1" to "owner"))
        } returns Result.failure(RuntimeException("Server error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
        verifySuspend(mode = VerifyMode.not) {
            memberRepository.updateMember(memberId = "m1", clubRoles = mapOf("club-1" to "admin"))
        }
    }

    @Test
    fun `invoke propagates demote failure after promotion succeeds`() = runTest {
        everySuspend {
            memberRepository.updateMember(memberId = "m2", clubRoles = mapOf("club-1" to "owner"))
        } returns Result.success(promotedMember)
        everySuspend {
            memberRepository.updateMember(memberId = "m1", clubRoles = mapOf("club-1" to "admin"))
        } returns Result.failure(RuntimeException("Server error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
