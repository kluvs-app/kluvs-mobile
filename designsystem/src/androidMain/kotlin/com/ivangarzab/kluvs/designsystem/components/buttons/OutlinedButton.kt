package com.ivangarzab.kluvs.designsystem.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Supporting/secondary action, "muted" emphasis (design-system "Secondary / Outlined",
 * grey variant — see design-system/docs/buttons.md). Named to shadow
 * `androidx.compose.material3.OutlinedButton` deliberately (matches [com.ivangarzab.kluvs.designsystem.components.Icon]'s
 * convention). Use [SecondaryButton] for the copper/active variant of this same role.
 */
@Composable
fun OutlinedButton(
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
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
    ) {
        Text(text = text, style = KluvsTheme.typography.label)
    }
}

@PreviewLightDark
@Composable
private fun Preview_OutlinedButton() = KluvsTheme {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(text = "Join this Read", onClick = {})
        OutlinedButton(text = "Join this Read", onClick = {}, enabled = false)
    }
}
