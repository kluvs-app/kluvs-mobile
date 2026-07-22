package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.kluvs.data.repositories.DiscussionNoteRepository
import com.ivangarzab.kluvs.model.DiscussionNote
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetDiscussionNoteUseCaseTest {

    private val discussionNoteRepository = mock<DiscussionNoteRepository>()
    private val useCase = GetDiscussionNoteUseCase(discussionNoteRepository)

    private val stubNote = DiscussionNote(
        id = "n1",
        discussionId = "d1",
        memberId = "m1",
        content = "Great chapter"
    )

    @Test
    fun `invoke returns note on success`() = runTest {
        everySuspend { discussionNoteRepository.getNote("d1") } returns Result.success(stubNote)

        val result = useCase("d1")

        assertTrue(result.isSuccess)
        assertEquals(stubNote, result.getOrNull())
    }

    @Test
    fun `invoke returns null when no note exists`() = runTest {
        everySuspend { discussionNoteRepository.getNote("d1") } returns Result.success(null)

        val result = useCase("d1")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `invoke propagates failure`() = runTest {
        everySuspend { discussionNoteRepository.getNote("d1") } returns
            Result.failure(RuntimeException("Not found"))

        val result = useCase("d1")

        assertTrue(result.isFailure)
    }
}
