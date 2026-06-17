package com.ivangarzab.kluvs.ui.utils

import androidx.compose.ui.graphics.Color
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.theme.blue
import com.ivangarzab.kluvs.theme.gold

/**
 * Gets the rim color for a member's role badge.
 *
 * @param role The member's role in the club
 * @return Color for the role's rim, or null if no rim should be shown
 */
fun getRoleRimColor(role: Role): Color? = when (role) {
    Role.OWNER -> gold
    Role.ADMIN -> blue
    Role.MEMBER -> null
}

/**
 * Gets the icon resource ID for a member's role badge.
 *
 * @param role The member's role in the club
 * @return Drawable resource ID for the role's icon, or null if no icon should be shown
 */
fun getRoleIcon(role: Role): Int? = when (role) {
    Role.OWNER -> R.drawable.ic_crown
    Role.ADMIN -> R.drawable.ic_shield
    Role.MEMBER -> null
}
