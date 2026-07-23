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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.clubs.presentation.ClubDetailsState
import com.ivangarzab.kluvs.clubs.presentation.ClubDetailsViewModel
import com.ivangarzab.kluvs.clubs.presentation.OperationResult
import com.ivangarzab.kluvs.model.AttendanceStatus
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.JoinPolicy
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.presentation.state.ScreenState
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme
import com.ivangarzab.kluvs.designsystem.components.ErrorScreen
import com.ivangarzab.kluvs.designsystem.components.ProgressTrackingMode
import com.ivangarzab.kluvs.ui.components.LoadingScreen
import com.ivangarzab.kluvs.designsystem.components.ReadingProgressBottomSheet
import com.ivangarzab.kluvs.ui.components.RoleEyebrow
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
    onNavigateToJoin: () -> Unit = {},
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

    // Show snackbar whenever operationResult is set, then consume it
    LaunchedEffect(state.operationResult) {
        state.operationResult?.let { result ->
            val message = when (result) {
                is OperationResult.Success -> result.message
                is OperationResult.Error -> result.message
            }
            snackbarHostState.showSnackbar(message)
            viewModel.onConsumeOperationResult()
        }
    }

    // Navigate into a just-created club, then consume the signal
    LaunchedEffect(state.createdClubId) {
        state.createdClubId?.let { clubId ->
            navController.navigate("detail/$clubId")
            viewModel.onConsumeCreatedClubId()
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
                var showCreateClubSheet by remember { mutableStateOf(false) }

                ClubsListScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    screenState = screenState,
                    onRetry = viewModel::refresh,
                    onClubSelected = { clubId ->
                        viewModel.selectClub(clubId)
                        navController.navigate("detail/$clubId")
                    },
                    onAddClub = { showCreateClubSheet = true },
                    onJoinWithCode = onNavigateToJoin
                )

                if (showCreateClubSheet) {
                    CreateClubBottomSheet(
                        onCreate = { name ->
                            viewModel.onCreateClub(userId, name)
                            showCreateClubSheet = false
                        },
                        onDismiss = { showCreateClubSheet = false }
                    )
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
                    onUpdateClubName = viewModel::onUpdateClubName,
                    onDeleteClub = viewModel::onDeleteClub,
                    onUpdateJoinPolicy = viewModel::onUpdateJoinPolicy,
                    onRotateInviteLink = viewModel::onRotateInviteLink,
                    onOpenShareSheet = { viewModel.refresh(forceRefresh = true) },
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
                        val currentMemberId = state.members.find { it.userId == userId }?.memberId ?: return@ClubsScreenContent
                        viewModel.onUpdateMemberRole(memberId, currentMemberId, newRole)
                    },
                    onRemoveMember = { memberId ->
                        val currentMemberId = state.members.find { it.userId == userId }?.memberId ?: return@ClubsScreenContent
                        viewModel.onRemoveMember(memberId, currentMemberId)
                    }
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
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
    onNavigateBack: () -> Unit = {},
    onUpdateClubName: (String) -> Unit = {},
    onDeleteClub: () -> Unit = {},
    onUpdateJoinPolicy: (JoinPolicy) -> Unit = {},
    onRotateInviteLink: () -> Unit = {},
    onOpenShareSheet: () -> Unit = {},
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
) {
    // Sheet / dialog visibility state
    var showEditClubSheet by remember { mutableStateOf(false) }
    var showShareClubSheet by remember { mutableStateOf(false) }
    var showDeleteClubDialog by remember { mutableStateOf(false) }
    var showCreateSessionSheet by remember { mutableStateOf(false) }
    var showEditSessionSheet by remember { mutableStateOf(false) }
    var showEndSessionDialog by remember { mutableStateOf(false) }
    var showProgressSheet by remember { mutableStateOf(false) }
    var showCreateDiscussionSheet by remember { mutableStateOf(false) }
    var editingDiscussionId by remember { mutableStateOf<String?>(null) }
    var deletingDiscussionId by remember { mutableStateOf<String?>(null) }
    var openNoteDiscussionId by remember { mutableStateOf<String?>(null) }
    var changingRoleMemberId by remember { mutableStateOf<String?>(null) }
    var removingMemberId by remember { mutableStateOf<String?>(null) }

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
                    modifier = modifier.background(color = MaterialTheme.colorScheme.background)
                ) {
                    // Operation in-progress indicator
                    if (state.isOperationInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.navigate_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = stringResource(R.string.club_eyebrow).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    state.currentClubDetails?.let { clubDetails ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = clubDetails.clubName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(12.dp))
                                ClubMetaRow(
                                    userRole = state.userRole,
                                    foundedYear = clubDetails.foundedYear,
                                    memberCount = clubDetails.memberCount
                                )
                            }
                            if (state.userRole == Role.OWNER || state.userRole == Role.ADMIN) {
                                ClubOverflowMenu(
                                    canManageClub = state.userRole == Role.OWNER,
                                    onEdit = { showEditClubSheet = true },
                                    onDelete = { showDeleteClubDialog = true },
                                    onShare = {
                                        onOpenShareSheet()
                                        showShareClubSheet = true
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicator = { tabPositions ->
                            if (pagerState.currentPage < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                    color = MaterialTheme.colorScheme.primary
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
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            )
                        }
                    }

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val tabModifier = Modifier
                            .background(color = MaterialTheme.colorScheme.background)
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp)

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
                                    currentUserId = currentUserId,
                                    onEditSession = { showEditSessionSheet = true },
                                    onEndSession = { showEndSessionDialog = true },
                                    onUpdateProgress = { showProgressSheet = true },
                                    onCreateSession = { showCreateSessionSheet = true },
                                    onToggleParticipation = { isReading ->
                                        val currentMemberId = state.members.find { it.userId == currentUserId }?.memberId
                                            ?: return@OverviewTab
                                        onToggleParticipation(currentMemberId, isReading)
                                    }
                                )
                                1 -> ActiveSessionTab(
                                    modifier = tabModifier,
                                    sessionDetails = state.activeSession,
                                    userRole = state.userRole,
                                    onCreateSession = { showCreateSessionSheet = true },
                                    onCreateDiscussion = { showCreateDiscussionSheet = true },
                                    onEditDiscussion = { id -> editingDiscussionId = id },
                                    onDeleteDiscussion = { id -> deletingDiscussionId = id },
                                    onOpenNote = { id -> openNoteDiscussionId = id },
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
                                    onChangeRole = { memberId -> changingRoleMemberId = memberId },
                                    onRemoveMember = { memberId -> removingMemberId = memberId }
                                )
                            }
                        }
                    }
                }

                // TODO: Everything underneath there is screaming for a ViewModel, with side effect handling
                //  and at least one new extra enum--we'll need that soon
                // ---- General tab sheets / dialogs ----

                if (showEditClubSheet) {
                    EditClubBottomSheet(
                        currentName = state.currentClubDetails?.clubName ?: "",
                        onSave = { newName ->
                            onUpdateClubName(newName)
                            showEditClubSheet = false
                        },
                        onDismiss = { showEditClubSheet = false }
                    )
                }

                if (showShareClubSheet) {
                    ShareClubBottomSheet(
                        joinPolicy = state.currentClubDetails?.joinPolicy,
                        inviteToken = state.currentClubDetails?.inviteToken,
                        canManage = state.userRole == Role.OWNER,
                        isOperationInProgress = state.isOperationInProgress,
                        onTogglePolicy = onUpdateJoinPolicy,
                        onRotate = onRotateInviteLink,
                        onDismiss = { showShareClubSheet = false }
                    )
                }

                if (showDeleteClubDialog) {
                    ConfirmationDialog(
                        title = "Delete Club",
                        message = "Are you sure you want to delete \"${state.currentClubDetails?.clubName}\"? This action cannot be undone.",
                        confirmLabel = "Delete",
                        onConfirm = {
                            onDeleteClub()
                            showDeleteClubDialog = false
                        },
                        onDismiss = { showDeleteClubDialog = false }
                    )
                }

                // ---- Session tab sheets ----

                if (showCreateSessionSheet) {
                    CreateSessionBottomSheet(
                        onSave = { book, dueDate ->
                            onCreateSession(book, dueDate)
                            showCreateSessionSheet = false
                        },
                        onDismiss = { showCreateSessionSheet = false }
                    )
                }

                if (showEditSessionSheet) {
                    EditSessionBottomSheet(
                        currentBook = state.activeSession?.book,
                        initialDueDate = state.activeSession?.rawDueDate,
                        onSave = { book, dueDate ->
                            onUpdateSession(book, dueDate)
                            showEditSessionSheet = false
                        },
                        onDismiss = { showEditSessionSheet = false }
                    )
                }

                if (showEndSessionDialog) {
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
                        onConfirm = {
                            onEndSession()
                            showEndSessionDialog = false
                        },
                        onDismiss = { showEndSessionDialog = false }
                    )
                }

                if (showProgressSheet) {
                    state.activeSession?.let { session ->
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
                                showProgressSheet = false
                            },
                            onDismiss = { showProgressSheet = false }
                        )
                    }
                }

                // ---- Discussion sheets / dialogs ----

                if (showCreateDiscussionSheet) {
                    DiscussionBottomSheet(
                        onSave = { title, location, date ->
                            onCreateDiscussion(title, location, date)
                            showCreateDiscussionSheet = false
                        },
                        onDismiss = { showCreateDiscussionSheet = false }
                    )
                }

                editingDiscussionId?.let { discussionId ->
                    val discussion = state.activeSession?.discussions?.find { it.id == discussionId }
                    DiscussionBottomSheet(
                        initialTitle = discussion?.title ?: "",
                        initialLocation = discussion?.location ?: "",
                        initialDate = discussion?.rawDate,
                        onSave = { title, location, date ->
                            onUpdateDiscussion(discussionId, title, location, date)
                            editingDiscussionId = null
                        },
                        onDismiss = { editingDiscussionId = null }
                    )
                }

                deletingDiscussionId?.let { discussionId ->
                    ConfirmationDialog(
                        title = "Delete Discussion",
                        message = "Are you sure you want to delete this discussion?",
                        confirmLabel = "Delete",
                        onConfirm = {
                            onDeleteDiscussion(discussionId)
                            deletingDiscussionId = null
                        },
                        onDismiss = { deletingDiscussionId = null }
                    )
                }

                openNoteDiscussionId?.let { discussionId ->
                    LaunchedEffect(discussionId) { onLoadDiscussionNote(discussionId) }
                    DiscussionNoteSheet(
                        note = state.discussionNotes[discussionId],
                        onSave = { content -> onSaveDiscussionNote(discussionId, content) },
                        onDelete = {
                            onDeleteDiscussionNote(discussionId)
                            openNoteDiscussionId = null
                        },
                        onDismiss = { openNoteDiscussionId = null }
                    )
                }

                // ---- Member sheets / dialogs ----

                changingRoleMemberId?.let { memberId ->
                    val member = state.members.find { it.memberId == memberId }
                    ChangeRoleBottomSheet(
                        memberName = member?.name ?: "",
                        currentRole = member?.role ?: Role.MEMBER,
                        onSave = { newRole ->
                            onUpdateMemberRole(memberId, newRole)
                            changingRoleMemberId = null
                        },
                        onDismiss = { changingRoleMemberId = null }
                    )
                }

                removingMemberId?.let { memberId ->
                    val member = state.members.find { it.memberId == memberId }
                    ConfirmationDialog(
                        title = "Remove Member",
                        message = "Are you sure you want to remove ${member?.name ?: "this member"} from the club?",
                        confirmLabel = "Remove",
                        onConfirm = {
                            onRemoveMember(memberId)
                            removingMemberId = null
                        },
                        onDismiss = { removingMemberId = null }
                    )
                }
            }
        }
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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MetaDot()
        }
        Text(
            text = stringResource(R.string.x_members, memberCount).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetaDot() {
    Box(
        modifier = Modifier
            .size(3.dp)
            .background(color = MaterialTheme.colorScheme.onSurfaceVariant, shape = CircleShape)
    )
}

@Composable
private fun ClubOverflowMenu(
    canManageClub: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Club options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Share") },
                onClick = {
                    expanded = false
                    onShare()
                }
            )
            if (canManageClub) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        expanded = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        expanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun Preview_ClubsScreen() = KluvsTheme {
    ClubsScreenContent(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        state = ClubDetailsState(
            isLoading = false
        ),
        screenState = ScreenState.Content,
        onRetry = { }
    )
}
