package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.BookDto
import com.ivangarzab.kluvs.api.models.DiscussionDto
import com.ivangarzab.kluvs.api.models.SessionDto
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionMappersTest {

    @Test
    fun `SessionDto toDomain maps all fields correctly`() {
        // Given: A SessionDto with full data (used both for GET /session?id= and
        // for sessions embedded within Club/Member responses as active_session)
        val bookDto = BookDto(
            id = 1,
            title = "Test Book",
            author = "Test Author",
            edition = "1st",
            year = 2024,
            isbn = "123-456"
        )

        val discussionDto = DiscussionDto(
            id = "disc-1",
            sessionId = "session-1",
            title = "Chapter 1",
            scheduledAt = "2024-06-15T18:00:00",
            location = "Discord"
        )

        val dto = SessionDto(
            id = "session-1",
            clubId = "club-1",
            status = SessionDto.Status.active,
            book = bookDto,
            dueDate = "2024-12-31T23:59:59",
            discussions = listOf(discussionDto)
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: All fields are mapped correctly
        assertEquals("session-1", domain.id)
        assertEquals("club-1", domain.clubId)
        assertEquals("Test Book", domain.book.title)
        assertEquals(LocalDateTime(2024, 12, 31, 23, 59, 59), domain.dueDate)
        assertEquals(1, domain.discussions.size)
        assertEquals("Chapter 1", domain.discussions.first().title)
    }

    @Test
    fun `SessionDto toDomain handles null discussions and dueDate`() {
        // Given: A SessionDto without discussions/dueDate
        val dto = SessionDto(
            id = "session-2",
            clubId = "club-2",
            status = SessionDto.Status.active,
            book = BookDto(id = 2, title = "Book", author = "Author"),
            dueDate = null,
            discussions = null
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: dueDate is null and discussions is empty
        assertEquals("session-2", domain.id)
        assertNull(domain.dueDate)
        assertTrue(domain.discussions.isEmpty())
    }

    @Test
    fun `SessionDto toDomain handles empty discussions list`() {
        // Given: A SessionDto with an empty discussions list
        val dto = SessionDto(
            id = "session-3",
            clubId = "club-3",
            status = SessionDto.Status.active,
            book = BookDto(id = 3, title = "Solo Book", author = "Lonely Author"),
            dueDate = null,
            discussions = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: discussions is empty
        assertTrue(domain.discussions.isEmpty())
    }

    @Test
    fun `SessionDto toDomain maps multiple discussions`() {
        // Given: A SessionDto with multiple discussions
        val bookDto = BookDto(id = 4, title = "Multi", author = "Discuss")

        val discussions = listOf(
            DiscussionDto("disc-1", "session-6", "Part 1", "2024-01-15T10:00:00", "Online"),
            DiscussionDto("disc-2", "session-6", "Part 2", "2024-01-22T10:00:00", "Online"),
            DiscussionDto("disc-3", "session-6", "Finale", "2024-01-29T10:00:00", "In-person")
        )

        val dto = SessionDto(
            id = "session-6",
            clubId = "club-4",
            status = SessionDto.Status.active,
            book = bookDto,
            dueDate = "2024-02-01T00:00:00",
            discussions = discussions
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: All discussions are mapped
        assertEquals(3, domain.discussions.size)
        assertEquals("Part 1", domain.discussions[0].title)
        assertEquals("Part 2", domain.discussions[1].title)
        assertEquals("Finale", domain.discussions[2].title)
    }

    @Test
    fun `SessionDto toDomain maps finished status session`() {
        // Given: A finished SessionDto
        val dto = SessionDto(
            id = "session-7",
            clubId = "club-5",
            status = SessionDto.Status.finished,
            book = BookDto(id = 5, title = "Finished Book", author = "Author"),
            dueDate = "2024-01-01T00:00:00"
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Maps correctly regardless of status (not modeled in domain Session)
        assertEquals("session-7", domain.id)
        assertEquals("Finished Book", domain.book.title)
    }
}
