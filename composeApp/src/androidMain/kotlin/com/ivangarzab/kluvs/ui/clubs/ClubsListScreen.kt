package com.ivangarzab.kluvs.ui.clubs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.clubs.presentation.ClubDetailsState
import com.ivangarzab.kluvs.clubs.presentation.ClubListItem
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.presentation.state.ScreenState
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.components.ErrorScreen
import com.ivangarzab.kluvs.ui.components.RoleEyebrow

/**
 * Entry-point list of the member's clubs — mirrors web's `/clubs` page. Tapping a row
 * pushes the club detail screen ([ClubsScreenContent]).
 */
@Composable
fun ClubsListScreen(
    modifier: Modifier = Modifier,
    state: ClubDetailsState,
    screenState: ScreenState,
    onRetry: () -> Unit,
    onClubSelected: (String) -> Unit,
) {
    when (screenState) {
        is ScreenState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is ScreenState.Error -> ErrorScreen(
            modifier = modifier,
            message = screenState.message,
            onRetry = onRetry
        )

        is ScreenState.Empty -> ClubsListEmptyState(modifier = modifier)

        is ScreenState.Content -> Column(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                Text(
                    text = stringResource(R.string.your_eyebrow).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.clubs),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(state.availableClubs) { club ->
                    ClubListRow(
                        club = club,
                        onClick = { onClubSelected(club.id) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ClubListRow(
    club: ClubListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = club.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            club.role?.let { RoleEyebrow(role = it) }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ClubsListEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No clubs yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Join a club to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_ClubsListScreen() = KluvsTheme {
    ClubsListScreen(
        modifier = Modifier.fillMaxSize(),
        state = ClubDetailsState(
            availableClubs = listOf(
                ClubListItem(id = "1", name = "Weird Fiction Club", role = Role.OWNER),
                ClubListItem(id = "2", name = "Nonfiction Nook", role = Role.ADMIN),
                ClubListItem(id = "3", name = "Babel Book Bar", role = Role.MEMBER),
            )
        ),
        screenState = ScreenState.Content,
        onRetry = {},
        onClubSelected = {}
    )
}
