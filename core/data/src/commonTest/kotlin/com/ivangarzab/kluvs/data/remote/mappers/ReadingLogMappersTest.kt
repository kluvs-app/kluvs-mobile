package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.SessionReadingLogBookDto
import com.ivangarzab.kluvs.api.models.SessionReadingLogClubDto
import com.ivangarzab.kluvs.api.models.SessionReadingLogEntryDto
import com.ivangarzab.kluvs.api.models.SessionReadingLogGroupsDto
import com.ivangarzab.kluvs.api.models.SessionReadingLogResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReadingLogMappersTest {

    private val entryDto = SessionReadingLogEntryDto(
        startedAt = null,
        endedAt = null,
        id = "session-1",
        dueDate = "2026-08-01",
        book = SessionReadingLogBookDto(
            id = 5,
            title = "The Hobbit",
            author = "J.R.R. Tolkien",
            imageUrl = "https://example.com/hobbit.jpg"
        ),
        club = SessionReadingLogClubDto(id = "club-1", name = "Freaks & Geeks")
    )

    @Test
    fun `response toDomain maps active and finished groups`() {
        // Given: A reading log with one active and one finished session
        val dto = SessionReadingLogResponseDto(
            success = true,
            readingLog = SessionReadingLogGroupsDto(
                active = listOf(entryDto),
                finished = listOf(entryDto.copy(id = "session-2"))
            )
        )

        // When: Mapping to domain
        val log = dto.toDomain()

        // Then: Both groups map with full nested data
        assertEquals(1, log.active.size)
        assertEquals(1, log.finished.size)
        val entry = log.active.first()
        assertEquals("session-1", entry.sessionId)
        assertNotNull(entry.dueDate)
        assertEquals("The Hobbit", entry.book?.title)
        assertEquals("J.R.R. Tolkien", entry.book?.author)
        assertEquals("Freaks & Geeks", entry.club?.name)
    }

    @Test
    fun `response toDomain handles empty groups`() {
        val log = SessionReadingLogResponseDto(
            success = true,
            readingLog = SessionReadingLogGroupsDto(active = emptyList(), finished = emptyList())
        ).toDomain()

        assertEquals(emptyList(), log.active)
        assertEquals(emptyList(), log.finished)
    }

    @Test
    fun `entry toDomain maps a null due date`() {
        val entry = entryDto.copy(dueDate = null).toDomain()

        assertNull(entry.dueDate)
        assertEquals("The Hobbit", entry.book?.title)
    }
}
