package com.ivangarzab.kluvs.database.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DiscussionAttendanceEntityTest {

    @Test
    fun testDiscussionAttendanceEntity_creation() {
        val entity = DiscussionAttendanceEntity(
            discussionId = "discussion-1",
            status = "YES",
            lastFetchedAt = 1234567890L
        )

        assertEquals("discussion-1", entity.discussionId)
        assertEquals("YES", entity.status)
        assertEquals(1234567890L, entity.lastFetchedAt)
    }

    @Test
    fun testDiscussionAttendanceEntity_copy() {
        val original = DiscussionAttendanceEntity("discussion-1", "MAYBE", 1234567890L)
        val updated = original.copy(status = "NO", lastFetchedAt = 9876543210L)

        assertEquals("discussion-1", updated.discussionId)
        assertEquals("NO", updated.status)
        assertEquals(9876543210L, updated.lastFetchedAt)
    }

    @Test
    fun testDiscussionAttendanceEntity_equality() {
        val attendance1 = DiscussionAttendanceEntity("discussion-1", "YES", 1234567890L)
        val attendance2 = DiscussionAttendanceEntity("discussion-1", "YES", 1234567890L)
        val attendance3 = DiscussionAttendanceEntity("discussion-2", "NO", 1234567890L)

        assertEquals(attendance1, attendance2)
        assertNotEquals(attendance1, attendance3)
    }
}
