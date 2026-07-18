package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ClubDto
import com.ivangarzab.kluvs.api.models.ServerDto
import com.ivangarzab.kluvs.api.models.ServerClubLatestSessionDto
import com.ivangarzab.kluvs.api.models.ServerClubSummaryDto
import com.ivangarzab.kluvs.api.models.ServerGetSingleResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ServerMappersTest {

    @Test
    fun `ServerDto toDomain maps basic fields only`() {
        // Given: A ServerDto with no clubs
        val dto = ServerDto(
            id = "server-1",
            name = "Production Server"
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Basic fields are mapped, clubs are null
        assertEquals("server-1", domain.id)
        assertEquals("Production Server", domain.name)
        assertNull(domain.clubs)
    }

    @Test
    fun `ServerDto toDomain maps plain nested clubs from list endpoint shape`() {
        // Given: A ServerDto (list/create/update shape) with plain ClubDto clubs
        val dto = ServerDto(
            id = "server-2",
            name = "Test Server",
            clubs = listOf(
                ClubDto(id = "club-1", name = "Club One", discordChannel = "123456789"),
                ClubDto(id = "club-2", name = "Club Two", discordChannel = "987654321")
            )
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: All nested clubs are mapped, with no active session (not in this shape)
        assertEquals("server-2", domain.id)
        assertNotNull(domain.clubs)
        assertEquals(2, domain.clubs?.size)
        assertEquals("club-1", domain.clubs?.first()?.id)
        assertNull(domain.clubs?.first()?.activeSession)
    }

    @Test
    fun `ServerGetSingleResponseDto toDomain maps nested clubs with member_count`() {
        // Given: A single-server response with nested clubs (member_count/latest_session shape)
        val fullClub = ServerClubSummaryDto(
            id = "club-full",
            name = "Full Club",
            discordChannel = "111222333",
            memberCount = 20,
            latestSession = ServerClubLatestSessionDto(
                id = "session-full",
                dueDate = "2024-12-31"
            )
        )

        val minimalClub = ServerClubSummaryDto(
            id = "club-minimal",
            name = "Minimal Club",
            memberCount = null,
            latestSession = null
        )

        val dto = ServerGetSingleResponseDto(
            id = "server-4",
            name = "Mixed Server",
            clubs = listOf(fullClub, minimalClub)
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Both clubs are mapped; activeSession is always null (latest_session
        // has no usable book id to construct a domain Session)
        assertEquals(2, domain.clubs?.size)

        val full = domain.clubs?.first { it.id == "club-full" }
        assertEquals("Full Club", full?.name)
        assertNull(full?.activeSession)

        val minimal = domain.clubs?.first { it.id == "club-minimal" }
        assertEquals("Minimal Club", minimal?.name)
        assertNull(minimal?.activeSession)
        assertNull(minimal?.discordChannel)
    }

    @Test
    fun `ServerGetSingleResponseDto toDomain handles empty clubs list`() {
        // Given: A single-server response with no clubs
        val dto = ServerGetSingleResponseDto(
            id = "server-3",
            name = "Empty Server",
            clubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: clubs is an empty list (not null)
        assertEquals("server-3", domain.id)
        assertEquals("Empty Server", domain.name)
        assertNotNull(domain.clubs)
        assertTrue(domain.clubs?.isEmpty() == true)
    }
}
