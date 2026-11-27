package com.ivangarzab.kluvs.ui.update

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.components.buttons.PrimaryButton
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.feature

// design-system tokens.json § component.illustration (size.mobile, gap) and
// § component.empty-state.body (max-width on mobile)
private val ILLUSTRATION_SIZE = 240.dp
private val ILLUSTRATION_GAP = 24.dp
private val BODY_MAX_WIDTH = 240.dp

/**
 * Hard block shown when the installed app is older than the backend supports
 * ([com.ivangarzab.kluvs.app.NavigationState.UpdateRequired]). The only way forward is the store,
 * which [onUpdate] opens as a shared platform effect.
 */
@Composable
fun ForceUpdateScreen(
    modifier: Modifier = Modifier,
    onUpdate: () -> Unit
) {
    // Scrolls so the only way forward stays reachable on short screens (landscape). When the
    // content fits, the Column wraps it and the Box centers it.
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp, vertical = ILLUSTRATION_GAP),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Used as-is on light and dark; must sit directly on the screen background
            // (design-system/docs/illustrations.md)
            Image(
                modifier = Modifier.size(ILLUSTRATION_SIZE),
                painter = painterResource(R.drawable.illustration_force_update),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.height(ILLUSTRATION_GAP))
            Text(
                text = stringResource(R.string.force_update_heading),
                style = KluvsTheme.typography.title.large.feature(),
                color = KluvsTheme.colors.labelVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                modifier = Modifier.widthIn(max = BODY_MAX_WIDTH),
                text = stringResource(R.string.force_update_body),
                style = KluvsTheme.typography.body.medium,
                color = KluvsTheme.colors.contentMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.force_update_button),
                onClick = onUpdate
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview_ForceUpdateScreen() = KluvsTheme {
    ForceUpdateScreen(
        modifier = Modifier.background(color = KluvsTheme.colors.background),
        onUpdate = { Bark.d("Preview: Update tapped") }
    )
}
