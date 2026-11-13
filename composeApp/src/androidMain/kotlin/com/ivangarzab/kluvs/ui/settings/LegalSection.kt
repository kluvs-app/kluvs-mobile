package com.ivangarzab.kluvs.ui.settings

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import androidx.core.net.toUri

private const val PRIVACY_POLICY_URL = "https://kluvs.com/privacy"
private const val TERMS_OF_USE_URL = "https://kluvs.com/terms"
private const val DATA_DELETE_URL = "https://kluvs.com/delete-account"

@Composable
fun LegalSection(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = stringResource(R.string.legal_title).uppercase(),
            style = KluvsTheme.typography.eyebrow,
            color = KluvsTheme.colors.contentMuted
        )

        Spacer(modifier = Modifier.padding(vertical = 8.dp))

        LegalRow(
            label = stringResource(R.string.privacy_policy),
            onClick = { openCustomTab(context, PRIVACY_POLICY_URL) }
        )

        HorizontalDivider(color = KluvsTheme.colors.divider)

        LegalRow(
            label = stringResource(R.string.terms_of_use),
            onClick = { openCustomTab(context, TERMS_OF_USE_URL) }
        )

        HorizontalDivider(color = KluvsTheme.colors.divider)

        LegalRow(
            label = stringResource(R.string.data_deletion),
            onClick = { openCustomTab(context, DATA_DELETE_URL) }
        )

        HorizontalDivider(color = KluvsTheme.colors.divider)
    }
}

@Composable
internal fun LegalRow(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = KluvsTheme.typography.body.large,
            color = KluvsTheme.colors.accent,
        )
    }
}

internal fun openCustomTab(context: Context, url: String) {
    CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
        .launchUrl(context, url.toUri())
}

@PreviewLightDark
@Composable
fun Preview_LegalSection() = KluvsTheme {
    LegalSection()
}
