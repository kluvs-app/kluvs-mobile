package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ClubPreviewDto
import kotlin.test.Test
import kotlin.test.assertEquals

class JoinMappersTest {

    @Test
    fun `ClubPreviewDto toDomain maps complete preview`() {
        val preview = ClubPreviewDto(id = "club-1", name = "Freaks & Geeks").toDomain()

        assertEquals("club-1", preview.id)
        assertEquals("Freaks & Geeks", preview.name)
    }
}
