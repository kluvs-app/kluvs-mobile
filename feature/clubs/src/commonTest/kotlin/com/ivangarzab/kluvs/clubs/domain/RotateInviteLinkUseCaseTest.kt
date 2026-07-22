package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.JoinPolicy
import com.ivangarzab.kluvs.model.Role
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RotateInviteLinkUseCaseTest {

    private val clubRepository = mock<ClubRepository>()
    private val useCase = RotateInviteLinkUseCase(clubRepository)

    private val params = RotateInviteLinkUseCase.Params(clubId = "club-1")

    @Test
    fun `invoke succeeds when user is OWNER deactivating then reactivating`() = runTest {
        val deactivatedClub = Club(id = "club-1", name = "Club", joinPolicy = JoinPolicy.PRIVATE, inviteToken = null)
        val rotatedClub = Club(id = "club-1", name = "Club", joinPolicy = JoinPolicy.INVITE_LINK, inviteToken = "tok-new")
        everySuspend {
            clubRepository.updateClub(clubId = "club-1", joinPolicy = JoinPolicy.PRIVATE)
        } returns Result.success(deactivatedClub)
        everySuspend {
            clubRepository.updateClub(clubId = "club-1", joinPolicy = JoinPolicy.INVITE_LINK)
        } returns Result.success(rotatedClub)

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
        assertEquals("tok-new", result.getOrThrow().inviteToken)
        verifySuspend { clubRepository.updateClub(clubId = "club-1", joinPolicy = JoinPolicy.PRIVATE) }
        verifySuspend { clubRepository.updateClub(clubId = "club-1", joinPolicy = JoinPolicy.INVITE_LINK) }
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
    fun `invoke propagates failure when the deactivation call fails`() = runTest {
        everySuspend {
            clubRepository.updateClub(clubId = "club-1", joinPolicy = JoinPolicy.PRIVATE)
        } returns Result.failure(RuntimeException("Network error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
        verifySuspend(mode = dev.mokkery.verify.VerifyMode.not) {
            clubRepository.updateClub(clubId = "club-1", joinPolicy = JoinPolicy.INVITE_LINK)
        }
    }

    @Test
    fun `invoke surfaces RotateInviteLinkException when reactivation fails after deactivation succeeds`() = runTest {
        val deactivatedClub = Club(id = "club-1", name = "Club", joinPolicy = JoinPolicy.PRIVATE, inviteToken = null)
        everySuspend {
            clubRepository.updateClub(clubId = "club-1", joinPolicy = JoinPolicy.PRIVATE)
        } returns Result.success(deactivatedClub)
        everySuspend {
            clubRepository.updateClub(clubId = "club-1", joinPolicy = JoinPolicy.INVITE_LINK)
        } returns Result.failure(RuntimeException("Network error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
        assertIs<RotateInviteLinkException>(result.exceptionOrNull())
    }
}
