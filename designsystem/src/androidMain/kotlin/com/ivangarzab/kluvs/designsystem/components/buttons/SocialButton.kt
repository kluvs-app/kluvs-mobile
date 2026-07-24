package com.ivangarzab.kluvs.designsystem.components.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.components.icons.toDrawableRes
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.providerDiscordBg
import com.ivangarzab.kluvs.designsystem.theme.providerDiscordText
import com.ivangarzab.kluvs.designsystem.theme.providerGoogleBg
import com.ivangarzab.kluvs.designsystem.theme.providerGoogleText

/**
 * Fixed brand-branded button for OAuth sign-in (design-system "Social / OAuth" — see
 * design-system/docs/buttons.md). Colors are per-provider and passed in by the caller, not
 * themed — Discord/Google/Apple each have a fixed brand fill regardless of light/dark theme.
 */
@Composable
fun SocialButton(
    text: String,
    icon: IconType,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 40.dp,
    iconSize: Dp = 20.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(color = backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(icon.toDrawableRes()),
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            style = KluvsTheme.typography.label
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview_SocialButton() = KluvsTheme {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SocialButton(
            text = "Continue with Discord",
            icon = IconType.Discord,
            backgroundColor = providerDiscordBg,
            textColor = providerDiscordText,
            onClick = {},
        )
        SocialButton(
            text = "Continue with Google",
            icon = IconType.Google,
            backgroundColor = providerGoogleBg,
            textColor = providerGoogleText,
            onClick = {},
        )
        SocialButton(
            text = "Continue with Google",
            icon = IconType.Google,
            backgroundColor = providerGoogleBg,
            textColor = providerGoogleText,
            onClick = {},
            enabled = false,
        )
    }
}
