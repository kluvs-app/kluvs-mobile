package com.ivangarzab.kluvs.settings.presentation

/**
 * UI model for the profile fields editable in SettingsScreen.
 *
 * Pre-populated with the member's current values when the screen loads.
 */
data class EditableProfile(
    val memberId: String,
    val name: String,
    val handle: String
)
