package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.DiscussionNoteCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteUpdateRequestDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.source.DiscussionNoteLocalDataSource
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DiscussionNoteRepositoryTest {

    private lateinit var remoteDataSource: DiscussionNoteRemoteDataSource
    private lateinit var localDataSource: DiscussionNoteLocalDataSource
    private lateinit var cachePolicy: CachePolicy
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
        localDataSource = mock<DiscussionNoteLocalDataSource>()
        cachePolicy = CachePolicy()
        repository = DiscussionNoteRepositoryImpl(remoteDataSource, localDataSource, cachePolicy)

        // Default behavior: cache miss (return null)
        everySuspend { localDataSource.getNote(any()) } returns null
        everySuspend { localDataSource.getLastFetchedAt(any()) } returns null
        everySuspend { localDataSource.insertNote(any()) } returns Unit
        everySuspend { localDataSource.deleteNote(any()) } returns Unit
    }

    @Test
    fun `getNote cache miss delegates to remote and caches result`() = runTest {
        everySuspend { remoteDataSource.getNote("discussion-1") } returns Result.success(testNote)

        val result = repository.getNote("discussion-1")

        assertTrue(result.isSuccess)
        assertEquals(testNote, result.getOrNull())
        verifySuspend { remoteDataSource.getNote("discussion-1") }
        verifySuspend { localDataSource.insertNote(testNote) }
    }

    @Test
    fun `getNote cache hit returns cached data without hitting remote`() = runTest {
        // remoteDataSource.getNote() intentionally left unstubbed: a strict mock
        // throws if it's invoked, so a passing test proves the cache path was used.
        everySuspend { localDataSource.getNote("discussion-1") } returns testNote
        everySuspend { localDataSource.getLastFetchedAt("discussion-1") } returns Long.MAX_VALUE

        val result = repository.getNote("discussion-1")

        assertTrue(result.isSuccess)
        assertEquals(testNote, result.getOrNull())
    }

    @Test
    fun `createNote success delegates to remote with mapped request and caches result`() = runTest {
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
        verifySuspend { localDataSource.insertNote(testNote) }
    }

    @Test
    fun `updateNote success delegates to remote with mapped request and caches result`() = runTest {
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
        verifySuspend { localDataSource.insertNote(testNote) }
    }

    @Test
    fun `deleteNote success delegates to remote and evicts cache`() = runTest {
        everySuspend { remoteDataSource.deleteNote("note-1") } returns Result.success(Unit)

        val result = repository.deleteNote("note-1")

        assertTrue(result.isSuccess)
        verifySuspend { remoteDataSource.deleteNote("note-1") }
        verifySuspend { localDataSource.deleteNote("note-1") }
    }
}
