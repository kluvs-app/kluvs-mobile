package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.data.remote.dtos.BookDto
import com.ivangarzab.kluvs.data.remote.dtos.ClubDto
import com.ivangarzab.kluvs.data.remote.dtos.ClubMemberDto
import com.ivangarzab.kluvs.data.remote.dtos.ClubResponseDto
import com.ivangarzab.kluvs.data.remote.dtos.ServerClubDto
import com.ivangarzab.kluvs.data.remote.dtos.SessionDto
import com.ivangarzab.kluvs.model.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClubMappersTest {

    @Test
    fun `ClubDto toDomain maps basic fields only`() {
        // Given: A ClubDto with basic info
        val dto = ClubDto(
            id = "club-1",
            name = "Book Club",
            discord_channel = "123456789",
            server_id = "987654321"
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Basic fields are mapped, relations are null
        assertEquals("club-1", domain.id)
        assertEquals("Book Club", domain.name)
        assertEquals("123456789", domain.discordChannel)
        assertEquals("987654321", domain.serverId)
        assertNull(domain.role) // No role when club is fetched standalone
        assertTrue(domain.shameList.isEmpty())
        assertNull(domain.members)
        assertNull(domain.activeSession)
        assertNull(domain.pastSessions)
    }

    @Test
    fun `ClubResponseDto toDomain maps all nested relations`() {
        // Given: A ClubResponseDto with nested data
        val clubMemberDto = ClubMemberDto(
            id = "1",
            name = "John Doe",
            books_read = 5,
            user_id = "user-1",
            role = "admin",
            clubs = emptyList()
        )

        val bookDto = BookDto(
            id = "book-1",
            title = "Test Book",
            author = "Test Author"
        )

        val sessionDto = SessionDto(
            id = "session-1",
            club_id = "club-1",
            book = bookDto,
            due_date = "2024-12-31T00:00:00",
            discussions = emptyList()
        )

        val dto = ClubResponseDto(
            id = "club-1",
            name = "Full Club",
            discord_channel = "123456789",
            server_id = "987654321",
            members = listOf(clubMemberDto),
            active_session = sessionDto,
            past_sessions = listOf(sessionDto),
            shame_list = listOf("1", "2")
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: All nested relations are mapped
        assertEquals("club-1", domain.id)
        assertEquals("Full Club", domain.name)
        assertNotNull(domain.members)
        assertEquals(1, domain.members?.size)
        assertEquals(Role.ADMIN, domain.members?.first()?.role)
        assertEquals("John Doe", domain.members?.first()?.member?.name)
        assertNotNull(domain.activeSession)
        assertEquals("session-1", domain.activeSession?.id)
        assertNotNull(domain.pastSessions)
        assertEquals(1, domain.pastSessions?.size)
        assertEquals(2, domain.shameList.size)
        assertTrue(domain.shameList.contains("1"))
        assertTrue(domain.shameList.contains("2"))
    }

    @Test
    fun `ServerClubDto toDomain maps with latest session`() {
        // Given: A ServerClubDto
        val sessionDto = SessionDto(
            id = "session-1",
            club_id = "club-1",
            book = BookDto(id = "book-1", title = "Book", author = "Author"),
            due_date = null,
            discussions = emptyList()
        )

        val dto = ServerClubDto(
            id = "club-1",
            name = "Server Club",
            discord_channel = "123456789",
            member_count = 10,
            latest_session = sessionDto
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Basic fields and latest session are mapped
        assertEquals("club-1", domain.id)
        assertEquals("Server Club", domain.name)
        assertEquals("123456789", domain.discordChannel)
        assertNull(domain.serverId) // Not available in ServerClubDto
        assertNotNull(domain.activeSession)
        assertEquals("session-1", domain.activeSession?.id)
        assertNull(domain.members)
        assertNull(domain.pastSessions)
        assertTrue(domain.shameList.isEmpty())
    }

    @Test
    fun `ServerClubDto toDomain handles null latest session`() {
        // Given: A ServerClubDto without latest session
        val dto = ServerClubDto(
            id = "club-2",
            name = "Empty Club",
            discord_channel = null,
            member_count = 0,
            latest_session = null
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: No active session
        assertEquals("club-2", domain.id)
        assertNull(domain.activeSession)
    }

    @Test
    fun `ClubDto with foundedDate maps to LocalDate`() {
        // Given: A ClubDto with foundedDate
        val dto = ClubDto(
            id = "club-1",
            name = "Test Club",
            discord_channel = null,
            server_id = null,
            founded_date = "2024-01-15"
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: foundedDate is parsed correctly
        assertNotNull(domain.foundedDate)
        assertEquals(2024, domain.foundedDate?.year)
        assertEquals(1, domain.foundedDate?.monthNumber)
        assertEquals(15, domain.foundedDate?.dayOfMonth)
    }

    @Test
    fun `ClubDto with null foundedDate maps correctly`() {
        // Given: A ClubDto without foundedDate
        val dto = ClubDto(
            id = "club-1",
            name = "Test Club",
            discord_channel = null,
            server_id = null,
            founded_date = null
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: foundedDate is null
        assertNull(domain.foundedDate)
    }

    @Test
    fun `ClubResponseDto with foundedDate maps to LocalDate`() {
        // Given: A ClubResponseDto with foundedDate
        val dto = ClubResponseDto(
            id = "club-1",
            name = "Test Club",
            discord_channel = null,
            server_id = null,
            founded_date = "2024-06-01",
            members = emptyList(),
            active_session = null,
            past_sessions = emptyList(),
            shame_list = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: foundedDate is parsed correctly
        assertNotNull(domain.foundedDate)
        assertEquals(2024, domain.foundedDate?.year)
        assertEquals(6, domain.foundedDate?.monthNumber)
        assertEquals(1, domain.foundedDate?.dayOfMonth)
    }

    @Test
    fun `ServerClubDto with foundedDate maps to LocalDate`() {
        // Given: A ServerClubDto with foundedDate
        val dto = ServerClubDto(
            id = "club-1",
            name = "Server Club",
            discord_channel = null,
            founded_date = "2023-12-25",
            member_count = 5,
            latest_session = null
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: foundedDate is parsed correctly
        assertNotNull(domain.foundedDate)
        assertEquals(2023, domain.foundedDate?.year)
        assertEquals(12, domain.foundedDate?.monthNumber)
        assertEquals(25, domain.foundedDate?.dayOfMonth)
    }
}
