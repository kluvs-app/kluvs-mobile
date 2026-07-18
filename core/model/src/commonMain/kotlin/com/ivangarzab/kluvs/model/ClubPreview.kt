package com.ivangarzab.kluvs.model

/**
 * Minimal club information, used when a club is referenced without its full
 * details — e.g. previewing an invite link or listing reading log entries.
 */
data class ClubPreview(

    val id: String,

    val name: String
)
