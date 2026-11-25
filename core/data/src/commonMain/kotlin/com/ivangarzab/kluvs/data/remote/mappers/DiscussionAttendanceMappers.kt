package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.bark.Bark
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
        responses = responses.mapNotNull { it.toDomain() },
        myStatus = when (myStatus) {
            DiscussionAttendanceRosterResponseDto.MyStatus.yes -> AttendanceStatus.YES
            DiscussionAttendanceRosterResponseDto.MyStatus.no -> AttendanceStatus.NO
            DiscussionAttendanceRosterResponseDto.MyStatus.maybe -> AttendanceStatus.MAYBE
            // Shown as "no response yet" rather than guessing an answer.
            DiscussionAttendanceRosterResponseDto.MyStatus.unknown_default_open_api -> {
                Bark.w("Unknown RSVP status for current member from API; treating as no response")
                null
            }
            null -> null
        },
        totalMembers = totalMembers
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterEntryDto] to an
 * [AttendanceResponse] domain model.
 *
 * Returns null (the entry is dropped from the roster) when its status is unknown to this
 * app version: any guess would miscount who's coming.
 */
fun DiscussionAttendanceRosterEntryDto.toDomain(): AttendanceResponse? {
    val status = when (status) {
        DiscussionAttendanceRosterEntryDto.Status.yes -> AttendanceStatus.YES
        DiscussionAttendanceRosterEntryDto.Status.no -> AttendanceStatus.NO
        DiscussionAttendanceRosterEntryDto.Status.maybe -> AttendanceStatus.MAYBE
        DiscussionAttendanceRosterEntryDto.Status.unknown_default_open_api -> {
            Bark.w("Unknown RSVP status from API; dropping roster entry (Member ID: $memberId)")
            return null
        }
    }
    return AttendanceResponse(
        memberId = memberId.toString(),
        name = name,
        status = status
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.DiscussionAttendanceDto]'s status to an
 * [AttendanceStatus] domain model.
 *
 * Returns null for a status this app version doesn't know; the caller falls back to
 * the status it just sent.
 */
fun DiscussionAttendanceDto.Status.toDomain(): AttendanceStatus? = when (this) {
    DiscussionAttendanceDto.Status.yes -> AttendanceStatus.YES
    DiscussionAttendanceDto.Status.no -> AttendanceStatus.NO
    DiscussionAttendanceDto.Status.maybe -> AttendanceStatus.MAYBE
    DiscussionAttendanceDto.Status.unknown_default_open_api -> null
}

/**
 * Maps an [AttendanceStatus] domain model to its upsert request payload counterpart.
 */
fun AttendanceStatus.toDto(): DiscussionAttendanceUpsertRequestDto.Status = when (this) {
    AttendanceStatus.YES -> DiscussionAttendanceUpsertRequestDto.Status.yes
    AttendanceStatus.NO -> DiscussionAttendanceUpsertRequestDto.Status.no
    AttendanceStatus.MAYBE -> DiscussionAttendanceUpsertRequestDto.Status.maybe
}
