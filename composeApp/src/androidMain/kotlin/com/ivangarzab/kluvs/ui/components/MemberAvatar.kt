package com.ivangarzab.kluvs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.ivangarzab.kluvs.theme.KluvsTheme

/**
 * Displays a member's avatar image with fallback to placeholder.
 *
 * Role is communicated via [RoleEyebrow] alongside the avatar, not on the avatar itself.
 *
 * @param avatarUrl The URL of the avatar image, or null for placeholder
 * @param size The diameter of the circular avatar
 * @param modifier Modifier for the composable
 * @param contentDescription Accessibility description
 * @param onClick Optional click handler (e.g., for editing)
 * @param isLoading Whether to show a loading indicator
 */
@Composable
fun MemberAvatar(
    avatarUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    isLoading: Boolean = false
) {
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
        modifier = baseModifier.clip(CircleShape),
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
}

@PreviewLightDark
@Composable
fun Preview_MemberAvatar() = KluvsTheme {
    Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
        MemberAvatar(
            avatarUrl = null,
            size = 60.dp
        )
    }
}
