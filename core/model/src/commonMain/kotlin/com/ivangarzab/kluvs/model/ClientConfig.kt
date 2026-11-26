package com.ivangarzab.kluvs.model

/**
 * Backend configuration resolved for this installed app.
 *
 * @property flags every feature flag key mapped to whether it is on for this app's version.
 * @property minSupportedVersion oldest supported version (MAJOR.MINOR.PATCH) for this platform,
 * or null when the backend sets no minimum.
 */
data class ClientConfig(

    val flags: Map<String, Boolean>,

    val minSupportedVersion: String?
)
