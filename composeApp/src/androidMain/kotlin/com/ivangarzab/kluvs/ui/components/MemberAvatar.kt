package com.ivangarzab.kluvs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.utils.getRoleIcon
import com.ivangarzab.kluvs.ui.utils.getRoleRimColor

/**
 * Displays a member's avatar image with fallback to placeholder.
 *
 * Shows a colored rim and role icon overlay for OWNER (gold crown) and ADMIN (blue shield).
 *
 * @param avatarUrl The URL of the avatar image, or null for placeholder
 * @param size The diameter of the circular avatar
 * @param modifier Modifier for the composable
 * @param role Optional role to display visual indicator (rim + icon)
 * @param contentDescription Accessibility description
 * @param onClick Optional click handler (e.g., for editing)
 * @param isLoading Whether to show a loading indicator
 */
@Composable
fun MemberAvatar(
    avatarUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    role: Role? = null,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    isLoading: Boolean = false
) {
    val rimColor = role?.let { getRoleRimColor(it) }
    val roleIcon = role?.let { getRoleIcon(it) }
    val iconAlignment = role?.let {
        if (it == Role.OWNER) Alignment.TopCenter else Alignment.BottomCenter
    } ?: Alignment.BottomCenter
    val iconAlignmentOffset = role?.let {
        if (it == Role.OWNER) -5.0 else 5.0
    } ?: 4.0


    val baseModifier = modifier
        .padding(4.dp)
        .size(size)
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .then(
                    if (rimColor != null) {
                        Modifier.border(width = 2.dp, color = rimColor, shape = CircleShape)
                    } else {
                        Modifier
                    }
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                placeholder = painterResource(R.drawable.img_fallback),
                fallback = painterResource(R.drawable.img_fallback),
                contentScale = ContentScale.Crop
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size * 0.5f),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (roleIcon != null && rimColor != null) {
            val iconSize = size * 0.28f
            Icon(
                painter = painterResource(roleIcon),
                contentDescription = "Role: ${role.name}",
                modifier = Modifier
                    .size(iconSize)
                    .align(iconAlignment)
                    .offset(y = iconAlignmentOffset.dp),
                tint = rimColor
            )
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_MemberAvatar() = KluvsTheme {
    Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            // Regular member (no rim)
            MemberAvatar(
                avatarUrl = null,
                size = 60.dp,
                role = Role.MEMBER
            )
            // Admin with blue rim and shield
            MemberAvatar(
                avatarUrl = null,
                size = 60.dp,
                role = Role.ADMIN
            )
            // Owner with gold rim and crown
            MemberAvatar(
                avatarUrl = null,
                size = 60.dp,
                role = Role.OWNER
            )
        }
    }
}
