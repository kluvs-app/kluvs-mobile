package com.ivangarzab.kluvs.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.settings.presentation.EditableProfile
import com.ivangarzab.kluvs.settings.presentation.SettingsState
import com.ivangarzab.kluvs.settings.presentation.SettingsViewModel
import com.ivangarzab.kluvs.theme.KluvsTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.save_success))
            viewModel.onDismissSaveSuccess()
        }
    }

    SettingsScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onNameChanged = viewModel::onNameChanged,
        onHandleChanged = viewModel::onHandleChanged,
        onSaveProfile = viewModel::onSaveProfile,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    state: SettingsState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onNameChanged: (String) -> Unit = {},
    onHandleChanged: (String) -> Unit = {},
    onSaveProfile: () -> Unit = {},
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            EditProfileSection(
                editedName = state.editedName,
                editedHandle = state.editedHandle,
                hasChanges = state.hasChanges,
                isSaving = state.isSaving,
                saveError = state.saveError,
                onNameChanged = onNameChanged,
                onHandleChanged = onHandleChanged,
                onSaveProfile = onSaveProfile,
            )

            HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

            LegalSection(context = context)

//            HorizontalDivider()

            AboutSection()
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_SettingsScreen() = KluvsTheme {
    SettingsScreenContent(
        state = SettingsState(
            isLoading = false,
            profile = EditableProfile(memberId = "1", name = "Alice", handle = "alice_reads"),
            editedName = "Alice",
            editedHandle = "alice_reads",
            hasChanges = false,
        )
    )
}

@PreviewLightDark
@Composable
fun Preview_SettingsScreen_WithChanges() = KluvsTheme {
    SettingsScreenContent(
        state = SettingsState(
            isLoading = false,
            profile = EditableProfile(memberId = "1", name = "Alice", handle = "alice_reads"),
            editedName = "Alice Updated",
            editedHandle = "alice_reads",
            hasChanges = true,
        )
    )
}
