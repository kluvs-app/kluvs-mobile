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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.presentation.state.ScreenState
import com.ivangarzab.kluvs.theme.KluvsTheme
import com.ivangarzab.kluvs.ui.components.ErrorScreen
import com.ivangarzab.kluvs.ui.components.LoadingScreen
import com.ivangarzab.kluvs.ui.components.ReadingProgressBottomSheet
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel

/**
 * Entry point for the Clubs tab: owns the list → detail navigation (mirrors web's
 * `/clubs` → `/clubs/:id`) and the single [ClubDetailsViewModel] shared across both.
 */
@Composable
fun ClubsScreen(
    modifier: Modifier = Modifier,
    userId: String,
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
                    onClubSelected = { clubId ->
                        viewModel.selectClub(clubId)
                        navController.navigate("detail/$clubId")
                    }
                )
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
                    onCreateSession = viewModel::onCreateSession,
                    onUpdateSession = viewModel::onUpdateSession,
                    onEndSession = viewModel::onEndSession,
                    onSaveProgress = viewModel::onSaveProgress,
                    onCreateDiscussion = viewModel::onCreateDiscussion,
                    onUpdateDiscussion = viewModel::onUpdateDiscussion,
                    onDeleteDiscussion = viewModel::onDeleteDiscussion,
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
    onCreateSession: (Book, LocalDateTime?) -> Unit = { _, _ -> },
    onUpdateSession: (Book?, LocalDateTime?) -> Unit = { _, _ -> },
    onEndSession: () -> Unit = {},
    onSaveProgress: (ProgressType, Int?, Float?, Boolean) -> Unit = { _, _, _, _ -> },
    onCreateDiscussion: (String, String, LocalDateTime) -> Unit = { _, _, _ -> },
    onUpdateDiscussion: (String, String?, String?, LocalDateTime?) -> Unit = { _, _, _, _ -> },
    onDeleteDiscussion: (String) -> Unit = {},
    onUpdateMemberRole: (memberId: String, newRole: Role) -> Unit = { _, _ -> },
    onRemoveMember: (memberId: String) -> Unit = {},
) {
    // Sheet / dialog visibility state
    var showEditClubSheet by remember { mutableStateOf(false) }
    var showDeleteClubDialog by remember { mutableStateOf(false) }
    var showCreateSessionSheet by remember { mutableStateOf(false) }
    var showEditSessionSheet by remember { mutableStateOf(false) }
    var showEndSessionDialog by remember { mutableStateOf(false) }
    var showProgressSheet by remember { mutableStateOf(false) }
    var showCreateDiscussionSheet by remember { mutableStateOf(false) }
    var editingDiscussionId by remember { mutableStateOf<String?>(null) }
    var deletingDiscussionId by remember { mutableStateOf<String?>(null) }
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

                Column(modifier = modifier) {
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
                    }

                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge
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
                            .background(color = MaterialTheme.colorScheme.surface)
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)

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
                                    onEditClub = { showEditClubSheet = true },
                                    onDeleteClub = { showDeleteClubDialog = true },
                                    onEditSession = { showEditSessionSheet = true },
                                    onEndSession = { showEndSessionDialog = true },
                                    onUpdateProgress = { showProgressSheet = true }
                                )
                                1 -> ActiveSessionTab(
                                    modifier = tabModifier,
                                    sessionDetails = state.activeSession,
                                    ownProgress = state.ownProgress,
                                    userRole = state.userRole,
                                    onCreateSession = { showCreateSessionSheet = true },
                                    onEditSession = { showEditSessionSheet = true },
                                    onEndSession = { showEndSessionDialog = true },
                                    onUpdateProgress = { showProgressSheet = true },
                                    onCreateDiscussion = { showCreateDiscussionSheet = true },
                                    onEditDiscussion = { id -> editingDiscussionId = id },
                                    onDeleteDiscussion = { id -> deletingDiscussionId = id }
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
                        ReadingProgressBottomSheet(
                            bookTitle = session.book.title,
                            pageCount = session.book.pageCount,
                            initialType = state.ownProgress?.type ?: ProgressType.PAGE,
                            initialCurrentPage = state.ownProgress?.currentPage,
                            initialPercentComplete = state.ownProgress?.percentComplete,
                            initialMarkFinished = state.ownProgress?.isCompleted ?: false,
                            onSave = { type, currentPage, percentComplete, markFinished ->
                                onSaveProgress(type, currentPage, percentComplete, markFinished)
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
