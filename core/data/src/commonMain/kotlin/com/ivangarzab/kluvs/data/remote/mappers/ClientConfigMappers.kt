package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ClientConfigDto
import com.ivangarzab.kluvs.model.ClientConfig

/**
 * Maps a [ClientConfigDto] to a [ClientConfig] domain model.
 */
fun ClientConfigDto.toDomain(): ClientConfig {
    return ClientConfig(
        flags = flags,
        minSupportedVersion = minSupportedVersion
    )
}
