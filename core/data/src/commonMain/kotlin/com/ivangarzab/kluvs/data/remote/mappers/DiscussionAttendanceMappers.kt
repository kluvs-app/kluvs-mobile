package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.DiscussionAttendanceDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterEntryDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterResponseDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceUpsertRequestDto
import com.ivangarzab.kluvs.model.AttendanceResponse
import com.ivangarzab.kluvs.model.AttendanceRoster
import com.ivangarzab.kluvs.model.AttendanceStatus

/**
 * Maps a [com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterResponseDto] from the API
 * to an [AttendanceRoster] domain model.
 */
fun DiscussionAttendanceRosterResponseDto.toDomain(): AttendanceRoster {
    return AttendanceRoster(
        responses = responses.map { it.toDomain() },
        myStatus = when (myStatus) {
            DiscussionAttendanceRosterResponseDto.MyStatus.yes -> AttendanceStatus.YES
            DiscussionAttendanceRosterResponseDto.MyStatus.no -> AttendanceStatus.NO
            DiscussionAttendanceRosterResponseDto.MyStatus.maybe -> AttendanceStatus.MAYBE
            null -> null
        },
        totalMembers = totalMembers
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterEntryDto] to an
 * [AttendanceResponse] domain model.
 */
fun DiscussionAttendanceRosterEntryDto.toDomain(): AttendanceResponse {
    return AttendanceResponse(
        memberId = memberId.toString(),
        name = name,
        status = when (status) {
            DiscussionAttendanceRosterEntryDto.Status.yes -> AttendanceStatus.YES
            DiscussionAttendanceRosterEntryDto.Status.no -> AttendanceStatus.NO
            DiscussionAttendanceRosterEntryDto.Status.maybe -> AttendanceStatus.MAYBE
        }
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.DiscussionAttendanceDto]'s status to an
 * [AttendanceStatus] domain model.
 */
fun DiscussionAttendanceDto.Status.toDomain(): AttendanceStatus = when (this) {
    DiscussionAttendanceDto.Status.yes -> AttendanceStatus.YES
    DiscussionAttendanceDto.Status.no -> AttendanceStatus.NO
    DiscussionAttendanceDto.Status.maybe -> AttendanceStatus.MAYBE
}

/**
 * Maps an [AttendanceStatus] domain model to its upsert request payload counterpart.
 */
fun AttendanceStatus.toDto(): DiscussionAttendanceUpsertRequestDto.Status = when (this) {
    AttendanceStatus.YES -> DiscussionAttendanceUpsertRequestDto.Status.yes
    AttendanceStatus.NO -> DiscussionAttendanceUpsertRequestDto.Status.no
    AttendanceStatus.MAYBE -> DiscussionAttendanceUpsertRequestDto.Status.maybe
}
