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

class CreateDiscussionNoteUseCaseTest {

    private val discussionNoteRepository = mock<DiscussionNoteRepository>()
    private val useCase = CreateDiscussionNoteUseCase(discussionNoteRepository)

    private val params = CreateDiscussionNoteUseCase.Params(
        discussionId = "d1",
        content = "Great chapter"
    )
    private val createdNote = DiscussionNote(
        id = "n1",
        discussionId = "d1",
        memberId = "m1",
        content = "Great chapter"
    )

    @Test
    fun `invoke returns created note on success`() = runTest {
        everySuspend { discussionNoteRepository.createNote("d1", "Great chapter") } returns Result.success(createdNote)

        val result = useCase(params)

        assertTrue(result.isSuccess)
        assertEquals(createdNote, result.getOrNull())
    }

    @Test
    fun `invoke calls repository with mapped params`() = runTest {
        everySuspend { discussionNoteRepository.createNote("d1", "Great chapter") } returns Result.success(createdNote)

        useCase(params)

        verifySuspend { discussionNoteRepository.createNote("d1", "Great chapter") }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        everySuspend { discussionNoteRepository.createNote("d1", "Great chapter") } returns
            Result.failure(RuntimeException("Server error"))

        val result = useCase(params)

        assertTrue(result.isFailure)
    }
}
