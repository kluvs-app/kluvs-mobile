package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.database.entities.DiscussionAttendanceEntity
import com.ivangarzab.kluvs.model.AttendanceStatus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Maps a [DiscussionAttendanceEntity] from the local database to an [AttendanceStatus].
 */
fun DiscussionAttendanceEntity.toDomain(): AttendanceStatus = AttendanceStatus.valueOf(status)

/**
 * Maps an [AttendanceStatus] to a [DiscussionAttendanceEntity] for local database storage.
 * Sets lastFetchedAt to current time.
 */
@OptIn(ExperimentalTime::class)
fun AttendanceStatus.toEntity(discussionId: String): DiscussionAttendanceEntity {
    return DiscussionAttendanceEntity(
        discussionId = discussionId,
        status = name,
        lastFetchedAt = Clock.System.now().toEpochMilliseconds()
    )
}
