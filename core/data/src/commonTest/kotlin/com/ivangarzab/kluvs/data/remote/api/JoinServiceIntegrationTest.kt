package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.ClubCreateRequestDto
import com.ivangarzab.kluvs.api.models.ClubUpdateRequestDto
import com.ivangarzab.kluvs.api.models.JoinRequestDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for [JoinService] using local Supabase instance.
 *
 * No invites are seeded, so the happy-path test builds its own flow: a bot-created
 * web-only club is switched to INVITE_LINK (which mints an invite token), then the
 * seeded auth user Ivan Garza joins through that token. The club is deleted
 * afterwards, which also removes Ivan's temporary membership.
 */
class JoinServiceIntegrationTest {

    private suspend fun joinService(): JoinService =
        JoinServiceImpl(createUserAuthedSupabaseClient())

    @Test
    fun testPreviewInvalidToken() = runTest {
        // When/Then: previewing a token that doesn't exist fails (404)
        assertFailsWith<Exception> {
            joinService().preview("00000000-0000-0000-0000-000000000000")
        }
    }

    @Test
    fun testFullJoinFlow() = runTest {
        val clubService = ClubServiceImpl(createBotSupabaseClient())
        var clubId: String? = null
        try {
            // Given: a fresh web-only club with an invite link enabled
            val created = clubService.create(ClubCreateRequestDto(name = "Join Flow Test Club"))
            clubId = assertNotNull(created.club?.id, "Club creation should return an ID")

            clubService.update(
                ClubUpdateRequestDto(
                    id = clubId,
                    joinPolicy = ClubUpdateRequestDto.JoinPolicy.INVITE_LINK,
                )
            )
            val token = assertNotNull(
                clubService.get(clubId).inviteToken,
                "INVITE_LINK club should expose an invite token"
            )

            val service = joinService()

            // When: previewing the invite
            val preview = service.preview(token)

            // Then: the preview shows the club
            assertEquals(true, preview.valid)
            assertEquals("Join Flow Test Club", preview.club?.name)

            // When: joining through the invite as Ivan
            val joined = service.join(JoinRequestDto(token = token))

            // Then: Ivan is now a member of the club
            assertTrue(joined.success == true, "Join should succeed")
            assertEquals(clubId, joined.clubId)

            val member = MemberServiceImpl(createBotSupabaseClient()).get(TEST_USER_MEMBER_ID.toString())
            assertTrue(member.clubs?.any { it.id == clubId } == true,
                "Ivan should belong to the joined club")
        } finally {
            // Cleanup: deleting the club cascades the invite and Ivan's membership
            clubId?.let {
                try { clubService.delete(it) } catch (_: Exception) { }
            }
        }
    }
}
