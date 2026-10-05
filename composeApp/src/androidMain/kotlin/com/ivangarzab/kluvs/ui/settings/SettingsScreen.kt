package com.ivangarzab.kluvs.ui.settings

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
import com.ivangarzab.kluvs.settings.presentation.SettingsState
import com.ivangarzab.kluvs.settings.presentation.SettingsViewModel
import com.ivangarzab.kluvs.designsystem.components.appbars.TopAppBar
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
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

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.save_success))
            viewModel.onDismissSaveSuccess()
        }
    }

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

    LaunchedEffect(state.changePasswordSuccess) {
        if (state.changePasswordSuccess) {
            // Close the sheet before awaiting the snackbar — showSnackbar suspends until it
            // auto-dismisses, so doing this after would leave the sheet open the whole time.
            // isChangePasswordSheetOpen isn't this effect's key, so clearing it here doesn't
            // cancel the coroutine — but changePasswordSuccess IS the key, so it must stay
            // untouched until after showSnackbar returns, or the recomposition it triggers
            // would cancel the in-flight snackbar before it's shown.
            viewModel.onChangePasswordSheetDismissed()
            snackbarHostState.showSnackbar(context.getString(R.string.password_updated))
            viewModel.onDismissChangePasswordSuccess()
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
        onChangePasswordClick = viewModel::onChangePasswordSheetOpened,
        onChangePasswordSheetDismiss = viewModel::onChangePasswordSheetDismissed,
        onNewPasswordFieldChange = viewModel::onNewPasswordFieldChanged,
        onConfirmPasswordFieldChange = viewModel::onConfirmPasswordFieldChanged,
        onSubmitChangePassword = viewModel::onSubmitChangePassword,
        onDeleteAccountClick = viewModel::onDeleteAccountClicked,
        onDeleteAccountDialogDismiss = viewModel::onDeleteAccountDialogDismissed,
        onDeleteAccountConfirmed = viewModel::onDeleteAccountConfirmed,
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
    onChangePasswordClick: () -> Unit = {},
    onChangePasswordSheetDismiss: () -> Unit = {},
    onNewPasswordFieldChange: (String) -> Unit = {},
    onConfirmPasswordFieldChange: (String) -> Unit = {},
    onSubmitChangePassword: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onDeleteAccountDialogDismiss: () -> Unit = {},
    onDeleteAccountConfirmed: () -> Unit = {},
) {
    val context = LocalContext.current

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
                onChangePasswordClick = onChangePasswordClick,
                onDeleteAccountClick = onDeleteAccountClick,
            )

            LegalSection(context = context)

            AboutSection()
        }

        if (state.isChangePasswordSheetOpen) {
            ChangePasswordSheet(
                state = state,
                onNewPasswordFieldChange = onNewPasswordFieldChange,
                onConfirmPasswordFieldChange = onConfirmPasswordFieldChange,
                onSubmit = onSubmitChangePassword,
                onDismiss = onChangePasswordSheetDismiss,
            )
        }

        if (state.isDeleteAccountDialogOpen) {
            DeleteAccountConfirmationDialog(
                onConfirm = onDeleteAccountConfirmed,
                onDismiss = onDeleteAccountDialogDismiss,
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
