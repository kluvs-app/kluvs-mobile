package com.ivangarzab.kluvs.ui.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.components.EmptyState
import com.ivangarzab.kluvs.designsystem.components.buttons.PrimaryButton
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.ivangarzab.kluvs"

/**
 * Hard block shown when the installed app is older than the backend supports
 * ([com.ivangarzab.kluvs.app.NavigationState.UpdateRequired]). The only way forward is the store.
 */
@Composable
fun ForceUpdateScreen(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    ForceUpdateContent(
        modifier = modifier,
        onUpdate = {
            Bark.i("Opening Play Store listing to update")
            uriHandler.openUri(PLAY_STORE_URL)
        }
    )
}

@Composable
private fun ForceUpdateContent(
    modifier: Modifier = Modifier,
    onUpdate: () -> Unit
) {
    EmptyState(
        modifier = modifier.fillMaxSize(),
        heading = stringResource(R.string.force_update_heading),
        body = stringResource(R.string.force_update_body),
        action = {
            PrimaryButton(text = stringResource(R.string.force_update_button), onClick = onUpdate)
        },
    )
}

@PreviewLightDark
@Composable
private fun Preview_ForceUpdateScreen() = KluvsTheme {
    ForceUpdateContent(
        modifier = Modifier.background(color = KluvsTheme.colors.background),
        onUpdate = { }
    )
}
