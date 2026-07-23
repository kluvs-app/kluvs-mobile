package com.ivangarzab.kluvs.ui.join

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.join.presentation.JoinState
import com.ivangarzab.kluvs.join.presentation.JoinViewModel
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Join-by-invite-token screen. Reachable today only via manual token entry — tapping a raw
 * invite URL does not yet open this screen (Android App Links deep linking is a follow-up).
 *
 * The preview shows only the club name — [com.ivangarzab.kluvs.model.ClubPreview] has no
 * avatar/member-count yet (also a follow-up, needs a backend spec change).
 */
@Composable
fun JoinScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToClub: (clubId: String) -> Unit,
    onNeedsSignIn: (token: String) -> Unit,
    viewModel: JoinViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.joinedClubId) {
        state.joinedClubId?.let { clubId ->
            onNavigateToClub(clubId)
            viewModel.onConsumeJoinedClubId()
        }
    }

    LaunchedEffect(state.needsSignIn) {
        if (state.needsSignIn) {
            onNeedsSignIn(state.tokenInput.trim())
            viewModel.onConsumeNeedsSignIn()
        }
    }

    JoinScreenContent(
        modifier = modifier,
        state = state,
        onNavigateBack = onNavigateBack,
        onTokenChanged = viewModel::onTokenChanged,
        onPreviewInvite = viewModel::previewInvite,
        onJoinClicked = viewModel::onJoinClicked
    )
}

@Composable
private fun JoinScreenContent(
    modifier: Modifier = Modifier,
    state: JoinState,
    onNavigateBack: () -> Unit = {},
    onTokenChanged: (String) -> Unit = {},
    onPreviewInvite: () -> Unit = {},
    onJoinClicked: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back)
            )
        }

        Text(
            text = "Join a club",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = state.tokenInput,
            onValueChange = onTokenChanged,
            label = { Text("Invite code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = onPreviewInvite,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.tokenInput.isNotBlank() && !state.isLoadingPreview
        ) {
            Text(
                text = "Preview",
                color = MaterialTheme.colorScheme.background
            )
        }

        if (state.isLoadingPreview) {
            CircularProgressIndicator()
        }

        state.previewError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        state.preview?.let { preview ->
            Text(
                text = preview.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            state.joinError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onJoinClicked,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isJoining
            ) {
                Text(
                    text = if (state.isJoining) "Joining..." else "Join",
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_JoinScreen() = KluvsTheme {
    JoinScreenContent(
        state = JoinState(
            tokenInput = "abc123",
            preview = com.ivangarzab.kluvs.model.ClubPreview(id = "club-1", name = "Weird Fiction Club")
        )
    )
}
