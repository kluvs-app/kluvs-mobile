package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ClubMemberDto
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
            joinedAt = "2026-01-01T00:00:00Z",
            id = 1,
            name = "Jane Doe",
            handle = "janedoe",
            avatarPath = "member-1/avatar.png",
            booksRead = 10,
            role = ClubMemberDto.Role.owner,
            createdAt = "2024-01-15T10:30:00+00:00",
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
        assertNull(domain.member.userId) // Not present on this embedded wrapper
        assertNotNull(domain.member.createdAt)
        assertNull(domain.member.clubs) // Relations not loaded in club context
        assertNull(domain.member.shameClubs)
    }

    @Test
    fun `ClubMemberDto with different roles maps correctly`() {
        // Given: ClubMemberDtos with different roles
        val ownerDto = ClubMemberDto(
            joinedAt = "2026-01-01T00:00:00Z",
            handle = "member-handle",
            avatarPath = null,
            createdAt = "2026-01-01T00:00:00Z",
            id = 1,
            name = "Owner",
            booksRead = 10,
            role = ClubMemberDto.Role.owner,
            clubs = emptyList()
        )

        val adminDto = ClubMemberDto(
            joinedAt = "2026-01-01T00:00:00Z",
            handle = "member-handle",
            avatarPath = null,
            createdAt = "2026-01-01T00:00:00Z",
            id = 2,
            name = "Admin",
            booksRead = 5,
            role = ClubMemberDto.Role.admin,
            clubs = emptyList()
        )

        val memberDto = ClubMemberDto(
            joinedAt = "2026-01-01T00:00:00Z",
            handle = "member-handle",
            avatarPath = null,
            createdAt = "2026-01-01T00:00:00Z",
            id = 3,
            name = "Member",
            booksRead = 3,
            role = ClubMemberDto.Role.member,
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
        // Given: A ClubMemberDto whose nullable fields are null
        val dto = ClubMemberDto(
            joinedAt = "2026-01-01T00:00:00Z",
            id = 4,
            name = "Quiet Member",
            handle = "quiet-member",
            avatarPath = null,
            booksRead = null,
            role = ClubMemberDto.Role.member,
            createdAt = "2026-01-01T00:00:00Z",
            clubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Null avatar stays null and a null books_read defaults to 0
        assertEquals(Role.MEMBER, domain.role)
        assertEquals("4", domain.member.id)
        assertNull(domain.member.avatarPath)
        assertEquals(0, domain.member.booksRead)
        assertNull(domain.member.userId)
    }
}
