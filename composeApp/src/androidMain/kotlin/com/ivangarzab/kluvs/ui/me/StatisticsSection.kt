package com.ivangarzab.kluvs.ui.me

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.member.presentation.UserStatistics
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * "Your Statistics" section: a 3-column stat strip (Clubs / Books / Since),
 * separated by hairline dividers. Mirrors web's ProfilePage stats row.
 */
@Composable
fun StatisticsSection(
    modifier: Modifier = Modifier,
    data: UserStatistics?,
    joinDate: String?,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatColumn(
            modifier = Modifier.weight(1f),
            value = data?.clubsCount?.takeIf { it > 0 }?.toString() ?: stringResource(R.string.na),
            label = stringResource(R.string.no_of_clubs)
        )
        StatDivider()
        StatColumn(
            modifier = Modifier.weight(1f),
            value = data?.booksRead?.takeIf { it > 0 }?.toString() ?: stringResource(R.string.na),
            label = stringResource(R.string.books_read)
        )
        StatDivider()
        StatColumn(
            modifier = Modifier.weight(1f),
            value = joinDate?.takeIf { it.isNotBlank() } ?: stringResource(R.string.na),
            label = stringResource(R.string.since)
        )
    }
}

@Composable
private fun StatColumn(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun StatDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@PreviewLightDark
@Composable
private fun Preview_StatisticsSection() = KluvsTheme {
    StatisticsSection(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        data = UserStatistics(clubsCount = 3, booksRead = 3),
        joinDate = "2025"
    )
}
