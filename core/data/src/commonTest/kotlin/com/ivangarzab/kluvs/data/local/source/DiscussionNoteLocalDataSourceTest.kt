package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.DatabaseMockFixture
import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.entities.DiscussionNoteEntity
import com.ivangarzab.kluvs.model.DiscussionNote
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DiscussionNoteLocalDataSourceTest {

    private lateinit var fixture: DatabaseMockFixture
    private lateinit var dataSource: DiscussionNoteLocalDataSource

    private fun setup() {
        fixture = DatabaseMockFixture()
        dataSource = DiscussionNoteLocalDataSourceImpl(fixture.database)
    }

    private val noteEntity = DiscussionNoteEntity(
        discussionId = "discussion-1",
        id = "note-1",
        memberId = "member-1",
        content = "My note",
        visibility = "PRIVATE",
        createdAt = null,
        updatedAt = null,
        lastFetchedAt = 0
    )

    @Test
    fun `getNote returns cached note`() = runTest {
        setup()
        everySuspend { fixture.discussionNoteDao.getNote("discussion-1") } returns noteEntity

        assertEquals(noteEntity.toDomain(), dataSource.getNote("discussion-1"))
    }

    @Test
    fun `getNote returns null when not cached`() = runTest {
        setup()
        everySuspend { fixture.discussionNoteDao.getNote("discussion-1") } returns null

        assertNull(dataSource.getNote("discussion-1"))
    }

    @Test
    fun `insertNote inserts note entity`() = runTest {
        setup()
        val note = DiscussionNote(
            id = "note-1",
            discussionId = "discussion-1",
            memberId = "member-1",
            content = "My note"
        )

        everySuspend { fixture.discussionNoteDao.insertNote(note.toEntity()) } returns Unit

        dataSource.insertNote(note)
    }

    @Test
    fun `deleteNote deletes existing note by note id`() = runTest {
        setup()
        everySuspend { fixture.discussionNoteDao.getNoteById("note-1") } returns noteEntity
        everySuspend { fixture.discussionNoteDao.deleteNote(noteEntity) } returns Unit

        dataSource.deleteNote("note-1")
    }

    @Test
    fun `deleteAll clears all notes`() = runTest {
        setup()

        dataSource.deleteAll()
    }
}
