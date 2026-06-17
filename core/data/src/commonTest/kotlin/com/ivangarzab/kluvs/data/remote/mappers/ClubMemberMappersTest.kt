package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.data.remote.dtos.ClubMemberDto
import com.ivangarzab.kluvs.model.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ClubMemberMappersTest {

    @Test
    fun `ClubMemberDto toDomain maps all fields including role`() {
        // Given: A ClubMemberDto with all fields
        val dto = ClubMemberDto(
            id = "1",
            name = "Jane Doe",
            handle = "janedoe",
            avatar_path = "member-1/avatar.png",
            books_read = 10,
            user_id = "user-123",
            role = "owner",
            created_at = "2024-01-15T10:30:00+00:00",
            clubs = listOf("club-1", "club-2")
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: ClubMember is created with role and member
        assertEquals(Role.OWNER, domain.role)
        assertEquals("1", domain.member.id)
        assertEquals("Jane Doe", domain.member.name)
        assertEquals("janedoe", domain.member.handle)
        assertEquals("member-1/avatar.png", domain.member.avatarPath)
        assertEquals(10, domain.member.booksRead)
        assertEquals("user-123", domain.member.userId)
        assertNotNull(domain.member.createdAt)
        assertNull(domain.member.clubs) // Relations not loaded in club context
        assertNull(domain.member.shameClubs)
    }

    @Test
    fun `ClubMemberDto with different roles maps correctly`() {
        // Given: ClubMemberDtos with different roles
        val ownerDto = ClubMemberDto(
            id = "1",
            name = "Owner",
            books_read = 10,
            user_id = "user-1",
            role = "owner",
            clubs = emptyList()
        )

        val adminDto = ClubMemberDto(
            id = "2",
            name = "Admin",
            books_read = 5,
            user_id = "user-2",
            role = "admin",
            clubs = emptyList()
        )

        val memberDto = ClubMemberDto(
            id = "3",
            name = "Member",
            books_read = 3,
            user_id = "user-3",
            role = "member",
            clubs = emptyList()
        )

        // When: Mapping to domain
        val owner = ownerDto.toDomain()
        val admin = adminDto.toDomain()
        val member = memberDto.toDomain()

        // Then: Each has correct role
        assertEquals(Role.OWNER, owner.role)
        assertEquals(Role.ADMIN, admin.role)
        assertEquals(Role.MEMBER, member.role)
    }

    @Test
    fun `ClubMemberDto with null optional fields maps correctly`() {
        // Given: A ClubMemberDto with minimal required fields
        val dto = ClubMemberDto(
            id = "4",
            name = null,
            handle = null,
            avatar_path = null,
            books_read = 0,
            user_id = null,
            role = "member",
            created_at = null,
            clubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Nullable fields are handled correctly
        assertEquals(Role.MEMBER, domain.role)
        assertEquals("4", domain.member.id)
        assertEquals("", domain.member.name) // Default empty string for null name
        assertNull(domain.member.handle)
        assertNull(domain.member.avatarPath)
        assertNull(domain.member.userId)
        assertNull(domain.member.createdAt)
    }
}
