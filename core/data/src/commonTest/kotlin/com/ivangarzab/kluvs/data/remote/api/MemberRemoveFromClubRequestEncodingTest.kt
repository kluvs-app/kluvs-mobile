package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.MemberUpdateRequestDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the wire format of the `remove_from_club` request.
 *
 * The backend rejects `remove_from_club` combined with ANY other update field (a null still
 * counts as present), so the body must be exactly `{ id, remove_from_club }`. This relies on
 * `explicitNulls = false` in [getJsonForSupabaseService]; if that ever changes, every removal
 * would start failing with 400, and this test says why.
 */
class MemberRemoveFromClubRequestEncodingTest {

    @Test
    fun `remove_from_club request body contains only id and remove_from_club`() {
        val body = getJsonForSupabaseService()
            .encodeToString(MemberUpdateRequestDto(id = 5, removeFromClub = "club-1"))

        assertEquals("""{"id":5,"remove_from_club":"club-1"}""", body)
    }
}
