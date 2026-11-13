package com.ivangarzab.kluvs.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.BuildConfig
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

// FAQ question ids are stable anchors (e.g. "#club-session-discussion"), so a future contextual
// link can append one to this single constant.
private const val FAQ_URL = "https://kluvs.com/faq"

@Composable
fun AboutSection(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    onCreditsClick: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = stringResource(R.string.about_title).uppercase(),
            style = KluvsTheme.typography.eyebrow,
            color = KluvsTheme.colors.contentMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LegalRow(
            label = stringResource(R.string.help_faq),
            onClick = { openCustomTab(context, FAQ_URL) }
        )

        HorizontalDivider(color = KluvsTheme.colors.divider)

        LegalRow(
            label = stringResource(R.string.credits_title),
            onClick = onCreditsClick
        )

        HorizontalDivider(color = KluvsTheme.colors.divider)

        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = stringResource(R.string.version_x, BuildConfig.VERSION_NAME),
                color = KluvsTheme.colors.contentMuted,
                style = KluvsTheme.typography.finePrint,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_AboutSection() = KluvsTheme {
    AboutSection()
}
