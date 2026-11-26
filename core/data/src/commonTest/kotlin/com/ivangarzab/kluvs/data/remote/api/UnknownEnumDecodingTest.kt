package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.ClubDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterResponseDto
import com.ivangarzab.kluvs.api.models.ShelfListResponseDto
import com.ivangarzab.kluvs.api.models.ShelfStatusDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins that an enum value the app doesn't know yet decodes to `unknown_default_open_api`
 * instead of throwing. This relies on `enumUnknownDefaultCase` plus the fallback serializer
 * added by `core/api/templates/`; if either is lost on a generator change, an API that adds
 * an enum value would crash every shipped app version, and this test says why.
 */
class UnknownEnumDecodingTest {

    private val json = getJsonForSupabaseService()

    @Test
    fun `nested enum with an unknown value decodes to the unknown case`() {
        val club = json.decodeFromString<ClubDto>(
            """{"id":"club-1","name":"Readers","join_policy":"REQUEST_TO_JOIN"}"""
        )

        assertEquals(ClubDto.JoinPolicy.unknown_default_open_api, club.joinPolicy)
    }

    @Test
    fun `known nested enum values still decode`() {
        val club = json.decodeFromString<ClubDto>(
            """{"id":"club-1","name":"Readers","join_policy":"INVITE_LINK"}"""
        )

        assertEquals(ClubDto.JoinPolicy.INVITE_LINK, club.joinPolicy)
    }

    @Test
    fun `top-level enum with an unknown value decodes to the unknown case`() {
        val response = json.decodeFromString<ShelfListResponseDto>(
            """
            {"success":true,"shelves":[
              {"shelf":"abandoned","updated_at":"2026-07-01T10:30:00","finished_at":null,
               "source":"manual","book":{"id":5,"title":"The Hobbit","author":"J.R.R. Tolkien"}},
              {"shelf":"read","updated_at":"2026-07-01T10:30:00","finished_at":null,
               "source":"import","book":{"id":6,"title":"Dune","author":"Frank Herbert"}}
            ]}
            """.trimIndent()
        )

        assertEquals(ShelfStatusDto.unknown_default_open_api, response.shelves[0].shelf)
        assertEquals(ShelfStatusDto.read, response.shelves[1].shelf)
    }

    @Test
    fun `nullable enum with an unknown value decodes to the unknown case`() {
        val roster = json.decodeFromString<DiscussionAttendanceRosterResponseDto>(
            """{"responses":[{"member_id":1,"name":"Ana","status":"late"}],"my_status":"late","total_members":3}"""
        )

        assertEquals(DiscussionAttendanceRosterResponseDto.MyStatus.unknown_default_open_api, roster.myStatus)
    }

    @Test
    fun `enum values encode with their wire value`() {
        val body = json.encodeToString(ClubDto.serializer(), ClubDto(id = "club-1", name = "Readers", joinPolicy = ClubDto.JoinPolicy.PRIVATE))

        assertEquals(true, body.contains(""""join_policy":"PRIVATE""""))
    }
}
