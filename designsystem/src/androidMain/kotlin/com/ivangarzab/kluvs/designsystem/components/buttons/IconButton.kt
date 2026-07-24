package com.ivangarzab.kluvs.designsystem.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.icons.Icon
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * A tappable [Icon] — nothing more. Wraps `androidx.compose.material3.IconButton` purely for
 * its 48dp minimum touch target and ripple, not for any visual chrome of its own; there's no
 * container, border, or background here, unlike the rest of the button family.
 */
@Composable
fun IconButton(
    type: IconType,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(type = type, contentDescription = contentDescription, tint = tint)
    }
}

@PreviewLightDark
@Composable
private fun Preview_IconButton() = KluvsTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(type = IconType.ArrowBack, contentDescription = "Back", onClick = {})
        IconButton(type = IconType.MoreVert, contentDescription = "More options", onClick = {})
        IconButton(type = IconType.MoreVert, contentDescription = "More options", onClick = {}, enabled = false)
    }
}
