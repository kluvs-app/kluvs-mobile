package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RemoveMemberUseCaseTest {

    private val memberRepository = mock<MemberRepository>()
    private val useCase = RemoveMemberUseCase(memberRepository)

    private val targetClub = Club(id = "club-1", name = "Test Club")
    private val otherClub = Club(id = "club-2", name = "Other Club")
    private val targetMember = Member(
        id = "m2", userId = "u2", name = "Bob", booksRead = 0,
        clubs = listOf(targetClub, otherClub)
    )
    private val updatedMember = targetMember.copy(clubs = listOf(otherClub))

    private val params = RemoveMemberUseCase.Params(
        memberId = "m2",
        clubId = "club-1",
        currentMemberId = "m1"
    )

    @Test
    fun `invoke succeeds when OWNER removes another member`() = runTest {
        everySuspend { memberRepository.getMember("m2") } returns Result.success(targetMember)
        everySuspend { memberRepository.updateMember(memberId = "m2", clubIds = listOf("club-2")) } returns
            Result.success(updatedMember)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke strips only the target club from member clubs`() = runTest {
        everySuspend { memberRepository.getMember("m2") } returns Result.success(targetMember)
        everySuspend { memberRepository.updateMember(memberId = "m2", clubIds = listOf("club-2")) } returns
            Result.success(updatedMember)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
        // The other club is preserved
        assertTrue(result.getOrThrow().clubs?.any { it.id == "club-2" } == true)
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
    fun `invoke fails when trying to remove self`() = runTest {
        val selfParams = RemoveMemberUseCase.Params(
            memberId = "m1",
            clubId = "club-1",
            currentMemberId = "m1"
        )

        val result = useCase(selfParams, Role.OWNER)

        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates fetch failure`() = runTest {
        everySuspend { memberRepository.getMember("m2") } returns
            Result.failure(RuntimeException("Not found"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke propagates update failure`() = runTest {
        everySuspend { memberRepository.getMember("m2") } returns Result.success(targetMember)
        everySuspend { memberRepository.updateMember(memberId = "m2", clubIds = listOf("club-2")) } returns
            Result.failure(RuntimeException("Server error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
