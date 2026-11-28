package com.ivangarzab.kluvs.ui.me

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.ivangarzab.kluvs.designsystem.components.KluvsSnackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.member.presentation.MeDialog
import com.ivangarzab.kluvs.member.presentation.MeSheet
import com.ivangarzab.kluvs.member.presentation.MeState
import com.ivangarzab.kluvs.member.presentation.MeViewModel
import com.ivangarzab.kluvs.member.presentation.ShelfItem
import com.ivangarzab.kluvs.member.presentation.UpNextItem
import com.ivangarzab.kluvs.member.presentation.UserProfile
import com.ivangarzab.kluvs.member.presentation.UserStatistics
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.presentation.state.ScreenState
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.components.EmptyState
import com.ivangarzab.kluvs.designsystem.components.ErrorScreen
import com.ivangarzab.kluvs.designsystem.components.appbars.TopAppBar
import com.ivangarzab.kluvs.designsystem.components.loading.PullToRefreshContainer
import com.ivangarzab.kluvs.designsystem.components.menus.ActionMenu
import com.ivangarzab.kluvs.designsystem.components.menus.ActionMenuItem
import com.ivangarzab.kluvs.designsystem.components.avatars.Avatar
import com.ivangarzab.kluvs.designsystem.components.ProgressTrackingMode
import com.ivangarzab.kluvs.designsystem.components.ReadingProgressBottomSheet
import com.ivangarzab.kluvs.designsystem.components.modals.ConfirmationDialog
import com.ivangarzab.kluvs.ui.components.ScreenEffectHost
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.loadUserData(userId)
    }

    ScreenEffectHost(viewModel.screenEffects, snackbarHostState)

    if (state.activeDialog == MeDialog.SignOut) {
        LogoutConfirmationDialog(
            onConfirm = viewModel::onSignOutDialogConfirmed,
            onDismiss = viewModel::onDismissDialog
        )
    }

    Column(modifier = modifier.background(color = KluvsTheme.colors.background)) {
        TopAppBar(
            header = stringResource(R.string.me),
            action = {
                ActionMenu(
                    items = listOf(
                        ActionMenuItem(label = stringResource(R.string.reading_log), onClick = { viewModel.onOpenSheet(MeSheet.ReadingLog) }),
                        ActionMenuItem(label = stringResource(R.string.settings), onClick = onNavigateToSettings),
                        ActionMenuItem(label = stringResource(R.string.sign_out), onClick = { viewModel.onOpenDialog(MeDialog.SignOut) }, isDestructive = true),
                    ),
                    contentDescription = stringResource(R.string.profile_menu)
                )
            },
        )

        Box(modifier = Modifier.weight(1f)) {
            MeScreenContent(
                modifier = Modifier,
                state = state,
                onRetry = viewModel::refresh,
                onRefresh = { viewModel.refresh(forceRefresh = true) },
                onUpdateProgress = { sessionId -> viewModel.onOpenSheet(MeSheet.UpdateProgress(sessionId)) },
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
                snackbar = { KluvsSnackbar(it) }
            )

            when (val sheet = state.activeSheet) {
                is MeSheet.UpdateProgress -> state.shelf.find { it.sessionId == sheet.sessionId }?.let { item ->
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
                        },
                        onDismiss = viewModel::onDismissSheet
                    )
                }

                MeSheet.ReadingLog -> ReadingLogBottomSheet(
                    log = state.readingLog,
                    isLoading = state.isReadingLogLoading,
                    onDismiss = viewModel::onDismissSheet
                )

                null -> Unit
            }
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(R.string.logout_confirmation_title),
        message = stringResource(R.string.logout_confirmation_message),
        confirmLabel = stringResource(R.string.sign_out),
        isDestructive = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun MeScreenContent(
    modifier: Modifier = Modifier,
    state: MeState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit = onRetry,
    onUpdateProgress: (sessionId: String) -> Unit = {},
) {
    // state.statistics.clubsCount (from GetUserStatisticsUseCase, already the source of the
    // "4 CLUBS" stat shown above) is the real "no clubs" signal — null means stats haven't
    // loaded or failed to load, not that the count is zero, so it deliberately does NOT count
    // as empty here.
    val hasNothingToShow = state.statistics?.clubsCount == 0

    val screenState = when {
        state.profile != null -> ScreenState.Content
        state.isLoading -> ScreenState.Loading
        state.error != null -> ScreenState.Error(state.error!!)
        else -> ScreenState.Empty
    }

    AnimatedContent(
        targetState = screenState,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "MeScreenTransition"
    ) { targetState ->
        when (targetState) {
            is ScreenState.Loading -> MeScreenSkeleton(modifier = modifier.fillMaxSize())
            is ScreenState.Error -> ErrorScreen(
                message = targetState.message,
                onRetry = onRetry
            )
            is ScreenState.Empty,
            is ScreenState.Content -> {
                PullToRefreshContainer(
                    isLoading = state.isLoading,
                    onRefresh = onRefresh,
                    modifier = modifier.fillMaxSize(),
                ) {
                    // hasNothingToShow skips verticalScroll entirely (rather than just adding
                    // fillMaxSize/weight inside it) — a scrollable Column gives its children
                    // unbounded height, so the EmptyState below could never actually fill the
                    // remaining viewport space if it stayed nested in one.
                    if (hasNothingToShow) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = KluvsTheme.colors.background)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProfileSection(
                                avatarUrl = state.profile?.avatarUrl,
                                name = state.profile?.name ?: "",
                                handle = state.profile?.handle ?: ""
                            )

                            Divider()

                            StatisticsSection(
                                modifier = Modifier.fillMaxWidth(),
                                data = state.statistics,
                                joinDate = state.profile?.joinDate
                            )

                            Divider()

                            EmptyState(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                heading = "Nothing to track yet.",
                                body = "Join or start a club and your next read will show up here."
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = KluvsTheme.colors.background)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProfileSection(
                                avatarUrl = state.profile?.avatarUrl,
                                name = state.profile?.name ?: "",
                                handle = state.profile?.handle ?: ""
                            )

                            Divider()

                            StatisticsSection(
                                modifier = Modifier.fillMaxWidth(),
                                data = state.statistics,
                                joinDate = state.profile?.joinDate
                            )

                            Divider()

                            if (state.upNext != null) {
                                UpNextSection(
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                                    upNext = state.upNext
                                )
                                Divider()
                            }

                            ShelfSection(
                                modifier = Modifier.fillMaxWidth(),
                                shelf = state.shelf,
                                onUpdateProgress = onUpdateProgress
                            )
                        }
                    }
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
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Read-only — editing (including the image) happens in Settings.
        Avatar(
            name = name,
            avatarUrl = avatarUrl,
            size = 64.dp,
            isOwn = true,
            contentDescription = stringResource(R.string.profile_picture)
        )

        Spacer(Modifier.padding(8.dp))

        Column {
            Text(
                text = name,
                color = KluvsTheme.colors.content,
                style = KluvsTheme.typography.headline.small
            )
            Text(
                text = "@$handle",
                color = KluvsTheme.colors.contentMuted,
                style = KluvsTheme.typography.body.medium
            )
        }
    }
}

@Composable
private fun Divider(
    modifier: Modifier = Modifier,
    color: Color = KluvsTheme.colors.divider
) {
    HorizontalDivider(modifier = modifier, color = color)
}

@PreviewLightDark
@Composable
fun Preview_MeScreen() = KluvsTheme {
    Column(modifier = Modifier.background(color = KluvsTheme.colors.background)) {
        TopAppBar(header = stringResource(R.string.me))

        MeScreenContent(
            state = MeState(
                isLoading = false,
                profile = UserProfile(
                    memberId = "0",
                    name = "Quill",
                    handle = "quill-bot",
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
        )
    }
}