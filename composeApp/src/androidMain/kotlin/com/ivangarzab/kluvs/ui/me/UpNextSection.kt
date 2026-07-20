package com.ivangarzab.kluvs.ui.me

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.member.presentation.UpNextItem
import com.ivangarzab.kluvs.theme.KluvsTheme

/**
 * "Up Next" section: the nearest upcoming discussion across all of the
 * member's clubs. Flat section matching the rest of the Me screen — no card
 * fill/border. Read-only; attendance/RSVP is a separate ticket. Renders
 * nothing when there's no upcoming discussion.
 */
@Composable
fun UpNextSection(
    modifier: Modifier = Modifier,
    upNext: UpNextItem?,
) {
    if (upNext == null) return

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.up_next_eyebrow).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = upNext.date,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Text(
            text = upNext.title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic
        )

        Text(
            text = listOfNotNull(upNext.clubName, upNext.location).joinToString(" — "),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview_UpNextSection() = KluvsTheme {
    UpNextSection(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        upNext = UpNextItem(
            title = "End-of-Year Check-in",
            clubName = "Showcase Kluv",
            location = "Online",
            date = "December 31, 2026"
        )
    )
}
