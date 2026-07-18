package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.DiscussionNoteCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.source.DiscussionNoteRemoteDataSource
import com.ivangarzab.kluvs.model.DiscussionNote
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiscussionNoteRepositoryTest {

    private lateinit var remoteDataSource: DiscussionNoteRemoteDataSource
    private lateinit var repository: DiscussionNoteRepository

    private val testNote = DiscussionNote(
        id = "note-1",
        discussionId = "discussion-1",
        memberId = "member-1",
        content = "My note content"
    )

    @BeforeTest
    fun setup() {
        remoteDataSource = mock<DiscussionNoteRemoteDataSource>()
        repository = DiscussionNoteRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getNote success delegates to remote`() = runTest {
        everySuspend { remoteDataSource.getNote("discussion-1") } returns Result.success(testNote)

        val result = repository.getNote("discussion-1")

        assertTrue(result.isSuccess)
        assertEquals(testNote, result.getOrNull())
        verifySuspend { remoteDataSource.getNote("discussion-1") }
    }

    @Test
    fun `createNote success delegates to remote with mapped request`() = runTest {
        everySuspend { remoteDataSource.createNote(any()) } returns Result.success(testNote)

        val result = repository.createNote("discussion-1", "My note content")

        assertTrue(result.isSuccess)
        assertEquals(testNote, result.getOrNull())
        verifySuspend {
            remoteDataSource.createNote(
                DiscussionNoteCreateRequestDto(
                    discussionId = "discussion-1",
                    content = "My note content"
                )
            )
        }
    }

    @Test
    fun `updateNote success delegates to remote with mapped request`() = runTest {
        everySuspend { remoteDataSource.updateNote(any()) } returns Result.success(testNote)

        val result = repository.updateNote("note-1", "Updated content")

        assertTrue(result.isSuccess)
        assertEquals(testNote, result.getOrNull())
        verifySuspend {
            remoteDataSource.updateNote(
                DiscussionNoteUpdateRequestDto(
                    id = "note-1",
                    content = "Updated content"
                )
            )
        }
    }

    @Test
    fun `deleteNote success delegates to remote`() = runTest {
        everySuspend { remoteDataSource.deleteNote("note-1") } returns Result.success(Unit)

        val result = repository.deleteNote("note-1")

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.deleteNote("note-1") }
    }
}
