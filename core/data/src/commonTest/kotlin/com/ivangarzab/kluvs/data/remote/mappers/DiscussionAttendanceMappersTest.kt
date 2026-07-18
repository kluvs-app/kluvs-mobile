package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.DiscussionAttendanceDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterEntryDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterResponseDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceUpsertRequestDto
import com.ivangarzab.kluvs.model.AttendanceStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DiscussionAttendanceMappersTest {

    @Test
    fun `roster response toDomain maps responses and my status`() {
        // Given: A roster with two responses and the caller RSVP'd yes
        val dto = DiscussionAttendanceRosterResponseDto(
            responses = listOf(
                DiscussionAttendanceRosterEntryDto(
                    memberId = 1,
                    name = "Ivan Garza",
                    status = DiscussionAttendanceRosterEntryDto.Status.yes
                ),
                DiscussionAttendanceRosterEntryDto(
                    memberId = 2,
                    name = null,
                    status = DiscussionAttendanceRosterEntryDto.Status.maybe
                )
            ),
            myStatus = DiscussionAttendanceRosterResponseDto.MyStatus.yes,
            totalMembers = 5
        )

        // When: Mapping to domain
        val roster = dto.toDomain()

        // Then: All fields map over, with member IDs stringified
        assertEquals(2, roster.responses.size)
        assertEquals("1", roster.responses[0].memberId)
        assertEquals("Ivan Garza", roster.responses[0].name)
        assertEquals(AttendanceStatus.YES, roster.responses[0].status)
        assertEquals(AttendanceStatus.MAYBE, roster.responses[1].status)
        assertEquals(AttendanceStatus.YES, roster.myStatus)
        assertEquals(5, roster.totalMembers)
    }

    @Test
    fun `roster response toDomain maps null my status to unanswered`() {
        val dto = DiscussionAttendanceRosterResponseDto(
            responses = emptyList(),
            myStatus = null,
            totalMembers = 3
        )

        assertNull(dto.toDomain().myStatus)
    }

    @Test
    fun `attendance status enums map in both directions`() {
        assertEquals(AttendanceStatus.YES, DiscussionAttendanceDto.Status.yes.toDomain())
        assertEquals(AttendanceStatus.NO, DiscussionAttendanceDto.Status.no.toDomain())
        assertEquals(AttendanceStatus.MAYBE, DiscussionAttendanceDto.Status.maybe.toDomain())
        assertEquals(DiscussionAttendanceUpsertRequestDto.Status.yes, AttendanceStatus.YES.toDto())
        assertEquals(DiscussionAttendanceUpsertRequestDto.Status.no, AttendanceStatus.NO.toDto())
        assertEquals(DiscussionAttendanceUpsertRequestDto.Status.maybe, AttendanceStatus.MAYBE.toDto())
    }
}
