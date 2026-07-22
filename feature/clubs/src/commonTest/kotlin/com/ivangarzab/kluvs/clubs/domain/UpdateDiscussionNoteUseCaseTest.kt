package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.DiscussionNoteRepository
import com.ivangarzab.kluvs.model.DiscussionNote
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateDiscussionNoteUseCaseTest {

    private val discussionNoteRepository = mock<DiscussionNoteRepository>()
    private val useCase = UpdateDiscussionNoteUseCase(discussionNoteRepository)

    private val params = UpdateDiscussionNoteUseCase.Params(
        noteId = "n1",
        content = "Updated thoughts"
    )
    private val updatedNote = DiscussionNote(
        id = "n1",
        discussionId = "d1",
        memberId = "m1",
        content = "Updated thoughts"
    )

    @Test
    fun `invoke returns updated note on success`() = runTest {
        everySuspend { discussionNoteRepository.updateNote("n1", "Updated thoughts") } returns Result.success(updatedNote)

        val result = useCase(params)

        assertTrue(result.isSuccess)
        assertEquals(updatedNote, result.getOrNull())
    }

    @Test
    fun `invoke calls repository with mapped params`() = runTest {
        everySuspend { discussionNoteRepository.updateNote("n1", "Updated thoughts") } returns Result.success(updatedNote)

        useCase(params)

        verifySuspend { discussionNoteRepository.updateNote("n1", "Updated thoughts") }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend { discussionNoteRepository.updateNote("n1", "Updated thoughts") } returns
            Result.failure(RuntimeException("Server error"))

        val result = useCase(params)

        assertTrue(result.isFailure)
    }
}
