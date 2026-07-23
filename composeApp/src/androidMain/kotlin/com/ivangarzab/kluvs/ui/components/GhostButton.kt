package com.ivangarzab.kluvs.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Low-emphasis outlined action button — mirrors web's `GhostButton` (design-system
 * "Secondary / Outlined" style, see design-system/docs/buttons.md). Used for
 * supporting actions like "Join this Read" / "Opt out" or "Update" progress.
 */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@PreviewLightDark
@Composable
fun Preview_GhostButton() = KluvsTheme {
    GhostButton(text = "Join this Read", onClick = {})
}
