package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ClubPreviewDto
import com.ivangarzab.kluvs.model.ClubPreview

/**
 * Maps a [com.ivangarzab.kluvs.api.models.ClubPreviewDto] to a [ClubPreview] domain model,
 * or null when the preview carries no club ID.
 */
fun ClubPreviewDto.toDomain(): ClubPreview? {
    val clubId = id ?: return null
    return ClubPreview(
        id = clubId,
        name = name ?: ""
    )
}
