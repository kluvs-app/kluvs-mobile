package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ClubPreviewDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JoinMappersTest {

    @Test
    fun `ClubPreviewDto toDomain maps complete preview`() {
        val preview = ClubPreviewDto(id = "club-1", name = "Freaks & Geeks").toDomain()

        assertNotNull(preview)
        assertEquals("club-1", preview.id)
        assertEquals("Freaks & Geeks", preview.name)
    }

    @Test
    fun `ClubPreviewDto toDomain returns null without an ID`() {
        assertNull(ClubPreviewDto(name = "Nameless").toDomain())
    }

    @Test
    fun `ClubPreviewDto toDomain falls back to empty name`() {
        assertEquals("", ClubPreviewDto(id = "club-1").toDomain()?.name)
    }
}
