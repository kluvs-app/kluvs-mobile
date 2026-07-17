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
            id = 1,
            name = "Owner",
            booksRead = 10,
            role = ClubMemberDto.Role.owner,
            clubs = emptyList()
        )

        val adminDto = ClubMemberDto(
            id = 2,
            name = "Admin",
            booksRead = 5,
            role = ClubMemberDto.Role.admin,
            clubs = emptyList()
        )

        val memberDto = ClubMemberDto(
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
        // Given: A ClubMemberDto with minimal fields
        val dto = ClubMemberDto(
            id = 4,
            name = null,
            handle = null,
            avatarPath = null,
            booksRead = 0,
            role = null,
            createdAt = null,
            clubs = emptyList()
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Nullable fields are handled correctly, role defaults to MEMBER
        assertEquals(Role.MEMBER, domain.role)
        assertEquals("4", domain.member.id)
        assertEquals("", domain.member.name) // Default empty string for null name
        assertNull(domain.member.handle)
        assertNull(domain.member.avatarPath)
        assertNull(domain.member.userId)
        assertNull(domain.member.createdAt)
    }
}
