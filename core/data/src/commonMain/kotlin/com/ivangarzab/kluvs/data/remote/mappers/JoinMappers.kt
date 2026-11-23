package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ClubPreviewDto
import com.ivangarzab.kluvs.model.ClubPreview

/**
 * Maps a [com.ivangarzab.kluvs.api.models.ClubPreviewDto] to a [ClubPreview] domain model.
 */
fun ClubPreviewDto.toDomain(): ClubPreview {
    return ClubPreview(
        id = id,
        name = name
    )
}
