package com.ivangarzab.kluvs.designsystem.components.pills

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.Icon
import com.ivangarzab.kluvs.designsystem.components.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Circular icon toggle (design-system "Pill" family), e.g. a like/favorite button — binary
 * checked/unchecked, not a multi-option selector, so unlike [com.ivangarzab.kluvs.designsystem.components.controls.ToggleControl]
 * there's no generic option list here. Copper border/tint when [checked], grey otherwise.
 * Extracted from a private, single-caller `LikeToggle` in `BookDetailActions`.
 */
@Composable
fun TogglePill(
    checked: Boolean,
    onToggle: () -> Unit,
    iconChecked: IconType,
    iconUnchecked: IconType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .size(36.dp)
            .border(1.dp, borderColor, CircleShape)
            .clickable(enabled = enabled, onClick = onToggle)
            .alpha(if (enabled) 1f else 0.4f),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            type = if (checked) iconChecked else iconUnchecked,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview_TogglePill() = KluvsTheme {
    var checked by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TogglePill(
            checked = checked,
            onToggle = { checked = !checked },
            iconChecked = IconType.Favorite,
            iconUnchecked = IconType.FavoriteOutline,
            contentDescription = "Like",
        )
        TogglePill(
            checked = true,
            onToggle = {},
            iconChecked = IconType.Favorite,
            iconUnchecked = IconType.FavoriteOutline,
            contentDescription = "Like",
            enabled = false,
        )
    }
}
