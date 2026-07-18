package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.database.entities.DiscussionAttendanceEntity
import com.ivangarzab.kluvs.model.AttendanceStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DiscussionAttendanceMappersTest {

    @Test
    fun testDiscussionAttendanceEntity_toDomain() {
        val entity = DiscussionAttendanceEntity(
            discussionId = "discussion-1",
            status = "MAYBE",
            lastFetchedAt = 1234567890L
        )

        assertEquals(AttendanceStatus.MAYBE, entity.toDomain())
    }

    @Test
    fun testAttendanceStatus_toEntity() {
        val entity = AttendanceStatus.YES.toEntity("discussion-1")

        assertEquals("discussion-1", entity.discussionId)
        assertEquals("YES", entity.status)
        assertNotNull(entity.lastFetchedAt)
    }
}
