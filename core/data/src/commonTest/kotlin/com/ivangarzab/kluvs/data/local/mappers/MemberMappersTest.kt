package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.database.entities.MemberEntity
import com.ivangarzab.kluvs.model.Member
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MemberMappersTest {

    @Test
    fun testMemberEntity_toDomain() {
        // Given
        val entity = MemberEntity(
            id = "member-1",
            userId = "user-1",
            name = "John Doe",
            handle = "@johndoe",
            avatarPath = "/avatars/johndoe.png",
            booksRead = 5,
            createdAt = "2024-01-01T12:00:00",
            lastFetchedAt = 1234567890L
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals("member-1", domain.id)
        assertEquals("John Doe", domain.name)
        assertEquals("@johndoe", domain.handle)
        assertEquals("/avatars/johndoe.png", domain.avatarPath)
        assertEquals(5, domain.booksRead)
        assertEquals("user-1", domain.userId)
        assertEquals(LocalDateTime.parse("2024-01-01T12:00:00"), domain.createdAt)
        assertNull(domain.clubs) // Relationship not loaded
        assertNull(domain.shameClubs) // Relationship not loaded
    }

    @Test
    fun testMemberEntity_toDomain_withNullCreatedAt() {
        // Given
        val entity = MemberEntity(
            id = "member-1",
            userId = "user-1",
            name = "John Doe",
            handle = null,
            avatarPath = null,
            booksRead = 0,
            createdAt = null,
            lastFetchedAt = 1234567890L
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals("member-1", domain.id)
        assertNull(domain.createdAt)
    }

    @Test
    fun testMember_toEntity() {
        // Given
        val domain = Member(
            id = "member-1",
            name = "John Doe",
            handle = "@johndoe",
            avatarPath = "/avatars/johndoe.png",
            booksRead = 5,
            userId = "user-1",
            createdAt = LocalDateTime.parse("2024-01-01T12:00:00")
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals("member-1", entity.id)
        assertEquals("user-1", entity.userId)
        assertEquals("John Doe", entity.name)
        assertEquals("@johndoe", entity.handle)
        assertEquals("/avatars/johndoe.png", entity.avatarPath)
        assertEquals(5, entity.booksRead)
        assertEquals("2024-01-01T12:00", entity.createdAt)
        assertNotNull(entity.lastFetchedAt)
    }
}
