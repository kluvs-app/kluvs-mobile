package com.ivangarzab.kluvs.designsystem.components

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.theme.feature

@Composable
fun NoTabData(
    modifier: Modifier = Modifier,
    @StringRes text: Int
) {
    Text(
        modifier = modifier,
        text = stringResource(text),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        // Italic Title at empty-state scale — design-system/docs/typography.md's confirmed pattern
        // for empty-states (feature() bakes the italic, no separate fontStyle param needed).
        style = KluvsTheme.typography.title.medium.feature(),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun NoSectionData(
    modifier: Modifier = Modifier,
    @StringRes text: Int
) {
    Text(
        modifier = modifier,
        text = stringResource(text),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        // Smaller, section-scoped empty-state aside — Caption (not Body: Body has no feature
        // modifier in the design-system model; this is muted/secondary emphasis, Caption's job).
        style = KluvsTheme.typography.caption.feature(),
    )
}