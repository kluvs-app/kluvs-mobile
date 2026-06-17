package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.data.remote.dtos.ClubDto
import com.ivangarzab.kluvs.data.remote.dtos.MemberDto
import com.ivangarzab.kluvs.data.remote.dtos.MemberResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MemberMappersTest {

    @Test
    fun `MemberDto toDomain maps basic fields only`() {
        // Given: A MemberDto with basic info
        val dto = MemberDto(
            id = "1",
            name = "Jane Doe",
            books_read = 10,
            user_id = "user-123",
            role = "member",
            clubs = emptyList()
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
    fun `MemberResponseDto toDomain maps all nested clubs`() {
        // Given: A MemberResponseDto with nested clubs
        val clubDto1 = ClubDto(
            id = "club-1",
            name = "Fiction Club",
            discord_channel = "123456789",
            server_id = "987654321"
        )

        val clubDto2 = ClubDto(
            id = "club-2",
            name = "Science Club",
            discord_channel = "111222333",
            server_id = "987654321"
        )

        val dto = MemberResponseDto(
            id = "2",
            name = "John Smith",
            books_read = 15,
            user_id = "user-456",
            clubs = listOf(clubDto1, clubDto2),
            shame_clubs = listOf(clubDto1)
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
    fun `MemberResponseDto toDomain handles empty club lists`() {
        // Given: A MemberResponseDto with no clubs
        val dto = MemberResponseDto(
            id = "3",
            name = "New Member",
            books_read = 0,
            user_id = null,
            clubs = emptyList(),
            shame_clubs = emptyList()
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
            id = "4",
            name = "Anonymous",
            books_read = 0,
            user_id = null,
            role = null,
            clubs = emptyList()
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
        // Given: A MemberDto with avatar_path
        val dto = MemberDto(
            id = "1",
            name = "Test Member",
            handle = "testuser",
            avatar_path = "member-1/avatar.png",
            books_read = 5,
            user_id = "user-123",
            role = "member",
            created_at = null,
            clubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: avatarPath is mapped correctly
        assertEquals("member-1/avatar.png", domain.avatarPath)
    }

    @Test
    fun `MemberDto with null avatarPath maps correctly`() {
        // Given: A MemberDto without avatar_path
        val dto = MemberDto(
            id = "1",
            name = "Test Member",
            handle = "testuser",
            avatar_path = null,
            books_read = 5,
            user_id = "user-123",
            role = "member",
            created_at = null,
            clubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: avatarPath is null
        assertNull(domain.avatarPath)
    }

    @Test
    fun `MemberResponseDto with avatarPath maps correctly`() {
        // Given: A MemberResponseDto with avatar_path
        val dto = MemberResponseDto(
            id = "2",
            name = "John Smith",
            handle = "johnsmith",
            avatar_path = "member-2/avatar.png",
            books_read = 15,
            user_id = "user-456",
            created_at = "2023-06-10T14:22:33Z",
            clubs = emptyList(),
            shame_clubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: avatarPath is mapped correctly
        assertEquals("member-2/avatar.png", domain.avatarPath)
    }

    @Test
    fun `MemberResponseDto with null avatarPath maps correctly`() {
        // Given: A MemberResponseDto without avatar_path
        val dto = MemberResponseDto(
            id = "2",
            name = "John Smith",
            handle = "johnsmith",
            avatar_path = null,
            books_read = 15,
            user_id = "user-456",
            created_at = "2023-06-10T14:22:33Z",
            clubs = emptyList(),
            shame_clubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: avatarPath is null
        assertNull(domain.avatarPath)
    }

    @Test
    fun `MemberDto with createdAt timestamp maps to LocalDateTime`() {
        // Given: A MemberDto with createdAt timestamp
        val dto = MemberDto(
            id = "1",
            name = "Test Member",
            handle = null,
            books_read = 0,
            user_id = null,
            role = null,
            created_at = "2024-01-15T10:30:00+00:00",
            clubs = emptyList()
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
            id = "1",
            name = "Test Member",
            handle = null,
            books_read = 0,
            user_id = null,
            role = null,
            created_at = null,
            clubs = emptyList()
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
            id = "1",
            name = "Test Member",
            handle = "testuser",
            books_read = 0,
            user_id = null,
            role = null,
            created_at = null,
            clubs = emptyList()
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
            id = "1",
            name = "Test Member",
            handle = null,
            books_read = 0,
            user_id = null,
            role = null,
            created_at = null,
            clubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: handle is null
        assertNull(domain.handle)
    }

    @Test
    fun `MemberResponseDto with createdAt and handle maps correctly`() {
        // Given: A MemberResponseDto with createdAt and handle
        val dto = MemberResponseDto(
            id = "2",
            name = "John Smith",
            handle = "johnsmith",
            books_read = 15,
            user_id = "user-456",
            created_at = "2023-06-10T14:22:33Z",
            clubs = emptyList(),
            shame_clubs = emptyList()
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
