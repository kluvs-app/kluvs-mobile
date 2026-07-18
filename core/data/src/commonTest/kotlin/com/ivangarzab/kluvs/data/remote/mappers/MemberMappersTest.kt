package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.MemberDto
import com.ivangarzab.kluvs.api.models.MemberClubEntryDto
import com.ivangarzab.kluvs.api.models.MemberGetResponseDto
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MemberMappersTest {

    @Test
    fun `MemberDto toDomain maps basic fields only`() {
        // Given: A MemberDto with basic info
        val dto = MemberDto(
            id = 1,
            name = "Jane Doe",
            platformMetadata = JsonObject(emptyMap()),
            booksRead = 10,
            userId = "user-123"
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Basic fields are mapped, relations are null
        assertEquals("1", domain.id)
        assertEquals("Jane Doe", domain.name)
        assertEquals(10, domain.booksRead)
        assertEquals("user-123", domain.userId)
        assertNull(domain.clubs) // Not available in MemberDto
        assertNull(domain.shameClubs) // Not available in MemberDto
    }

    @Test
    fun `MemberGetResponseDto toDomain maps all nested clubs`() {
        // Given: A MemberGetResponseDto with nested clubs
        val clubInner1 = MemberClubEntryDto(
            id = "club-1",
            name = "Fiction Club",
            discordChannel = "123456789",
            serverId = "987654321"
        )

        val clubInner2 = MemberClubEntryDto(
            id = "club-2",
            name = "Science Club",
            discordChannel = "111222333",
            serverId = "987654321"
        )

        val shameClub = com.ivangarzab.kluvs.api.models.ClubDto(
            id = "club-1",
            name = "Fiction Club",
            discordChannel = "123456789",
            serverId = "987654321"
        )

        val dto = MemberGetResponseDto(
            id = 2,
            name = "John Smith",
            platformMetadata = JsonObject(emptyMap()),
            booksRead = 15,
            userId = "user-456",
            clubs = listOf(clubInner1, clubInner2),
            shameClubs = listOf(shameClub)
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: All nested relations are mapped
        assertEquals("2", domain.id)
        assertEquals("John Smith", domain.name)
        assertEquals(15, domain.booksRead)
        assertEquals("user-456", domain.userId)

        assertNotNull(domain.clubs)
        assertEquals(2, domain.clubs?.size)
        assertEquals("Fiction Club", domain.clubs?.first()?.name)

        assertNotNull(domain.shameClubs)
        assertEquals(1, domain.shameClubs?.size)
        assertEquals("club-1", domain.shameClubs?.first()?.id)
    }

    @Test
    fun `MemberGetResponseDto toDomain handles empty club lists`() {
        // Given: A MemberGetResponseDto with no clubs
        val dto = MemberGetResponseDto(
            id = 3,
            name = "New Member",
            platformMetadata = JsonObject(emptyMap()),
            booksRead = 0,
            clubs = emptyList(),
            shameClubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Empty lists are mapped correctly
        assertEquals("3", domain.id)
        assertNotNull(domain.clubs)
        assertEquals(0, domain.clubs?.size)
        assertNotNull(domain.shameClubs)
        assertEquals(0, domain.shameClubs?.size)
    }

    @Test
    fun `MemberDto toDomain handles nullable fields`() {
        // Given: A MemberDto with null optional fields
        val dto = MemberDto(
            id = 4,
            name = "Anonymous",
            platformMetadata = JsonObject(emptyMap()),
            booksRead = 0,
            userId = null
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Nullable fields are null
        assertEquals("4", domain.id)
        assertEquals("Anonymous", domain.name)
        assertNull(domain.userId)
    }

    @Test
    fun `MemberDto with avatarPath maps correctly`() {
        // Given: A MemberDto with avatarPath
        val dto = MemberDto(
            id = 1,
            name = "Test Member",
            platformMetadata = JsonObject(emptyMap()),
            handle = "testuser",
            avatarPath = "member-1/avatar.png",
            booksRead = 5,
            userId = "user-123"
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: avatarPath is mapped correctly
        assertEquals("member-1/avatar.png", domain.avatarPath)
    }

    @Test
    fun `MemberDto with null avatarPath maps correctly`() {
        // Given: A MemberDto without avatarPath
        val dto = MemberDto(
            id = 1,
            name = "Test Member",
            platformMetadata = JsonObject(emptyMap()),
            handle = "testuser",
            avatarPath = null,
            booksRead = 5,
            userId = "user-123"
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: avatarPath is null
        assertNull(domain.avatarPath)
    }

    @Test
    fun `MemberGetResponseDto with avatarPath maps correctly`() {
        // Given: A MemberGetResponseDto with avatarPath
        val dto = MemberGetResponseDto(
            id = 2,
            name = "John Smith",
            platformMetadata = JsonObject(emptyMap()),
            handle = "johnsmith",
            avatarPath = "member-2/avatar.png",
            booksRead = 15,
            userId = "user-456",
            createdAt = "2023-06-10T14:22:33Z",
            clubs = emptyList(),
            shameClubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: avatarPath is mapped correctly
        assertEquals("member-2/avatar.png", domain.avatarPath)
    }

    @Test
    fun `MemberDto with createdAt timestamp maps to LocalDateTime`() {
        // Given: A MemberDto with createdAt timestamp
        val dto = MemberDto(
            id = 1,
            name = "Test Member",
            platformMetadata = JsonObject(emptyMap()),
            booksRead = 0,
            createdAt = "2024-01-15T10:30:00+00:00"
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: createdAt is parsed correctly
        assertNotNull(domain.createdAt)
        assertEquals(2024, domain.createdAt?.year)
        assertEquals(1, domain.createdAt?.monthNumber)
        assertEquals(15, domain.createdAt?.dayOfMonth)
        assertEquals(10, domain.createdAt?.hour)
        assertEquals(30, domain.createdAt?.minute)
    }

    @Test
    fun `MemberDto with null createdAt maps correctly`() {
        // Given: A MemberDto without createdAt
        val dto = MemberDto(
            id = 1,
            name = "Test Member",
            platformMetadata = JsonObject(emptyMap()),
            booksRead = 0,
            createdAt = null
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: createdAt is null
        assertNull(domain.createdAt)
    }

    @Test
    fun `MemberDto with handle maps correctly`() {
        // Given: A MemberDto with handle
        val dto = MemberDto(
            id = 1,
            name = "Test Member",
            platformMetadata = JsonObject(emptyMap()),
            handle = "testuser",
            booksRead = 0
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: handle is mapped correctly
        assertEquals("testuser", domain.handle)
    }

    @Test
    fun `MemberDto with null handle maps correctly`() {
        // Given: A MemberDto without handle
        val dto = MemberDto(
            id = 1,
            name = "Test Member",
            platformMetadata = JsonObject(emptyMap()),
            handle = null,
            booksRead = 0
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: handle is null
        assertNull(domain.handle)
    }

    @Test
    fun `MemberGetResponseDto with createdAt and handle maps correctly`() {
        // Given: A MemberGetResponseDto with createdAt and handle
        val dto = MemberGetResponseDto(
            id = 2,
            name = "John Smith",
            platformMetadata = JsonObject(emptyMap()),
            handle = "johnsmith",
            booksRead = 15,
            userId = "user-456",
            createdAt = "2023-06-10T14:22:33Z",
            clubs = emptyList(),
            shameClubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: createdAt and handle are mapped correctly
        assertEquals("johnsmith", domain.handle)
        assertNotNull(domain.createdAt)
        assertEquals(2023, domain.createdAt?.year)
        assertEquals(6, domain.createdAt?.monthNumber)
        assertEquals(10, domain.createdAt?.dayOfMonth)
        assertEquals(14, domain.createdAt?.hour)
        assertEquals(22, domain.createdAt?.minute)
        assertEquals(33, domain.createdAt?.second)
    }
}
