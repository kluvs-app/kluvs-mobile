package com.ivangarzab.kluvs.ui.settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.ivangarzab.kluvs.designsystem.components.KluvsSnackbar
import com.ivangarzab.kluvs.designsystem.components.KluvsSnackbarVisuals
import com.ivangarzab.kluvs.designsystem.components.SnackbarVariant
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.settings.presentation.EditableProfile
import com.ivangarzab.kluvs.settings.presentation.SettingsDialog
import com.ivangarzab.kluvs.settings.presentation.SettingsSheet
import com.ivangarzab.kluvs.settings.presentation.SettingsState
import com.ivangarzab.kluvs.settings.presentation.SettingsViewModel
import com.ivangarzab.kluvs.designsystem.components.appbars.TopAppBar
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.components.LoadingScreen
import com.ivangarzab.kluvs.ui.components.ScreenEffectHost
import com.ivangarzab.kluvs.ui.utils.readAndCompressAvatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch(Dispatchers.IO) {
            readAndCompressAvatar(context.contentResolver, uri)
                .onSuccess(viewModel::uploadAvatar)
                .onFailure { viewModel.onAvatarPickFailed(it.message) }
        }
    }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    ScreenEffectHost(viewModel.screenEffects, snackbarHostState)

    LaunchedEffect(state.avatarError) {
        state.avatarError?.let { error ->
            snackbarHostState.showSnackbar(
                KluvsSnackbarVisuals(message = error, variant = SnackbarVariant.DANGER)
            )
            viewModel.clearAvatarError()
        }
    }

    LaunchedEffect(state.deleteAccountError) {
        state.deleteAccountError?.let { error ->
            snackbarHostState.showSnackbar(
                KluvsSnackbarVisuals(message = error, variant = SnackbarVariant.DANGER)
            )
            viewModel.clearDeleteAccountError()
        }
    }

    SettingsScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onNameChanged = viewModel::onNameChanged,
        onHandleChanged = viewModel::onHandleChanged,
        onSaveProfile = viewModel::onSaveProfile,
        onAvatarClick = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onNewPasswordFieldChange = viewModel::onNewPasswordFieldChanged,
        onConfirmPasswordFieldChange = viewModel::onConfirmPasswordFieldChanged,
        onSubmitChangePassword = viewModel::onSubmitChangePassword,
        onDeleteAccountConfirmed = viewModel::onDeleteAccountConfirmed,
        onOpenSheet = viewModel::onOpenSheet,
        onDismissSheet = viewModel::onDismissSheet,
        onOpenDialog = viewModel::onOpenDialog,
        onDismissDialog = viewModel::onDismissDialog,
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
    onAvatarClick: () -> Unit = {},
    onNewPasswordFieldChange: (String) -> Unit = {},
    onConfirmPasswordFieldChange: (String) -> Unit = {},
    onSubmitChangePassword: () -> Unit = {},
    onDeleteAccountConfirmed: () -> Unit = {},
    onOpenSheet: (SettingsSheet) -> Unit = {},
    onDismissSheet: () -> Unit = {},
    onOpenDialog: (SettingsDialog) -> Unit = {},
    onDismissDialog: () -> Unit = {},
) {
    val context = LocalContext.current

    // Deletion is irreversible once sent — swallow back presses so the user can't leave
    // Settings mid-request and end up back in the app with a deleted account.
    if (state.isDeletingAccount) {
        BackHandler {}
        LoadingScreen()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                header = stringResource(R.string.settings_title),
                onNavigateBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState, snackbar = { KluvsSnackbar(it) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            EditProfileSection(
                avatarUrl = state.profile?.avatarUrl,
                name = state.editedName,
                isUploadingAvatar = state.isUploadingAvatar,
                onAvatarClick = onAvatarClick,
                editedName = state.editedName,
                editedHandle = state.editedHandle,
                handleError = state.handleError,
                hasChanges = state.hasChanges,
                isSaving = state.isSaving,
                saveError = state.saveError,
                onNameChanged = onNameChanged,
                onHandleChanged = onHandleChanged,
                onSaveProfile = onSaveProfile,
            )

            AccountSection(
                onChangePasswordClick = { onOpenSheet(SettingsSheet.ChangePassword) },
                onDeleteAccountClick = { onOpenDialog(SettingsDialog.DeleteAccount) },
            )

            LegalSection(context = context)

            AboutSection(context = context, onCreditsClick = { onOpenSheet(SettingsSheet.Credits) })
        }

        when (state.activeSheet) {
            SettingsSheet.Credits -> CreditsSheet(onDismiss = onDismissSheet)
            SettingsSheet.ChangePassword -> ChangePasswordSheet(
                state = state,
                onNewPasswordFieldChange = onNewPasswordFieldChange,
                onConfirmPasswordFieldChange = onConfirmPasswordFieldChange,
                onSubmit = onSubmitChangePassword,
                onDismiss = onDismissSheet,
            )
            null -> Unit
        }

        if (state.activeDialog == SettingsDialog.DeleteAccount) {
            DeleteAccountConfirmationDialog(
                onConfirm = onDeleteAccountConfirmed,
                onDismiss = onDismissDialog,
            )
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_SettingsScreen() = KluvsTheme {
    SettingsScreenContent(
        state = SettingsState(
            isLoading = false,
            profile = EditableProfile(memberId = "1", name = "Alice", handle = "alice-reads"),
            editedName = "Alice",
            editedHandle = "alice-reads",
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
            profile = EditableProfile(memberId = "1", name = "Alice", handle = "alice-reads"),
            editedName = "Alice Updated",
            editedHandle = "alice-reads",
            hasChanges = true,
        )
    )
}
