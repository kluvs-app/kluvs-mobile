package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.DiscussionNoteCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.api.DiscussionNoteService
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiscussionNoteRemoteDataSourceTest {

    private lateinit var discussionNoteService: DiscussionNoteService
    private lateinit var dataSource: DiscussionNoteRemoteDataSource

    private val noteDto = DiscussionNoteDto(
        id = "note-1",
        discussionId = "disc-1",
        memberId = 1,
        content = "Great chapter",
        visibility = DiscussionNoteDto.Visibility.`private`,
        createdAt = "2026-07-01T10:30:00",
        updatedAt = "2026-07-01T10:30:00"
    )

    @BeforeTest
    fun setup() {
        discussionNoteService = mock<DiscussionNoteService>()
        dataSource = DiscussionNoteRemoteDataSourceImpl(discussionNoteService)
    }

    @Test
    fun `getNote maps existing note to domain`() = runTest {
        everySuspend { discussionNoteService.get("disc-1") } returns noteDto

        val result = dataSource.getNote("disc-1")

        assertTrue(result.isSuccess)
        assertEquals("note-1", result.getOrNull()?.id)
    }

    @Test
    fun `getNote returns null when no note exists`() = runTest {
        everySuspend { discussionNoteService.get("disc-1") } returns null

        val result = dataSource.getNote("disc-1")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `createNote maps created note to domain`() = runTest {
        val request = DiscussionNoteCreateRequestDto(
            discussionId = "disc-1",
            content = "Great chapter"
        )
        everySuspend { discussionNoteService.create(request) } returns noteDto

        val result = dataSource.createNote(request)

        assertEquals("Great chapter", result.getOrNull()?.content)
    }

    @Test
    fun `updateNote returns failure when service throws`() = runTest {
        val request = DiscussionNoteUpdateRequestDto(id = "note-1", content = "Edited")
        everySuspend { discussionNoteService.update(request) } throws Exception("Network error")

        val result = dataSource.updateNote(request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteNote delegates to service`() = runTest {
        everySuspend { discussionNoteService.delete("note-1") } returns Unit

        val result = dataSource.deleteNote("note-1")

        assertTrue(result.isSuccess)
        verifySuspend { discussionNoteService.delete("note-1") }
    }
}
