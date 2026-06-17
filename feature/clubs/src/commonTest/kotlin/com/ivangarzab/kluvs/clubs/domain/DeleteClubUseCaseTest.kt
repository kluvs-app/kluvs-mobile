package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Role
import dev.mokkery.everySuspend
import dev.mokkery.answering.returns
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DeleteClubUseCaseTest {

    private val clubRepository = mock<ClubRepository>()
    private val useCase = DeleteClubUseCase(clubRepository)

    private val params = DeleteClubUseCase.Params(clubId = "club-1")

    @Test
    fun `invoke succeeds when user is OWNER`() = runTest {
        everySuspend { clubRepository.deleteClub(clubId = "club-1") } returns Result.success("deleted")

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isSuccess)
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
        everySuspend { clubRepository.deleteClub(clubId = "club-1") } returns
            Result.failure(RuntimeException("Server error"))

        val result = useCase(params, Role.OWNER)

        assertTrue(result.isFailure)
    }
}
