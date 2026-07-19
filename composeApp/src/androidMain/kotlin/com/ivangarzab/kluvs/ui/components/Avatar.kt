package com.ivangarzab.kluvs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.theme.avatarHuesDark
import com.ivangarzab.kluvs.theme.avatarHuesLight
import com.ivangarzab.kluvs.theme.brandOnPrimary
import com.ivangarzab.kluvs.theme.brandPrimary
import com.ivangarzab.kluvs.theme.ebGaramond
import com.ivangarzab.kluvs.theme.foregroundLightLabelVariant
import com.ivangarzab.kluvs.theme.foregroundWarmPrimary
import kotlin.math.abs

/**
 * Circular, generated member avatar: shows an uploaded image if present, otherwise
 * initials on a deterministic hue background. Mirrors web's `Avatar` component.
 *
 * Role is never shown here — no ring, no badge. Use [RoleEyebrow] for role display.
 *
 * @param name Member display name — used for initials fallback
 * @param memberId Member ID — hashed to pick a deterministic background hue when not [isOwn]
 * @param avatarUrl The URL of the avatar image, or null to use the initials fallback
 * @param size The diameter of the circular avatar
 * @param isOwn When true, forces the copper "own user" background over the hue palette
 */
@Composable
fun Avatar(
    name: String,
    avatarUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    memberId: String = "",
    isOwn: Boolean = false,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    isLoading: Boolean = false
) {
    val baseModifier = modifier
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
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                placeholder = painterResource(R.drawable.img_fallback),
                fallback = painterResource(R.drawable.img_fallback),
                contentScale = ContentScale.Crop
            )
        } else {
            val isDark = isSystemInDarkTheme()
            val hues = if (isDark) avatarHuesDark else avatarHuesLight
            val backgroundColor = if (isOwn) brandPrimary else hues[hueIndex(memberId)]
            val textColor = if (isOwn) {
                brandOnPrimary
            } else if (isDark) {
                foregroundWarmPrimary
            } else {
                foregroundLightLabelVariant
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(color = backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(name),
                    color = textColor,
                    fontFamily = ebGaramond,
                    fontWeight = FontWeight.Medium,
                    fontSize = (size.value * 0.3f).sp
                )
            }
        }

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

private fun hueIndex(memberId: String): Int =
    if (memberId.isEmpty()) 0 else abs(memberId.hashCode()) % avatarHuesDark.size

private fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
        parts.isNotEmpty() -> parts.first().take(2).uppercase()
        else -> "?"
    }
}

@PreviewLightDark
@Composable
fun Preview_Avatar() = KluvsTheme {
    Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
        Avatar(
            name = "Jane Doe",
            memberId = "42",
            avatarUrl = null,
            size = 60.dp
        )
    }
}
