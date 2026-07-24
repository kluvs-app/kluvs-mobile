package com.ivangarzab.kluvs.designsystem.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.designsystem.components.icons.Icon
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * The single most important action on a screen (design-system "Primary", see
 * design-system/docs/buttons.md). Copper fill, white text, 12dp radius.
 *
 * Deliberately does not touch content color: M3's own `onPrimary` (white) already has
 * correct, DS-canonical contrast against the copper container in both themes. Several call
 * sites across the app used to hardcode content color to `colorScheme.background` instead,
 * which is near-black in dark theme and unreadable on the copper container — letting M3's
 * default flow through here fixes that as call sites migrate to this primitive.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: IconType? = null,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
    ) {
        if (icon != null) {
            Icon(type = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(8.dp))
        }
        Text(text = text, style = KluvsTheme.typography.label)
    }
}

@PreviewLightDark
@Composable
private fun Preview_PrimaryButton() = KluvsTheme {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.size(240.dp, 160.dp),
    ) {
        PrimaryButton(text = "Continue", onClick = {}, modifier = Modifier.fillMaxWidth())
        PrimaryButton(text = "Create Session", onClick = {}, icon = IconType.Add, modifier = Modifier.fillMaxWidth())
        PrimaryButton(text = "Continue", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
    }
}
