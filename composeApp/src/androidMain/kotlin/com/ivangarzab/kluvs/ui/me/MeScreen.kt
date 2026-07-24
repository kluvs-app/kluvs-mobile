package com.ivangarzab.kluvs.ui.me

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.member.presentation.MeState
import com.ivangarzab.kluvs.member.presentation.MeViewModel
import com.ivangarzab.kluvs.member.presentation.ShelfItem
import com.ivangarzab.kluvs.member.presentation.UpNextItem
import com.ivangarzab.kluvs.member.presentation.UserProfile
import com.ivangarzab.kluvs.member.presentation.UserStatistics
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.state.ScreenState
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.components.ErrorScreen
import com.ivangarzab.kluvs.designsystem.components.icons.IconType
import com.ivangarzab.kluvs.designsystem.components.icons.Icon
import com.ivangarzab.kluvs.ui.components.LoadingScreen
import com.ivangarzab.kluvs.designsystem.components.avatars.Avatar
import com.ivangarzab.kluvs.designsystem.components.ProgressTrackingMode
import com.ivangarzab.kluvs.designsystem.components.ReadingProgressBottomSheet
import com.ivangarzab.kluvs.ui.utils.compressImage
import org.koin.compose.viewmodel.koinViewModel

/** Translation at the boundary into the hollow [ReadingProgressBottomSheet] — see its call site below. */
private fun ProgressType.toTrackingMode(): ProgressTrackingMode = when (this) {
    ProgressType.PAGE -> ProgressTrackingMode.PAGE
    ProgressType.PERCENT -> ProgressTrackingMode.PERCENT
}

private fun ProgressTrackingMode.toDomain(): ProgressType = when (this) {
    ProgressTrackingMode.PAGE -> ProgressType.PAGE
    ProgressTrackingMode.PERCENT -> ProgressType.PERCENT
}

@Composable
fun MeScreen(
    modifier: Modifier = Modifier,
    userId: String,
    onNavigateToSettings: () -> Unit = {},
    viewModel: MeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var editingShelfItem by remember { mutableStateOf<ShelfItem?>(null) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val bytes = compressImage(inputStream.readBytes())
                viewModel.uploadAvatar(bytes)
            }
        }
    }

    LaunchedEffect(userId) {
        viewModel.loadUserData(userId)
    }

    // Show snackbar for errors
    LaunchedEffect(state.snackbarError) {
        state.snackbarError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearAvatarError()
        }
    }

    if (state.showLogoutConfirmation) {
        LogoutConfirmationDialog(
            onConfirm = viewModel::onSignOutDialogConfirmed,
            onDismiss = viewModel::onSignOutDialogDismissed
        )
    }

    Column(modifier = modifier.background(color = MaterialTheme.colorScheme.background)) {
        MeTopBar(onReadingLogClick = viewModel::onReadingLogClicked)

        Box(modifier = Modifier.weight(1f)) {
            MeScreenContent(
                modifier = Modifier,
                state = state,
                onRetry = viewModel::refresh,
                onSettingsClick = onNavigateToSettings,
                onHelpClick = { /* TODO() */ },
                onSignOutClick = viewModel::onSignOutClicked,
                onAvatarClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onUpdateProgress = { sessionId ->
                    editingShelfItem = state.shelf.find { it.sessionId == sessionId }
                }
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            editingShelfItem?.let { item ->
                // ReadingProgressBottomSheet is hollow (design-system primitives migration) — it
                // reports back in ProgressTrackingMode, translated to the domain ProgressType here.
                ReadingProgressBottomSheet(
                    bookTitle = item.bookTitle,
                    pageCount = item.bookPageCount,
                    initialType = (item.ownProgress?.type ?: ProgressType.PAGE).toTrackingMode(),
                    initialCurrentPage = item.ownProgress?.currentPage,
                    initialPercentComplete = item.ownProgress?.percentComplete,
                    initialMarkFinished = item.ownProgress?.isCompleted ?: false,
                    onSave = { type, currentPage, percentComplete, markFinished ->
                        viewModel.onSaveProgress(item.sessionId, type.toDomain(), currentPage, percentComplete, markFinished)
                        editingShelfItem = null
                    },
                    onDismiss = { editingShelfItem = null }
                )
            }

            if (state.showReadingLog) {
                ReadingLogBottomSheet(
                    log = state.readingLog,
                    isLoading = state.isReadingLogLoading,
                    onDismiss = viewModel::onReadingLogDismissed
                )
            }
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.logout_confirmation_title)) },
        text = { Text(stringResource(R.string.logout_confirmation_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.no))
            }
        }
    )
}

@Composable
fun MeScreenContent(
    modifier: Modifier = Modifier,
    state: MeState,
    onRetry: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onAvatarClick: () -> Unit = {},
    onUpdateProgress: (sessionId: String) -> Unit = {},
) {
    val screenState = when {
        state.isLoading -> ScreenState.Loading
        state.error != null -> ScreenState.Error(state.error!!)
        else -> ScreenState.Content
    }

    AnimatedContent(
        targetState = screenState,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "MeScreenTransition"
    ) { targetState ->
        when (targetState) {
            is ScreenState.Loading -> LoadingScreen()
            is ScreenState.Error -> ErrorScreen(
                message = targetState.message,
                onRetry = onRetry
            )
            is ScreenState.Empty,
            is ScreenState.Content -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileSection(
                        avatarUrl = state.profile?.avatarUrl,
                        name = state.profile?.name ?: "",
                        handle = state.profile?.handle ?: "",
                        isUploadingAvatar = state.isUploadingAvatar,
                        onAvatarClick = onAvatarClick
                    )

                    Divider()

                    StatisticsSection(
                        modifier = Modifier.fillMaxWidth(),
                        data = state.statistics,
                        joinDate = state.profile?.joinDate
                    )

                    Divider()

                    UpNextSection(
                        modifier = Modifier.fillMaxWidth(),
                        upNext = state.upNext
                    )

                    if (state.upNext != null) {
                        Divider()
                    }

                    ShelfSection(
                        modifier = Modifier.fillMaxWidth(),
                        shelf = state.shelf,
                        onUpdateProgress = onUpdateProgress
                    )

                    Divider()

                    FooterSection(
                        modifier = Modifier.fillMaxWidth(),
                        onSettingsClick = onSettingsClick,
                        onHelpClick = onHelpClick,
                        onSignOutClick = onSignOutClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    modifier: Modifier = Modifier,
    avatarUrl: String?,
    name: String,
    handle: String,
    isUploadingAvatar: Boolean = false,
    onAvatarClick: () -> Unit = {},
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with edit button overlay
        Box {
            Avatar(
                name = name,
                avatarUrl = avatarUrl,
                size = 60.dp,
                isOwn = true,
                contentDescription = stringResource(R.string.profile_picture),
                onClick = onAvatarClick,
                isLoading = isUploadingAvatar
            )

            // Edit icon overlay
            FloatingActionButton(
                onClick = onAvatarClick,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    type = IconType.Edit,
                    contentDescription = stringResource(R.string.edit_profile_picture),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.padding(8.dp))

        Column {
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = handle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FooterSection(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FooterItem(
            label = stringResource(R.string.settings),
            icon = IconType.Settings,
            onClick = onSettingsClick
        )

        Divider()

//        FooterItem(
//            label = stringResource(R.string.help_and_support),
//            icon = IconType.Help,
//            onClick = onHelpClick
//        )
//
//        Divider()

        FooterItem(
            label = stringResource(R.string.sign_out),
            labelColor = MaterialTheme.colorScheme.error,
            icon = IconType.SignOut,
            iconColor = MaterialTheme.colorScheme.error,
            onClick = onSignOutClick
        )

    }
}

@Composable
private fun FooterItem(
    modifier: Modifier = Modifier,
    label: String,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    icon: IconType,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            type = icon,
            contentDescription = null,
            tint = iconColor
        )
        Spacer(Modifier.padding(horizontal = 4.dp))
        Text(
            text = label,
            color = labelColor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun Divider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    HorizontalDivider(modifier = modifier, color = color)
}

@PreviewLightDark
@Composable
fun Preview_MeScreen() = KluvsTheme {
    Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
        MeTopBar()

        MeScreenContent(
            state = MeState(
                isLoading = false,
                profile = UserProfile(
                    memberId = "0",
                    name = "Quill",
                    handle = "@quill-bot",
                    joinDate = "2025",
                    avatarUrl = null
                ),
                statistics = UserStatistics(clubsCount = 6, booksRead = 2),
                upNext = UpNextItem(
                    title = "End-of-Year Check-in",
                    clubName = "Showcase Kluv",
                    location = "Online",
                    date = "January 1, 2027"
                ),
                shelf = listOf(
                    ShelfItem(
                        sessionId = "s0",
                        bookId = "b0",
                        bookTitle = "1984",
                        bookAuthor = "George Orwell",
                        bookCoverUrl = null,
                        bookPageCount = null,
                        clubId = "c0",
                        clubName = "Quill's Club",
                        nextDiscussionDate = "Tomorrow",
                        ownProgress = null
                    )
                )
            ),
            onRetry = { },
            onSettingsClick = { },
            onHelpClick = { },
            onSignOutClick = { },
        )
    }
}