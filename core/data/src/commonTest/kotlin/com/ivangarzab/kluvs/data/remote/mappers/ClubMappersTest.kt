package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.BookDto
import com.ivangarzab.kluvs.api.models.ClubDto
import com.ivangarzab.kluvs.api.models.ClubMemberDto
import com.ivangarzab.kluvs.api.models.MemberClubEntryDto
import com.ivangarzab.kluvs.api.models.ServerClubSummaryDto
import com.ivangarzab.kluvs.api.models.SessionDto
import com.ivangarzab.kluvs.model.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClubMappersTest {

    @Test
    fun `ClubDto toDomain maps basic fields only`() {
        // Given: A ClubDto with basic info, no relations
        val dto = ClubDto(
            id = "club-1",
            name = "Book Club",
            discordChannel = "123456789",
            serverId = "987654321"
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
    fun `ClubDto toDomain maps nested relations`() {
        // Given: A ClubDto with nested data
        val clubMemberDto = ClubMemberDto(
            id = 1,
            name = "John Doe",
            booksRead = 5,
            role = ClubMemberDto.Role.admin
        )

        val bookDto = BookDto(
            id = 1,
            title = "Test Book",
            author = "Test Author"
        )

        val sessionDto = SessionDto(
            id = "session-1",
            clubId = "club-1",
            status = SessionDto.Status.active,
            book = bookDto,
            dueDate = "2024-12-31T00:00:00",
            discussions = emptyList()
        )

        val dto = ClubDto(
            id = "club-1",
            name = "Full Club",
            discordChannel = "123456789",
            serverId = "987654321",
            members = listOf(clubMemberDto),
            activeSession = sessionDto,
            shameList = listOf(1, 2)
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Nested relations are mapped (role is always null at this level)
        assertEquals("club-1", domain.id)
        assertEquals("Full Club", domain.name)
        assertNull(domain.role)
        assertNotNull(domain.members)
        assertEquals(1, domain.members?.size)
        assertEquals(Role.ADMIN, domain.members?.first()?.role)
        assertEquals("John Doe", domain.members?.first()?.member?.name)
        assertNotNull(domain.activeSession)
        assertEquals("session-1", domain.activeSession?.id)
        assertEquals(2, domain.shameList.size)
        assertTrue(domain.shameList.contains("1"))
        assertTrue(domain.shameList.contains("2"))
    }

    @Test
    fun `MemberClubEntryDto toDomain populates role`() {
        // Given: A club embedded within a member's clubs list, with a role
        val dto = MemberClubEntryDto(
            id = "club-1",
            name = "Full Club",
            discordChannel = "123456789",
            serverId = "987654321",
            role = MemberClubEntryDto.Role.owner
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Role is populated from the wrapper
        assertEquals("club-1", domain.id)
        assertEquals(Role.OWNER, domain.role)
    }

    @Test
    fun `ServerClubSummaryDto toDomain maps basic fields`() {
        // Given: A club embedded in a single-server response, with member_count/latest_session
        val dto = ServerClubSummaryDto(
            id = "club-1",
            name = "Server Club",
            discordChannel = "123456789",
            memberCount = 10,
            latestSession = com.ivangarzab.kluvs.api.models.ServerClubLatestSessionDto(
                id = "session-1",
                dueDate = "2024-12-31"
            )
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Basic fields are mapped; activeSession is null (latest_session has no book id)
        assertEquals("club-1", domain.id)
        assertEquals("Server Club", domain.name)
        assertEquals("123456789", domain.discordChannel)
        assertNull(domain.activeSession)
        assertNull(domain.members)
        assertNull(domain.pastSessions)
        assertTrue(domain.shameList.isEmpty())
    }

    @Test
    fun `ServerClubSummaryDto toDomain handles null latest session`() {
        // Given: A club without a latest session
        val dto = ServerClubSummaryDto(
            id = "club-2",
            name = "Empty Club",
            memberCount = 0,
            latestSession = null
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
            discordChannel = null,
            serverId = null,
            foundedDate = "2024-01-15"
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
            discordChannel = null,
            serverId = null,
            foundedDate = null
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: foundedDate is null
        assertNull(domain.foundedDate)
    }

    @Test
    fun `ServerClubSummaryDto with foundedDate maps to LocalDate`() {
        // Given: A club embedded in a single-server response, with foundedDate
        val dto = ServerClubSummaryDto(
            id = "club-1",
            name = "Server Club",
            foundedDate = "2023-12-25",
            memberCount = 5,
            latestSession = null
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
