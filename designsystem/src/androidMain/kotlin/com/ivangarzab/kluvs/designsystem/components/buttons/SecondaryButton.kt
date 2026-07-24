package com.ivangarzab.kluvs.designsystem.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Supporting/secondary action, "active" emphasis (design-system "Secondary / Outlined",
 * copper variant — see design-system/docs/buttons.md). Outlined copper border and text,
 * 12dp radius. Use [OutlinedButton] for the grey/muted variant of this same role.
 */
@Composable
fun SecondaryButton(
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
        border = androidx.compose.foundation.BorderStroke(1.dp, KluvsTheme.colors.accent),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = KluvsTheme.colors.accent),
    ) {
        Text(text = text, style = KluvsTheme.typography.label)
    }
}

@PreviewLightDark
@Composable
private fun Preview_SecondaryButton() = KluvsTheme {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(text = "Change Role", onClick = {}, modifier = Modifier.fillMaxWidth())
        SecondaryButton(text = "Change Role", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
    }
}
