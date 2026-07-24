package com.ivangarzab.kluvs.designsystem.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Low-emphasis action with no container (design-system "Ghost / Text" — see
 * design-system/docs/buttons.md), e.g. "Forgot password?" or "Cancel." Copper or grey text,
 * selected via [emphasized].
 */
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasized: Boolean = false,
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (emphasized) KluvsTheme.colors.accent else KluvsTheme.colors.contentMuted
        ),
    ) {
        Text(text = text, style = KluvsTheme.typography.label)
    }
}

@PreviewLightDark
@Composable
private fun Preview_TextButton() = KluvsTheme {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(text = "Cancel", onClick = {})
        TextButton(text = "Forgot password?", onClick = {}, emphasized = true)
        TextButton(text = "Cancel", onClick = {}, enabled = false)
    }
}
