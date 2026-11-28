package com.ivangarzab.kluvs.ui.clubs

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.ivangarzab.kluvs.designsystem.components.KluvsSnackbar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.clubs.presentation.ClubDetailsDialog
import com.ivangarzab.kluvs.clubs.presentation.ClubDetailsSheet
import com.ivangarzab.kluvs.clubs.presentation.ClubDetailsState
import com.ivangarzab.kluvs.clubs.presentation.ClubDetailsViewModel
import com.ivangarzab.kluvs.model.AttendanceStatus
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.JoinPolicy
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.presentation.state.ScreenState
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.components.appbars.TopAppBar
import com.ivangarzab.kluvs.designsystem.components.ErrorScreen
import com.ivangarzab.kluvs.designsystem.components.loading.PullToRefreshContainer
import com.ivangarzab.kluvs.designsystem.components.menus.ActionMenu
import com.ivangarzab.kluvs.designsystem.components.menus.ActionMenuItem
import com.ivangarzab.kluvs.designsystem.components.modals.ConfirmationDialog
import com.ivangarzab.kluvs.designsystem.components.ProgressTrackingMode
import com.ivangarzab.kluvs.ui.components.LoadingScreen
import com.ivangarzab.kluvs.designsystem.components.ReadingProgressBottomSheet
import com.ivangarzab.kluvs.ui.components.RoleEyebrow
import com.ivangarzab.kluvs.ui.components.ScreenEffectHost
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
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

/**
 * Entry point for the Clubs tab: owns the list → detail navigation (mirrors web's
 * `/clubs` → `/clubs/:id`) and the single [ClubDetailsViewModel] shared across both.
 */
@Composable
fun ClubsScreen(
    modifier: Modifier = Modifier,
    userId: String,
    initialClubId: String? = null,
    viewModel: ClubDetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    val screenState = when {
        state.availableClubs.isNotEmpty() -> ScreenState.Content
        state.isLoading -> ScreenState.Loading
        state.error != null -> ScreenState.Error(state.error!!)
        else -> ScreenState.Empty
    }

    LaunchedEffect(userId) {
        viewModel.loadUserClubs(userId)
    }

    ScreenEffectHost(viewModel.screenEffects, snackbarHostState)

    // Navigate into a just-created club, then consume the signal
    LaunchedEffect(state.createdClubId) {
        state.createdClubId?.let { clubId ->
            viewModel.selectClub(clubId)
            navController.navigate("detail/$clubId")
            viewModel.onConsumeCreatedClubId()
        }
    }

    // Navigate back out of a just-deleted club's detail screen, then consume the signal
    LaunchedEffect(state.deletedClubId) {
        state.deletedClubId?.let {
            navController.popBackStack()
            viewModel.onConsumeDeletedClubId()
        }
    }

    // Navigate back out of a club the signed-in member just left, then consume the signal
    LaunchedEffect(state.leftClubId) {
        state.leftClubId?.let {
            navController.popBackStack()
            viewModel.onConsumeLeftClubId()
        }
    }

    // Navigate into a club opened from outside this tab (e.g. auto-join after sign-in)
    LaunchedEffect(initialClubId) {
        initialClubId?.let { clubId ->
            viewModel.selectClub(clubId)
            navController.navigate("detail/$clubId")
        }
    }

    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = "list"
        ) {
            composable("list") {
                ClubsListScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    screenState = screenState,
                    onRetry = viewModel::refresh,
                    onRefresh = { viewModel.loadUserClubs(userId, forceRefresh = true) },
                    onClubSelected = { clubId ->
                        viewModel.selectClub(clubId)
                        navController.navigate("detail/$clubId")
                    },
                    onAddClub = { viewModel.onOpenSheet(ClubDetailsSheet.CreateClub) },
                    onJoinWithCode = { viewModel.onOpenSheet(ClubDetailsSheet.JoinClub) }
                )

                when (state.activeSheet) {
                    ClubDetailsSheet.CreateClub -> CreateClubBottomSheet(
                        onCreate = { name -> viewModel.onCreateClub(userId, name) },
                        onDismiss = viewModel::onDismissSheet
                    )
                    // Joining runs in JoinViewModel, so this screen closes the sheet itself once it reports back
                    ClubDetailsSheet.JoinClub -> JoinBottomSheet(
                        onDismiss = viewModel::onDismissSheet,
                        onJoined = { clubId ->
                            viewModel.onDismissSheet()
                            viewModel.selectClub(clubId)
                            navController.navigate("detail/$clubId")
                        }
                    )
                    else -> Unit
                }
            }
            composable("detail/{clubId}") {
                ClubsScreenContent(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    screenState = screenState,
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() },
                    onRetry = viewModel::refresh,
                    onRefresh = { viewModel.refresh(forceRefresh = true) },
                    onUpdateClubName = viewModel::onUpdateClubName,
                    onDeleteClub = viewModel::onDeleteClub,
                    onUpdateJoinPolicy = viewModel::onUpdateJoinPolicy,
                    onRotateInviteLink = viewModel::onRotateInviteLink,
                    onCopyInviteLink = viewModel::onCopyInviteLink,
                    onShareInviteLink = viewModel::onShareInviteLink,
                    onBookSearchQueryChange = viewModel::onBookSearchQueryChange,
                    onSearchBooks = viewModel::onSearchBooks,
                    onSelectBook = viewModel::onSelectBook,
                    onClearBook = viewModel::onClearSelectedBook,
                    onCreateSession = viewModel::onCreateSession,
                    onUpdateSession = viewModel::onUpdateSession,
                    onEndSession = viewModel::onEndSession,
                    onToggleParticipation = viewModel::onToggleParticipation,
                    onSaveProgress = viewModel::onSaveProgress,
                    onCreateDiscussion = viewModel::onCreateDiscussion,
                    onUpdateDiscussion = viewModel::onUpdateDiscussion,
                    onDeleteDiscussion = viewModel::onDeleteDiscussion,
                    onLoadAttendanceRoster = viewModel::onLoadAttendanceRoster,
                    onSetAttendance = viewModel::onSetAttendance,
                    onLoadDiscussionNote = viewModel::onLoadDiscussionNote,
                    onSaveDiscussionNote = viewModel::onSaveDiscussionNote,
                    onDeleteDiscussionNote = viewModel::onDeleteDiscussionNote,
                    onUpdateMemberRole = { memberId, newRole ->
                        val currentMemberId = state.currentMemberId ?: return@ClubsScreenContent
                        viewModel.onUpdateMemberRole(memberId, currentMemberId, newRole)
                    },
                    onRemoveMember = { memberId ->
                        // Without our own member ID there's nothing to send; just close the dialog
                        state.currentMemberId?.let { viewModel.onRemoveMember(memberId, it) }
                            ?: viewModel.onDismissDialog()
                    },
                    onLeaveClub = viewModel::onLeaveClub,
                    onTransferOwnership = viewModel::onTransferOwnership,
                    onOpenSheet = viewModel::onOpenSheet,
                    onDismissSheet = viewModel::onDismissSheet,
                    onOpenDialog = viewModel::onOpenDialog,
                    onDismissDialog = viewModel::onDismissDialog
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            snackbar = { KluvsSnackbar(it) }
        )
    }
}

@Composable
fun ClubsScreenContent(
    modifier: Modifier = Modifier,
    state: ClubDetailsState,
    screenState: ScreenState,
    userId: String = "",
    onRetry: () -> Unit,
    onRefresh: () -> Unit = onRetry,
    onNavigateBack: () -> Unit = {},
    onUpdateClubName: (String) -> Unit = {},
    onDeleteClub: () -> Unit = {},
    onUpdateJoinPolicy: (JoinPolicy) -> Unit = {},
    onRotateInviteLink: () -> Unit = {},
    onCopyInviteLink: (String) -> Unit = {},
    onShareInviteLink: (String) -> Unit = {},
    onBookSearchQueryChange: (String) -> Unit = {},
    onSearchBooks: (String) -> Unit = {},
    onSelectBook: (Book) -> Unit = {},
    onClearBook: () -> Unit = {},
    onCreateSession: (Book, LocalDateTime?) -> Unit = { _, _ -> },
    onUpdateSession: (Book?, LocalDateTime?) -> Unit = { _, _ -> },
    onEndSession: () -> Unit = {},
    onToggleParticipation: (memberId: String, isReading: Boolean) -> Unit = { _, _ -> },
    onSaveProgress: (ProgressType, Int?, Float?, Boolean) -> Unit = { _, _, _, _ -> },
    onCreateDiscussion: (String, String, LocalDateTime) -> Unit = { _, _, _ -> },
    onUpdateDiscussion: (String, String?, String?, LocalDateTime?) -> Unit = { _, _, _, _ -> },
    onDeleteDiscussion: (String) -> Unit = {},
    onLoadAttendanceRoster: (discussionId: String) -> Unit = {},
    onSetAttendance: (discussionId: String, status: AttendanceStatus) -> Unit = { _, _ -> },
    onLoadDiscussionNote: (discussionId: String) -> Unit = {},
    onSaveDiscussionNote: (discussionId: String, content: String) -> Unit = { _, _ -> },
    onDeleteDiscussionNote: (discussionId: String) -> Unit = {},
    onUpdateMemberRole: (memberId: String, newRole: Role) -> Unit = { _, _ -> },
    onRemoveMember: (memberId: String) -> Unit = {},
    onLeaveClub: () -> Unit = {},
    onTransferOwnership: (memberId: String) -> Unit = {},
    onOpenSheet: (ClubDetailsSheet) -> Unit = {},
    onDismissSheet: () -> Unit = {},
    onOpenDialog: (ClubDetailsDialog) -> Unit = {},
    onDismissDialog: () -> Unit = {},
) {
    val currentUserId = userId

    AnimatedContent(
        targetState = screenState,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "ClubsScreenTransition"
    ) { targetState ->
        when (targetState) {
            is ScreenState.Loading -> LoadingScreen()
            is ScreenState.Error -> ErrorScreen(
                message = targetState.message,
                onRetry = onRetry
            )
            is ScreenState.Empty -> {
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
                            style = KluvsTheme.typography.headline.small,
                            color = KluvsTheme.colors.content
                        )
                        Text(
                            text = "Join a club to get started",
                            style = KluvsTheme.typography.body.medium,
                            color = KluvsTheme.colors.contentMuted
                        )
                    }
                }
            }
            is ScreenState.Content -> {
                val scope = rememberCoroutineScope()
                val pagerState = rememberPagerState(
                    pageCount = { 3 },
                    initialPage = 0
                )
                val tabs = listOf(
                    stringResource(R.string.general),
                    stringResource(R.string.active_session),
                    stringResource(R.string.members)
                )

                Column(
                    modifier = modifier.background(color = KluvsTheme.colors.background)
                ) {
                    // Operation in-progress indicator
                    if (state.isOperationInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    TopAppBar(
                        header = stringResource(R.string.club_eyebrow),
                        title = state.currentClubDetails?.clubName,
                        onNavigateBack = onNavigateBack,
                        action = {
                            if (state.userRole != null) {
                                val canManageClub = state.userRole == Role.OWNER
                                ActionMenu(
                                    items = buildList {
                                        add(
                                            ActionMenuItem(
                                                label = "Share",
                                                onClick = { onOpenSheet(ClubDetailsSheet.ShareClub) }
                                            )
                                        )
                                        if (canManageClub) {
                                            add(ActionMenuItem(label = "Edit", onClick = { onOpenSheet(ClubDetailsSheet.EditClub) }))
                                            add(
                                                ActionMenuItem(
                                                    label = "Delete",
                                                    onClick = { onOpenDialog(ClubDetailsDialog.DeleteClub) },
                                                    isDestructive = true
                                                )
                                            )
                                        } else {
                                            add(
                                                ActionMenuItem(
                                                    label = "Leave Club",
                                                    onClick = { onOpenDialog(ClubDetailsDialog.LeaveClub) },
                                                    isDestructive = true
                                                )
                                            )
                                        }
                                    },
                                    contentDescription = "Club options"
                                )
                            }
                        },
                    )

                    if (state.isLoading && state.currentClubDetails == null) {
                        ClubDetailsSkeleton(modifier = Modifier.fillMaxSize())
                    } else {
                        state.currentClubDetails?.let { clubDetails ->
                            ClubMetaRow(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                userRole = state.userRole,
                                foundedYear = clubDetails.foundedYear,
                                memberCount = clubDetails.memberCount
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = KluvsTheme.colors.background,
                            contentColor = KluvsTheme.colors.contentMuted,
                            indicator = { tabPositions ->
                                if (pagerState.currentPage < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                        color = KluvsTheme.colors.accent
                                    )
                                }
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val selected = pagerState.currentPage == index
                                Tab(
                                    selected = selected,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = title,
                                            style = KluvsTheme.typography.label,
                                            color = if (selected) {
                                                KluvsTheme.colors.accent
                                            } else {
                                                KluvsTheme.colors.contentMuted
                                            }
                                        )
                                    }
                                )
                            }
                        }

                        val tabModifier = Modifier
                            .background(color = KluvsTheme.colors.background)
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp)

                        // ActiveSessionTab's discussion list handles its own bottom inset via
                        // LazyColumn contentPadding, so its bottom edge scrolls with content
                        // instead of pinning a fixed gap above the nav bar regardless of scroll position.
                        val discussionsTabModifier = Modifier
                            .background(color = KluvsTheme.colors.background)
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)

                        PullToRefreshContainer(
                            isLoading = state.isLoading,
                            onRefresh = onRefresh,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            // Swipeable tab content
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                when (page) {
                                    0 -> OverviewTab(
                                        modifier = tabModifier,
                                        clubDetails = state.currentClubDetails,
                                        sessionDetails = state.activeSession,
                                        ownProgress = state.ownProgress,
                                        userRole = state.userRole,
                                        members = state.members,
                                        currentMemberId = state.currentMemberId,
                                        onEditSession = { onOpenSheet(ClubDetailsSheet.EditSession) },
                                        onEndSession = { onOpenDialog(ClubDetailsDialog.EndSession) },
                                        onUpdateProgress = { onOpenSheet(ClubDetailsSheet.UpdateProgress) },
                                        onCreateSession = { onOpenSheet(ClubDetailsSheet.CreateSession) },
                                        onToggleParticipation = { isReading ->
                                            val currentMemberId = state.currentMemberId ?: return@OverviewTab
                                            onToggleParticipation(currentMemberId, isReading)
                                        }
                                    )
                                    1 -> ActiveSessionTab(
                                        modifier = discussionsTabModifier,
                                        sessionDetails = state.activeSession,
                                        userRole = state.userRole,
                                        onCreateSession = { onOpenSheet(ClubDetailsSheet.CreateSession) },
                                        onCreateDiscussion = { onOpenSheet(ClubDetailsSheet.CreateDiscussion) },
                                        onEditDiscussion = { id -> onOpenSheet(ClubDetailsSheet.EditDiscussion(id)) },
                                        onDeleteDiscussion = { id -> onOpenDialog(ClubDetailsDialog.DeleteDiscussion(id)) },
                                        onOpenNote = { id -> onOpenSheet(ClubDetailsSheet.DiscussionNote(id)) },
                                        discussionRosters = state.discussionRosters,
                                        onLoadAttendanceRoster = onLoadAttendanceRoster,
                                        onSetAttendance = onSetAttendance
                                    )
                                    2 -> MembersTab(
                                        modifier = tabModifier,
                                        members = state.members,
                                        participants = state.activeSession?.participants ?: emptyList(),
                                        currentUserId = currentUserId,
                                        userRole = state.userRole,
                                        onChangeRole = { memberId -> onOpenSheet(ClubDetailsSheet.ChangeRole(memberId)) },
                                        onRemoveMember = { memberId -> onOpenDialog(ClubDetailsDialog.RemoveMember(memberId)) },
                                        onTransferOwnership = { memberId -> onOpenDialog(ClubDetailsDialog.TransferOwnership(memberId)) },
                                        onInviteMember = { onOpenSheet(ClubDetailsSheet.ShareClub) }
                                    )
                                }
                            }
                        }
                    }
                }

                ClubDetailsSheetHost(
                    state = state,
                    onDismissSheet = onDismissSheet,
                    onOpenDialog = onOpenDialog,
                    onUpdateClubName = onUpdateClubName,
                    onUpdateJoinPolicy = onUpdateJoinPolicy,
                    onRotateInviteLink = onRotateInviteLink,
                    onCopyInviteLink = onCopyInviteLink,
                    onShareInviteLink = onShareInviteLink,
                    onBookSearchQueryChange = onBookSearchQueryChange,
                    onSearchBooks = onSearchBooks,
                    onSelectBook = onSelectBook,
                    onClearBook = onClearBook,
                    onCreateSession = onCreateSession,
                    onUpdateSession = onUpdateSession,
                    onSaveProgress = onSaveProgress,
                    onCreateDiscussion = onCreateDiscussion,
                    onUpdateDiscussion = onUpdateDiscussion,
                    onLoadDiscussionNote = onLoadDiscussionNote,
                    onSaveDiscussionNote = onSaveDiscussionNote,
                    onUpdateMemberRole = onUpdateMemberRole,
                )

                // Drawn after the sheet so a dialog opened over a sheet (delete note) lands on top
                ClubDetailsDialogHost(
                    state = state,
                    onDismissDialog = onDismissDialog,
                    onDeleteClub = onDeleteClub,
                    onLeaveClub = onLeaveClub,
                    onEndSession = onEndSession,
                    onDeleteDiscussion = onDeleteDiscussion,
                    onDeleteDiscussionNote = onDeleteDiscussionNote,
                    onRemoveMember = onRemoveMember,
                    onTransferOwnership = onTransferOwnership,
                )
            }
        }
    }
}

/** Draws [ClubDetailsState.activeSheet]. Saves close their sheet from the ViewModel once they succeed. */
@Composable
private fun ClubDetailsSheetHost(
    state: ClubDetailsState,
    onDismissSheet: () -> Unit,
    onOpenDialog: (ClubDetailsDialog) -> Unit,
    onUpdateClubName: (String) -> Unit,
    onUpdateJoinPolicy: (JoinPolicy) -> Unit,
    onRotateInviteLink: () -> Unit,
    onCopyInviteLink: (String) -> Unit,
    onShareInviteLink: (String) -> Unit,
    onBookSearchQueryChange: (String) -> Unit,
    onSearchBooks: (String) -> Unit,
    onSelectBook: (Book) -> Unit,
    onClearBook: () -> Unit,
    onCreateSession: (Book, LocalDateTime?) -> Unit,
    onUpdateSession: (Book?, LocalDateTime?) -> Unit,
    onSaveProgress: (ProgressType, Int?, Float?, Boolean) -> Unit,
    onCreateDiscussion: (String, String, LocalDateTime) -> Unit,
    onUpdateDiscussion: (String, String?, String?, LocalDateTime?) -> Unit,
    onLoadDiscussionNote: (discussionId: String) -> Unit,
    onSaveDiscussionNote: (discussionId: String, content: String) -> Unit,
    onUpdateMemberRole: (memberId: String, newRole: Role) -> Unit,
) {
    when (val sheet = state.activeSheet) {
        ClubDetailsSheet.EditClub -> EditClubBottomSheet(
            currentName = state.currentClubDetails?.clubName ?: "",
            onSave = onUpdateClubName,
            onDismiss = onDismissSheet,
            onDeleteClub = {
                onDismissSheet()
                onOpenDialog(ClubDetailsDialog.DeleteClub)
            }
        )

        ClubDetailsSheet.ShareClub -> ShareClubBottomSheet(
            joinPolicy = state.currentClubDetails?.joinPolicy,
            inviteToken = state.currentClubDetails?.inviteToken,
            canManage = state.userRole == Role.OWNER,
            isOperationInProgress = state.isOperationInProgress,
            onTogglePolicy = onUpdateJoinPolicy,
            onRotate = onRotateInviteLink,
            onCopy = onCopyInviteLink,
            onShare = onShareInviteLink,
            onDismiss = onDismissSheet
        )

        ClubDetailsSheet.CreateSession -> CreateSessionBottomSheet(
            bookSearchQuery = state.bookSearchQuery,
            bookSearchResults = state.bookSearchResults,
            selectedBook = state.selectedBook,
            isSearchingBooks = state.isSearchingBooks,
            isRegisteringBook = state.isRegisteringBook,
            bookSearchError = state.bookSearchError,
            onBookSearchQueryChange = onBookSearchQueryChange,
            onSearchBooks = onSearchBooks,
            onSelectBook = onSelectBook,
            onClearBook = onClearBook,
            onSave = onCreateSession,
            onDismiss = onDismissSheet
        )

        ClubDetailsSheet.EditSession -> EditSessionBottomSheet(
            initialDueDate = state.activeSession?.rawDueDate,
            onSave = { dueDate -> onUpdateSession(null, dueDate) },
            onDismiss = onDismissSheet
        )

        ClubDetailsSheet.UpdateProgress -> state.activeSession?.let { session ->
            // ReadingProgressBottomSheet is hollow (design-system primitives
            // migration) — it reports back in ProgressTrackingMode, translated to the
            // domain ProgressType at this boundary.
            ReadingProgressBottomSheet(
                bookTitle = session.book.title,
                pageCount = session.book.pageCount,
                initialType = (state.ownProgress?.type ?: ProgressType.PAGE).toTrackingMode(),
                initialCurrentPage = state.ownProgress?.currentPage,
                initialPercentComplete = state.ownProgress?.percentComplete,
                initialMarkFinished = state.ownProgress?.isCompleted ?: false,
                onSave = { type, currentPage, percentComplete, markFinished ->
                    onSaveProgress(type.toDomain(), currentPage, percentComplete, markFinished)
                },
                onDismiss = onDismissSheet
            )
        }

        ClubDetailsSheet.CreateDiscussion -> DiscussionBottomSheet(
            bookTitle = state.activeSession?.book?.title,
            onSave = onCreateDiscussion,
            onDismiss = onDismissSheet
        )

        is ClubDetailsSheet.EditDiscussion -> {
            val discussion = state.activeSession?.discussions?.find { it.id == sheet.discussionId }
            DiscussionBottomSheet(
                isEditing = true,
                initialTitle = discussion?.title ?: "",
                initialLocation = discussion?.location ?: "",
                initialDate = discussion?.rawDate,
                bookTitle = state.activeSession?.book?.title,
                onSave = { title, location, date -> onUpdateDiscussion(sheet.discussionId, title, location, date) },
                onDismiss = onDismissSheet
            )
        }

        is ClubDetailsSheet.DiscussionNote -> {
            LaunchedEffect(sheet.discussionId) { onLoadDiscussionNote(sheet.discussionId) }
            DiscussionNoteSheet(
                discussionTitle = state.activeSession?.discussions
                    ?.firstOrNull { it.id == sheet.discussionId }?.title,
                note = state.discussionNotes[sheet.discussionId],
                onSave = { content -> onSaveDiscussionNote(sheet.discussionId, content) },
                onDelete = { onOpenDialog(ClubDetailsDialog.DeleteDiscussionNote(sheet.discussionId)) },
                onDismiss = onDismissSheet
            )
        }

        is ClubDetailsSheet.ChangeRole -> {
            val member = state.members.find { it.memberId == sheet.memberId }
            ChangeRoleBottomSheet(
                memberName = member?.name ?: "",
                currentRole = member?.role ?: Role.MEMBER,
                onSave = { newRole -> onUpdateMemberRole(sheet.memberId, newRole) },
                onDismiss = onDismissSheet
            )
        }

        // Drawn by the clubs list, not the detail screen
        ClubDetailsSheet.CreateClub, ClubDetailsSheet.JoinClub, null -> Unit
    }
}

/** Draws [ClubDetailsState.activeDialog]. Confirming calls the action, which closes the dialog. */
@Composable
private fun ClubDetailsDialogHost(
    state: ClubDetailsState,
    onDismissDialog: () -> Unit,
    onDeleteClub: () -> Unit,
    onLeaveClub: () -> Unit,
    onEndSession: () -> Unit,
    onDeleteDiscussion: (String) -> Unit,
    onDeleteDiscussionNote: (discussionId: String) -> Unit,
    onRemoveMember: (memberId: String) -> Unit,
    onTransferOwnership: (memberId: String) -> Unit,
) {
    when (val dialog = state.activeDialog) {
        ClubDetailsDialog.DeleteClub -> ConfirmationDialog(
            title = "Delete Club",
            message = "Are you sure you want to delete \"${state.currentClubDetails?.clubName}\"? This action cannot be undone.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = onDeleteClub,
            onDismiss = onDismissDialog
        )

        ClubDetailsDialog.LeaveClub -> ConfirmationDialog(
            title = "Leave Club",
            message = "Are you sure you want to leave \"${state.currentClubDetails?.clubName}\"? You'll need a new invite to rejoin.",
            confirmLabel = "Leave",
            isDestructive = true,
            onConfirm = onLeaveClub,
            onDismiss = onDismissDialog
        )

        ClubDetailsDialog.EndSession -> {
            val readingCount = state.activeSession?.participants?.count { it.isReading } ?: 0
            val creditMessage = if (readingCount > 0) {
                "$readingCount member${if (readingCount != 1) "s" else ""} will receive credit."
            } else {
                "No members are marked as reading — no credit will be awarded."
            }
            ConfirmationDialog(
                title = "End Session",
                message = "Are you sure you want to end the current reading session for " +
                    "\"${state.activeSession?.book?.title}\"?\n\n$creditMessage",
                confirmLabel = "Confirm End",
                isDestructive = true,
                onConfirm = onEndSession,
                onDismiss = onDismissDialog
            )
        }

        is ClubDetailsDialog.DeleteDiscussion -> ConfirmationDialog(
            title = "Delete Discussion",
            message = "Are you sure you want to delete this discussion?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = { onDeleteDiscussion(dialog.discussionId) },
            onDismiss = onDismissDialog
        )

        is ClubDetailsDialog.DeleteDiscussionNote -> ConfirmationDialog(
            title = "Delete Note",
            message = "Are you sure you want to delete this note?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = { onDeleteDiscussionNote(dialog.discussionId) },
            onDismiss = onDismissDialog
        )

        is ClubDetailsDialog.RemoveMember -> {
            val member = state.members.find { it.memberId == dialog.memberId }
            ConfirmationDialog(
                title = "Remove Member",
                message = "Are you sure you want to remove ${member?.name ?: "this member"} from the club?",
                confirmLabel = "Remove",
                isDestructive = true,
                onConfirm = { onRemoveMember(dialog.memberId) },
                onDismiss = onDismissDialog
            )
        }

        is ClubDetailsDialog.TransferOwnership -> {
            val member = state.members.find { it.memberId == dialog.memberId }
            ConfirmationDialog(
                title = "Transfer Ownership",
                message = "Make ${member?.name ?: "this member"} the new owner of \"${state.currentClubDetails?.clubName}\"? You'll be demoted to Admin.",
                confirmLabel = "Transfer",
                isDestructive = true,
                onConfirm = { onTransferOwnership(dialog.memberId) },
                onDismiss = onDismissDialog
            )
        }

        null -> Unit
    }
}

@Composable
private fun ClubMetaRow(
    userRole: Role?,
    foundedYear: String?,
    memberCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        userRole?.let { role ->
            RoleEyebrow(role = role)
            MetaDot()
        }
        if (foundedYear != null) {
            Text(
                text = stringResource(R.string.founded_x, foundedYear).uppercase(),
                style = KluvsTheme.typography.eyebrow,
                color = KluvsTheme.colors.contentMuted
            )
            MetaDot()
        }
        Text(
            text = stringResource(R.string.x_members, memberCount).uppercase(),
            style = KluvsTheme.typography.eyebrow,
            color = KluvsTheme.colors.contentMuted
        )
    }
}

@Composable
private fun MetaDot() {
    Box(
        modifier = Modifier
            .size(3.dp)
            .background(color = KluvsTheme.colors.contentMuted, shape = CircleShape)
    )
}

@PreviewLightDark
@Composable
fun Preview_ClubsScreen() = KluvsTheme {
    ClubsScreenContent(
        modifier = Modifier.background(color = KluvsTheme.colors.background),
        state = ClubDetailsState(
            isLoading = false
        ),
        screenState = ScreenState.Content,
        onRetry = { }
    )
}
