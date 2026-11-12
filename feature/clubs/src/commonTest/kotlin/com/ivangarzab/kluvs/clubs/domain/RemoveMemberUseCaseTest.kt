package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.UserFacingException
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RemoveMemberUseCaseTest {

    private val memberRepository = mock<MemberRepository>()
    private val useCase = RemoveMemberUseCase(memberRepository)

    private val params = RemoveMemberUseCase.Params(
        memberId = "m2",
        clubId = "club-1",
        currentMemberId = "m1"
    )

    @Test
    fun `invoke succeeds when OWNER removes another member`() = runTest {
        everySuspend { memberRepository.removeMemberFromClub("m2", "club-1") } returns Result.success(Unit)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
        verifySuspend { memberRepository.removeMemberFromClub("m2", "club-1") }
    }

    @Test
    fun `invoke removes from the target club only and never rewrites the member clubs`() = runTest {
        everySuspend { memberRepository.removeMemberFromClub("m2", "club-1") } returns Result.success(Unit)

        useCase(params, Role.OWNER)

        // The old flow fetched the member and replaced their club list (403 for non-owners of
        // the target record). Neither call may happen any more.
        verifySuspend(mode = VerifyMode.not) { memberRepository.getMember(any(), any()) }
        verifySuspend(mode = VerifyMode.not) {
            memberRepository.updateMember(any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `invoke fails with UnauthorizedException when user is ADMIN`() = runTest {
        val result = useCase(params, Role.ADMIN)

        assertTrue(result.isFailure)
        assertIs<UnauthorizedException>(result.exceptionOrNull())
        verifySuspend(mode = VerifyMode.not) { memberRepository.removeMemberFromClub("m2", "club-1") }
    }

    @Test
    fun `invoke fails with UnauthorizedException when user is MEMBER`() = runTest {
        val result = useCase(params, Role.MEMBER)

        assertTrue(result.isFailure)
        assertIs<UnauthorizedException>(result.exceptionOrNull())
        verifySuspend(mode = VerifyMode.not) { memberRepository.removeMemberFromClub("m2", "club-1") }
    }

    @Test
    fun `invoke fails when trying to remove self`() = runTest {
        val selfParams = RemoveMemberUseCase.Params(
            memberId = "m1",
            clubId = "club-1",
            currentMemberId = "m1"
        )

        val result = useCase(selfParams, Role.OWNER)

        assertTrue(result.isFailure)
        assertIs<UserFacingException>(result.exceptionOrNull())
        verifySuspend(mode = VerifyMode.not) { memberRepository.removeMemberFromClub("m1", "club-1") }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend { memberRepository.removeMemberFromClub("m2", "club-1") } returns
            Result.failure(RuntimeException("Forbidden"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
        assertEquals("Forbidden", result.exceptionOrNull()?.message)
    }
}
