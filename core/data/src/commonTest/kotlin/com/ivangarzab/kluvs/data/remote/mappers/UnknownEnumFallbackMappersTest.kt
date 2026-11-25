package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.BookDto
import com.ivangarzab.kluvs.api.models.ClubDetailDto
import com.ivangarzab.kluvs.api.models.ClubDto
import com.ivangarzab.kluvs.api.models.ClubMemberDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterEntryDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterResponseDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteDto
import com.ivangarzab.kluvs.api.models.MemberClubEntryDto
import com.ivangarzab.kluvs.api.models.ReadingProgressDto
import com.ivangarzab.kluvs.api.models.ServerClubSummaryDto
import com.ivangarzab.kluvs.api.models.ShelfEntryDto
import com.ivangarzab.kluvs.api.models.ShelfListResponseDto
import com.ivangarzab.kluvs.api.models.ShelfStatusDto
import com.ivangarzab.kluvs.model.AttendanceStatus
import com.ivangarzab.kluvs.model.JoinPolicy
import com.ivangarzab.kluvs.model.NoteVisibility
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Generated DTO enums decode values this app version doesn't know to
 * `unknown_default_open_api`. These tests pin what each mapper turns that into: the most
 * restrictive domain value, or nothing when any guess would mislead.
 */
class UnknownEnumFallbackMappersTest {

    private val unknownClubMember = ClubMemberDto(
        id = 7,
        name = "Ana",
        role = ClubMemberDto.Role.unknown_default_open_api,
        joinedAt = "2026-07-01T10:30:00",
        booksRead = null,
        handle = "ana",
        avatarPath = null,
        createdAt = "2026-07-01T10:30:00",
        clubs = emptyList()
    )

    private fun shelfEntry(shelf: ShelfStatusDto, source: ShelfEntryDto.Source) = ShelfEntryDto(
        shelf = shelf,
        updatedAt = "2026-07-01T10:30:00",
        finishedAt = null,
        source = source,
        book = BookDto(id = 5, title = "The Hobbit", author = "J.R.R. Tolkien")
    )

    private fun progress(
        type: ReadingProgressDto.ProgressType,
        status: ReadingProgressDto.Status
    ) = ReadingProgressDto(
        id = "progress-1",
        memberId = 1,
        bookId = 5,
        sessionId = null,
        progressType = type,
        currentPage = 50,
        percentComplete = null,
        status = status,
        startedAt = "2026-07-01T10:30:00",
        updatedAt = "2026-07-01T10:30:00",
        completedAt = null,
        book = null
    )

    @Test
    fun `unknown join policy maps to PRIVATE in every club shape`() {
        val club = ClubDto(id = "club-1", name = "Readers", joinPolicy = ClubDto.JoinPolicy.unknown_default_open_api)
        val detail = ClubDetailDto(
            id = "club-1",
            name = "Readers",
            discordChannel = null,
            serverId = null,
            foundedDate = null,
            joinPolicy = ClubDetailDto.JoinPolicy.unknown_default_open_api,
            inviteToken = null,
            members = emptyList(),
            activeSession = null,
            pastSessions = emptyList()
        )
        val entry = MemberClubEntryDto(
            id = "club-1",
            name = "Readers",
            joinPolicy = MemberClubEntryDto.JoinPolicy.unknown_default_open_api,
            role = MemberClubEntryDto.Role.member,
            joinedAt = "2026-07-01T10:30:00"
        )
        val summary = ServerClubSummaryDto(
            id = "club-1",
            name = "Readers",
            joinPolicy = ServerClubSummaryDto.JoinPolicy.unknown_default_open_api,
            memberCount = 3,
            latestSession = null
        )

        assertEquals(JoinPolicy.PRIVATE, club.toDomain().joinPolicy)
        assertEquals(JoinPolicy.PRIVATE, detail.toDomain().joinPolicy)
        assertEquals(JoinPolicy.PRIVATE, entry.toDomain().joinPolicy)
        assertEquals(JoinPolicy.PRIVATE, summary.toDomain().joinPolicy)
    }

    @Test
    fun `unknown club role maps to MEMBER`() {
        val entry = MemberClubEntryDto(
            id = "club-1",
            name = "Readers",
            joinPolicy = MemberClubEntryDto.JoinPolicy.PRIVATE,
            role = MemberClubEntryDto.Role.unknown_default_open_api,
            joinedAt = "2026-07-01T10:30:00"
        )

        assertEquals(Role.MEMBER, entry.toDomain().role)
        assertEquals(Role.MEMBER, unknownClubMember.toDomain().role)
    }

    @Test
    fun `unknown note visibility maps to PRIVATE`() {
        val note = DiscussionNoteDto(
            id = "note-1",
            discussionId = "discussion-1",
            memberId = 1,
            content = "Thoughts",
            visibility = DiscussionNoteDto.Visibility.unknown_default_open_api,
            createdAt = "2026-07-01T10:30:00",
            updatedAt = "2026-07-01T10:30:00"
        )

        assertEquals(NoteVisibility.PRIVATE, note.toDomain().visibility)
    }

    @Test
    fun `unknown shelf status maps to null`() {
        assertNull(ShelfStatusDto.unknown_default_open_api.toDomain())
    }

    @Test
    fun `shelf list drops entries with an unknown shelf status`() {
        val response = ShelfListResponseDto(
            success = true,
            shelves = listOf(
                shelfEntry(ShelfStatusDto.unknown_default_open_api, ShelfEntryDto.Source.manual),
                shelfEntry(ShelfStatusDto.read, ShelfEntryDto.Source.manual)
            )
        )

        val entries = response.toDomain()

        assertEquals(1, entries.size)
        assertEquals(ShelfStatus.READ, entries.single().shelf)
    }

    @Test
    fun `unknown shelf source maps to MANUAL`() {
        val entry = shelfEntry(ShelfStatusDto.read, ShelfEntryDto.Source.unknown_default_open_api).toDomain()

        assertEquals(ShelfSource.MANUAL, assertNotNull(entry).source)
    }

    @Test
    fun `progress with an unknown type maps to null`() {
        val dto = progress(ReadingProgressDto.ProgressType.unknown_default_open_api, ReadingProgressDto.Status.in_progress)

        assertNull(dto.toDomain())
    }

    @Test
    fun `unknown progress status maps to IN_PROGRESS`() {
        val dto = progress(ReadingProgressDto.ProgressType.page, ReadingProgressDto.Status.unknown_default_open_api)

        assertEquals(ProgressStatus.IN_PROGRESS, assertNotNull(dto.toDomain()).status)
    }

    @Test
    fun `roster drops entries with an unknown status and treats an unknown own status as no response`() {
        val roster = DiscussionAttendanceRosterResponseDto(
            responses = listOf(
                DiscussionAttendanceRosterEntryDto(
                    memberId = 1,
                    name = "Ana",
                    status = DiscussionAttendanceRosterEntryDto.Status.unknown_default_open_api
                ),
                DiscussionAttendanceRosterEntryDto(
                    memberId = 2,
                    name = "Ben",
                    status = DiscussionAttendanceRosterEntryDto.Status.yes
                )
            ),
            myStatus = DiscussionAttendanceRosterResponseDto.MyStatus.unknown_default_open_api,
            totalMembers = 3
        ).toDomain()

        assertEquals(listOf("2"), roster.responses.map { it.memberId })
        assertNull(roster.myStatus)
        assertEquals(3, roster.totalMembers)
    }

    @Test
    fun `unknown attendance status maps to null`() {
        assertNull(DiscussionAttendanceDto.Status.unknown_default_open_api.toDomain())
        assertEquals(AttendanceStatus.YES, DiscussionAttendanceDto.Status.yes.toDomain())
    }
}
