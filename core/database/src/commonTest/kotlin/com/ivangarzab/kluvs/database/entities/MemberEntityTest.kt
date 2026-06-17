package com.ivangarzab.kluvs.database.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MemberEntityTest {
    @Test
    fun testMemberEntity_creation() {
        // Given
        val memberEntity = MemberEntity(
            id = "member-1",
            userId = "user-1",
            name = "John Doe",
            handle = "@johndoe",
            avatarPath = "/avatars/johndoe.png",
            booksRead = 5,
            createdAt = "2024-01-01T12:00:00Z",
            lastFetchedAt = 1234567890L
        )

        // Then
        assertEquals("member-1", memberEntity.id)
        assertEquals("user-1", memberEntity.userId)
        assertEquals("John Doe", memberEntity.name)
        assertEquals("@johndoe", memberEntity.handle)
        assertEquals("/avatars/johndoe.png", memberEntity.avatarPath)
        assertEquals(5, memberEntity.booksRead)
        assertEquals("2024-01-01T12:00:00Z", memberEntity.createdAt)
        assertEquals(1234567890L, memberEntity.lastFetchedAt)
    }

    @Test
    fun testMemberEntity_withNullFields() {
        // Given
        val memberEntity = MemberEntity(
            id = "member-1",
            userId = null,
            name = null,
            handle = null,
            avatarPath = null,
            booksRead = 0,
            createdAt = null,
            lastFetchedAt = 1234567890L
        )

        // Then
        assertEquals("member-1", memberEntity.id)
        assertEquals(null, memberEntity.userId)
        assertEquals(null, memberEntity.name)
        assertEquals(null, memberEntity.handle)
        assertEquals(null, memberEntity.avatarPath)
        assertEquals(null, memberEntity.createdAt)
        assertEquals(0, memberEntity.booksRead)
    }

    @Test
    fun testMemberEntity_copy() {
        // Given
        val original = MemberEntity(
            id = "member-1",
            userId = "user-1",
            name = "John Doe",
            handle = "@johndoe",
            avatarPath = "/avatars/johndoe.png",
            booksRead = 5,
            createdAt = "2024-01-01T12:00:00Z",
            lastFetchedAt = 1234567890L
        )

        // When
        val updated = original.copy(
            booksRead = 6,
            lastFetchedAt = 9876543210L
        )

        // Then
        assertEquals("member-1", updated.id)
        assertEquals(6, updated.booksRead)
        assertEquals("2024-01-01T12:00:00Z", updated.createdAt)
        assertEquals(9876543210L, updated.lastFetchedAt)
    }

    @Test
    fun testMemberEntity_equality() {
        // Given
        val member1 = MemberEntity(
            id = "member-1",
            userId = "user-1",
            name = "John Doe",
            handle = "@johndoe",
            avatarPath = "/avatars/johndoe.png",
            booksRead = 5,
            createdAt = "2024-01-01T12:00:00Z",
            lastFetchedAt = 1234567890L
        )

        val member2 = MemberEntity(
            id = "member-1",
            userId = "user-1",
            name = "John Doe",
            handle = "@johndoe",
            avatarPath = "/avatars/johndoe.png",
            booksRead = 5,
            createdAt = "2024-01-01T12:00:00Z",
            lastFetchedAt = 1234567890L
        )

        val member3 = MemberEntity(
            id = "member-2",
            userId = "user-2",
            name = "Jane Doe",
            handle = "@janedoe",
            avatarPath = "/avatars/janedoe.png",
            booksRead = 3,
            createdAt = "2024-01-15T12:00:00Z",
            lastFetchedAt = 1234567890L
        )

        // Then
        assertEquals(member1, member2)
        assertNotEquals(member1, member3)
    }
}
