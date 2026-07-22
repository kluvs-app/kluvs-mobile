package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.JoinPolicy
import com.ivangarzab.kluvs.model.Role
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UpdateJoinPolicyUseCaseTest {

    private val clubRepository = mock<ClubRepository>()
    private val useCase = UpdateJoinPolicyUseCase(clubRepository)

    private val stubClub = Club(id = "club-1", name = "Club", joinPolicy = JoinPolicy.INVITE_LINK, inviteToken = "tok-1")
    private val params = UpdateJoinPolicyUseCase.Params(clubId = "club-1", joinPolicy = JoinPolicy.INVITE_LINK)

    @Test
    fun `invoke succeeds when user is OWNER`() = runTest {
        everySuspend {
            clubRepository.updateClub(clubId = "club-1", joinPolicy = JoinPolicy.INVITE_LINK)
        } returns Result.success(stubClub)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
        assertEquals(JoinPolicy.INVITE_LINK, result.getOrThrow().joinPolicy)
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
    fun `invoke propagates repository failure`() = runTest {
        everySuspend {
            clubRepository.updateClub(clubId = "club-1", joinPolicy = JoinPolicy.INVITE_LINK)
        } returns Result.failure(RuntimeException("Network error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
