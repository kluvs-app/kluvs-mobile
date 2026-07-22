package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.DiscussionNoteRepository
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DeleteDiscussionNoteUseCaseTest {

    private val discussionNoteRepository = mock<DiscussionNoteRepository>()
    private val useCase = DeleteDiscussionNoteUseCase(discussionNoteRepository)

    private val params = DeleteDiscussionNoteUseCase.Params(noteId = "n1")

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        everySuspend { discussionNoteRepository.deleteNote("n1") } returns Result.success(Unit)

        val result = useCase(params)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke calls repository with mapped params`() = runTest {
        everySuspend { discussionNoteRepository.deleteNote("n1") } returns Result.success(Unit)

        useCase(params)

        verifySuspend { discussionNoteRepository.deleteNote("n1") }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend { discussionNoteRepository.deleteNote("n1") } returns
            Result.failure(RuntimeException("Not found"))

        val result = useCase(params)

        assertTrue(result.isFailure)
    }
}
