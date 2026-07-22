package com.ivangarzab.kluvs.join.domain

import com.ivangarzab.kluvs.data.repositories.JoinRepository
import com.ivangarzab.kluvs.model.ClubPreview
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PreviewInviteUseCaseTest {

    private lateinit var joinRepository: JoinRepository
    private lateinit var useCase: PreviewInviteUseCase

    @BeforeTest
    fun setup() {
        joinRepository = mock<JoinRepository>()
        useCase = PreviewInviteUseCase(joinRepository)
    }

    @Test
    fun `invoke returns preview on success`() = runTest {
        val preview = ClubPreview(id = "club-1", name = "Sci-Fi Club")
        everySuspend { joinRepository.previewInvite("token-1") } returns Result.success(preview)

        val result = useCase("token-1")

        assertTrue(result.isSuccess)
        assertEquals(preview, result.getOrNull())
        verifySuspend { joinRepository.previewInvite("token-1") }
    }

    @Test
    fun `invoke returns failure for invalid token`() = runTest {
        val exception = Exception("Invalid or expired token")
        everySuspend { joinRepository.previewInvite("bad-token") } returns Result.failure(exception)

        val result = useCase("bad-token")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
