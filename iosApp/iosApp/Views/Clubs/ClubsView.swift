import SwiftUI
import Shared
import DesignSystem

private struct IDWrapper: Identifiable {
    let id: String
}

/// SwiftUI bindings over the shared ViewModel's `activeSheet` / `activeDialog`. The ViewModel
/// decides what's open; a sheet the user closes (Cancel, drag, scrim tap) reports back through
/// `onDismissSheet()`.
private extension ClubDetailsViewModelWrapper {
    /// Shown while `sheet` is the open one.
    func isShowing(_ sheet: Shared.ClubDetailsSheet) -> Binding<Bool> {
        Binding(
            get: { self.activeSheet == sheet },
            set: { if !$0 { self.onDismissSheet() } }
        )
    }

    /// For sheets that carry an ID (Edit Discussion, Notes, Change Role): the ID while one of type
    /// `T` is open.
    func sheetItem<T: Shared.ClubDetailsSheet>(_ type: T.Type, id: @escaping (T) -> String) -> Binding<IDWrapper?> {
        Binding(
            get: { (self.activeSheet as? T).map { IDWrapper(id: id($0)) } },
            set: { if $0 == nil { self.onDismissSheet() } }
        )
    }

    /// Shown while a dialog of type `T` is open. Read-only: Cancel and scrim taps go through
    /// `onDismiss`, and confirming calls an action that closes the dialog itself.
    func isShowing<T: Shared.ClubDetailsDialog>(_ type: T.Type) -> Binding<Bool> {
        Binding(get: { self.activeDialog is T }, set: { _ in })
    }
}

/// Shared by both Clubs and Me's Reading Progress sheet call sites.
func formatPercent(_ value: Float) -> String {
    value == value.rounded() ? String(Int(value)) : String(value)
}

/// Entry point for the Clubs tab: owns list -> detail navigation (mirrors web's
/// `/clubs` -> `/clubs/:id`) and the single `ClubDetailsViewModelWrapper` shared
/// across both, matching Android's `ClubsScreen`.
struct ClubsView: View {
    let userId: String
    var initialClubId: String? = nil
    /// Unused today — "Join with a code" now opens a local `JoinFields` bottom sheet instead
    /// (matches Android's move away from a full-screen nav destination). Kept wired from
    /// `ContentView` for the not-yet-built deep-link case (tapping a raw invite URL while
    /// signed out needs a full screen to land on before auth resolves), same reasoning as
    /// Android's still-registered-but-unused `JoinScreen` route.
    var onNavigateToJoin: () -> Void = {}
    @StateObject private var viewModel = ClubDetailsViewModelWrapper()
    @StateObject private var joinViewModel = JoinViewModelWrapper()
    @State private var path = NavigationPath()
    @State private var newClubName = ""

    private var isEmptyScreenState: Bool {
        if case .empty = viewModel.screenState { return true }
        return false
    }

    var body: some View {
        NavigationStack(path: $path) {
            VStack(spacing: 0) {
                // Always rendered, matching Android's ClubsListScreen - the header must not
                // disappear during loading/error/empty, only the content below it changes.
                TopAppBar(header: "Your", title: "Kluvs") {
                    // Empty state already shows its own "Join with a code" button.
                    if !isEmptyScreenState {
                        OutlinedButton(text: "Join with a code", action: { viewModel.onOpenSheet(ClubDetailsSheet.JoinClub.shared) })
                    }
                }

                Group {
                    switch viewModel.screenState {
                    case .loading:
                        ClubsListSkeleton()
                    case .error(let message):
                        ErrorView(message: message, onRetry: {
                            viewModel.loadUserClubs(userId: userId)
                        })
                    case .empty:
                        ZStack(alignment: .bottomTrailing) {
                            EmptyState(
                                heading: "No clubs yet.",
                                body: "Join a club or start your own to get things going."
                            ) {
                                SecondaryButton(text: "Join with a code", action: { viewModel.onOpenSheet(ClubDetailsSheet.JoinClub.shared) })
                            }
                            .frame(maxWidth: .infinity, maxHeight: .infinity)

                            ClubsFAB(action: {
                                newClubName = ""
                                viewModel.onOpenSheet(ClubDetailsSheet.CreateClub.shared)
                            })
                        }
                    case .content:
                        ClubsListView(
                            clubs: viewModel.availableClubs,
                            onClubSelected: { clubId in
                                viewModel.selectClub(clubId: clubId)
                                path.append(clubId)
                            },
                            onAddClub: {
                                newClubName = ""
                                viewModel.onOpenSheet(ClubDetailsSheet.CreateClub.shared)
                            },
                            isRefreshing: viewModel.isLoading,
                            onRefresh: { viewModel.loadUserClubs(userId: userId, forceRefresh: true) }
                        )
                    }
                }
            }
            .navigationDestination(for: String.self) { _ in
                ClubDetailView(userId: userId, viewModel: viewModel)
            }
        }
        .onAppear {
            viewModel.loadUserClubs(userId: userId)
        }
        .onChange(of: viewModel.createdClubId) { _, newValue in
            if let clubId = newValue {
                viewModel.selectClub(clubId: clubId)
                path.append(clubId)
                viewModel.onConsumeCreatedClubId()
            }
        }
        .onChange(of: initialClubId) { _, newValue in
            if let clubId = newValue {
                viewModel.selectClub(clubId: clubId)
                path.append(clubId)
            }
        }
        .onChange(of: joinViewModel.joinedClubId) { _, newValue in
            if let clubId = newValue {
                joinViewModel.onConsumeJoinedClubId()
                // Joining runs in JoinViewModel, so this screen closes the sheet itself once it reports back
                viewModel.onDismissSheet()
                viewModel.selectClub(clubId: clubId)
                path.append(clubId)
            }
        }
        .kluvsBottomSheet(isPresented: viewModel.isShowing(ClubDetailsSheet.JoinClub.shared), header: "Join with a Code") {
            JoinFields(viewModel: joinViewModel)
        } footer: {
            let hasPreview = joinViewModel.preview != nil
            BottomSheetFooter(
                actionLabel: hasPreview ? (joinViewModel.isJoining ? "Joining…" : "Join") : "Preview",
                onAction: hasPreview ? { joinViewModel.onJoinClicked() } : { joinViewModel.previewInvite() },
                onCancel: { viewModel.onDismissSheet() },
                actionEnabled: hasPreview
                    ? !joinViewModel.isJoining
                    : !joinViewModel.tokenInput.trimmingCharacters(in: .whitespaces).isEmpty && !joinViewModel.isLoadingPreview
            )
        }
        .kluvsBottomSheet(isPresented: viewModel.isShowing(ClubDetailsSheet.CreateClub.shared), header: "New Club") {
            CreateClubFields(name: $newClubName)
        } footer: {
            let trimmedName = newClubName.trimmingCharacters(in: .whitespaces)
            BottomSheetFooter(
                actionLabel: "Create",
                onAction: { viewModel.onCreateClub(userId: userId, name: trimmedName) },
                onCancel: { viewModel.onDismissSheet() },
                actionEnabled: !trimmedName.isEmpty
            )
        }
    }
}

#Preview {
    ClubsView(userId: "1")
}

// MARK: - Club Detail View

/// The tabbed club detail screen (masthead + Overview/Discussions/Members). Shares
/// the `ClubDetailsViewModelWrapper` instance owned by `ClubsView`.
private struct ClubDetailView: View {
    let userId: String
    @ObservedObject var viewModel: ClubDetailsViewModelWrapper
    @State private var selectedTab = 0
    @Environment(\.dismiss) private var dismiss

    // Sheet form fields. Which sheet or dialog is open lives in the shared ViewModel
    // (`viewModel.activeSheet` / `activeDialog`); only what the user is typing stays here.
    @State private var editClubName = ""
    @State private var createSessionHasDueDate = false
    @State private var createSessionDueDate = Date()
    @State private var editSessionHasDueDate = false
    @State private var editSessionDueDate = Date()
    @State private var progressType: Shared.ProgressType = .page
    @State private var progressCurrentPageText = ""
    @State private var progressPercentText = ""
    @State private var progressMarkFinished = false
    @State private var progressLastAutoTriggerValue: String? = nil
    @State private var discussionTitle = ""
    @State private var discussionLocation = ""
    @State private var discussionDate = Date()
    @State private var selectedRole: Shared.Role = .member

    /// The current user's own memberId (needed for role/remove calls and the Overview tab's
    /// participation toggle) — resolved independently of the club's member roster, see
    /// GetCurrentMemberIdUseCase's doc for why.
    private var currentMemberId: String? { viewModel.currentMemberId }

    /// Resets Create Session's fields fresh each time it opens — since the sheet is now a
    /// persistent overlay rather than a re-instantiated `.sheet()` presentation, its `@State`
    /// wouldn't otherwise reset between opens the way it used to for free. The ViewModel resets
    /// the book search itself when the sheet opens.
    private func openCreateSession() {
        createSessionHasDueDate = false
        createSessionDueDate = Date()
        viewModel.onOpenSheet(ClubDetailsSheet.CreateSession.shared)
    }

    private func openProgressSheet() {
        let ownProgress = viewModel.ownProgress
        progressType = ownProgress?.type ?? .page
        let currentPage: Int32? = ownProgress?.currentPage?.int32Value
        progressCurrentPageText = currentPage.map { String($0) } ?? ""
        let percentComplete: Float? = ownProgress?.percentComplete?.floatValue
        progressPercentText = percentComplete.map { formatPercent($0) } ?? ""
        progressMarkFinished = ownProgress?.isCompleted ?? false
        progressLastAutoTriggerValue = nil
        viewModel.onOpenSheet(ClubDetailsSheet.UpdateProgress.shared)
    }

    private func openCreateDiscussion() {
        discussionTitle = ""
        discussionLocation = ""
        discussionDate = Date()
        viewModel.onOpenSheet(ClubDetailsSheet.CreateDiscussion.shared)
    }

    private func openEditDiscussion(discussionId: String) {
        let discussion = viewModel.activeSession?.discussions.first { $0.id == discussionId }
        discussionTitle = discussion?.title ?? ""
        discussionLocation = discussion?.location ?? ""
        discussionDate = viewModel.discussionDateIso(for: discussionId)?.toSwiftDate() ?? Date()
        viewModel.onOpenSheet(ClubDetailsSheet.EditDiscussion(discussionId: discussionId))
    }

    private func openEditSession() {
        editSessionHasDueDate = viewModel.sessionDueDateIso != nil
        editSessionDueDate = viewModel.sessionDueDateIso?.toSwiftDate() ?? Date()
        viewModel.onOpenSheet(ClubDetailsSheet.EditSession.shared)
    }

    var body: some View {
        VStack(spacing: 0) {
            // Operation in-progress indicator
            if viewModel.isOperationInProgress {
                ProgressView()
                    .progressViewStyle(LinearProgressViewStyle())
                    .tint(.brandOrange)
            }

            // Masthead: eyebrow + back + club name + owner overflow, mirrors Android's TopAppBar
            TopAppBar(
                header: "Club",
                title: viewModel.clubDetails?.clubName,
                onNavigateBack: { dismiss() }
            ) {
                if viewModel.userRole != nil {
                    ActionMenu(items: {
                        var items = [
                            ActionMenuItem(label: "Share", action: { viewModel.onOpenSheet(ClubDetailsSheet.ShareClub.shared) })
                        ]
                        if viewModel.userRole == .owner {
                            items.append(ActionMenuItem(label: "Edit", action: {
                                editClubName = viewModel.clubDetails?.clubName ?? ""
                                viewModel.onOpenSheet(ClubDetailsSheet.EditClub.shared)
                            }))
                            items.append(ActionMenuItem(label: "Delete", action: { viewModel.onOpenDialog(ClubDetailsDialog.DeleteClub.shared) }, isDestructive: true))
                        } else {
                            items.append(ActionMenuItem(label: "Leave Club", action: { viewModel.onOpenDialog(ClubDetailsDialog.LeaveClub.shared) }, isDestructive: true))
                        }
                        return items
                    }())
                }
            }

            // Meta row + tab row + tab content are gated together, so the tab row doesn't
            // sit there statically while the rest of the screen is still loading. Gated on
            // clubDetails alone (not isLoading) - loadClubData's isLoading=true reset runs
            // inside a coroutine dispatched from selectClub, so there's a brief window right
            // after navigating in where isLoading can still read false with stale/nil data;
            // "do we have this club's data yet" is the more robust question to ask.
            if viewModel.clubDetails == nil {
                ClubDetailsSkeleton()
                    .padding(.top, 16)
            } else {
                if let clubDetails = viewModel.clubDetails {
                    ClubMetaRow(
                        userRole: viewModel.userRole,
                        foundedYear: clubDetails.foundedYear,
                        memberCount: Int(clubDetails.memberCount)
                    )
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 16)
                    .padding(.bottom, 8)
                }

                TabRow(
                    selectedIndex: $selectedTab,
                    titles: [
                        String(localized: "tab_general"),
                        String(localized: "tab_discussions"),
                        String(localized: "tab_members")
                    ]
                )
                .padding(.horizontal, 8)

                TabView(selection: $selectedTab) {
                    OverviewTab(
                        clubDetails: viewModel.clubDetails,
                        sessionDetails: viewModel.activeSession,
                        ownProgress: viewModel.ownProgress,
                        userRole: viewModel.userRole,
                        members: viewModel.members,
                        currentMemberId: viewModel.currentMemberId,
                        onEditSession: { openEditSession() },
                        onEndSession: { viewModel.onOpenDialog(ClubDetailsDialog.EndSession.shared) },
                        onUpdateProgress: { openProgressSheet() },
                        onCreateSession: { openCreateSession() },
                        onToggleParticipation: { isReading in
                            if let memberId = currentMemberId {
                                viewModel.onToggleParticipation(memberId: memberId, isReading: isReading)
                            }
                        }
                    )
                    .tag(0)

                    ActiveSessionTab(
                        sessionDetails: viewModel.activeSession,
                        userRole: viewModel.userRole,
                        onCreateSession: { openCreateSession() },
                        onCreateDiscussion: { openCreateDiscussion() },
                        onEditDiscussion: { id in openEditDiscussion(discussionId: id) },
                        onDeleteDiscussion: { id in viewModel.onOpenDialog(ClubDetailsDialog.DeleteDiscussion(discussionId: id)) },
                        onOpenNote: { id in viewModel.onOpenSheet(ClubDetailsSheet.DiscussionNote(discussionId: id)) },
                        discussionRosters: viewModel.discussionRosters,
                        onLoadAttendanceRoster: { id in viewModel.onLoadAttendanceRoster(discussionId: id) },
                        onSetAttendance: { id, status in viewModel.onSetAttendance(discussionId: id, status: status) }
                    )
                    .tag(1)

                    MembersTab(
                        members: viewModel.members,
                        participants: viewModel.activeSession?.participants ?? [],
                        currentUserId: userId,
                        userRole: viewModel.userRole,
                        onChangeRole: { memberId in
                            let member = viewModel.members.first { $0.memberId == memberId }
                            let assignable: [Shared.Role] = [.admin, .member]
                            selectedRole = assignable.contains(member?.role ?? .member) ? (member?.role ?? .member) : .member
                            viewModel.onOpenSheet(ClubDetailsSheet.ChangeRole(memberId: memberId))
                        },
                        onRemoveMember: { memberId in viewModel.onOpenDialog(ClubDetailsDialog.RemoveMember(memberId: memberId)) },
                        onTransferOwnership: { memberId in viewModel.onOpenDialog(ClubDetailsDialog.TransferOwnership(memberId: memberId)) },
                        onInviteMember: { viewModel.onOpenSheet(ClubDetailsSheet.ShareClub.shared) }
                    )
                    .tag(2)
                }
                .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
                .background(Color.kluvsBackground)
                .kluvsPullToRefresh(isRefreshing: viewModel.isLoading) {
                    viewModel.refresh(forceRefresh: true)
                }
            }
        }
        .background(Color.kluvsBackground)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.hidden, for: .navigationBar)
        // Operation result message — transient snackbar, not a blocking alert (a tap-to-dismiss
        // dialog on every read-toggle was obnoxious)
        .snackbar(Binding(
            get: {
                viewModel.operationMessage.map {
                    SnackbarData(message: $0, variant: viewModel.operationIsError ? .danger : .success)
                }
            },
            set: { if $0 == nil { viewModel.onConsumeOperationResult() } }
        ))
        // Share club invite link
        .kluvsBottomSheet(isPresented: viewModel.isShowing(ClubDetailsSheet.ShareClub.shared), header: "Share Club") {
            ShareClubFields(
                joinPolicy: viewModel.clubDetails?.joinPolicy,
                inviteToken: viewModel.clubDetails?.inviteToken,
                canManage: viewModel.userRole == .owner,
                isOperationInProgress: viewModel.isOperationInProgress,
                onTogglePolicy: { viewModel.onUpdateJoinPolicy($0) },
                onRotate: { viewModel.onRotateInviteLink() },
                onCopy: { viewModel.onCopyInviteLink($0) },
                onShare: { viewModel.onShareInviteLink($0) }
            )
        }
        // Edit club name
        .kluvsBottomSheet(isPresented: viewModel.isShowing(ClubDetailsSheet.EditClub.shared), header: "Edit Club") {
            EditClubFields(
                currentName: viewModel.clubDetails?.clubName ?? "",
                name: $editClubName,
                onDeleteClub: {
                    viewModel.onDismissSheet()
                    viewModel.onOpenDialog(ClubDetailsDialog.DeleteClub.shared)
                }
            )
        } footer: {
            BottomSheetFooter(
                actionLabel: "Save",
                onAction: { viewModel.onUpdateClubName(editClubName.trimmingCharacters(in: .whitespaces)) },
                onCancel: { viewModel.onDismissSheet() },
                actionEnabled: {
                    let trimmed = editClubName.trimmingCharacters(in: .whitespaces)
                    return !trimmed.isEmpty && trimmed != (viewModel.clubDetails?.clubName ?? "")
                }()
            )
        }
        .onChange(of: viewModel.deletedClubId) { _, newValue in
            if newValue != nil {
                dismiss()
                viewModel.onConsumeDeletedClubId()
            }
        }
        .onChange(of: viewModel.leftClubId) { _, newValue in
            if newValue != nil {
                dismiss()
                viewModel.onConsumeLeftClubId()
            }
        }
        // Create session
        .kluvsBottomSheet(isPresented: viewModel.isShowing(ClubDetailsSheet.CreateSession.shared), header: "Create Session") {
            CreateSessionFields(
                bookSearchQuery: Binding(
                    get: { viewModel.bookSearchQuery },
                    set: { viewModel.onBookSearchQueryChange($0) }
                ),
                bookSearchResults: viewModel.bookSearchResults,
                selectedBook: viewModel.selectedBook,
                isSearchingBooks: viewModel.isSearchingBooks,
                isRegisteringBook: viewModel.isRegisteringBook,
                bookSearchError: viewModel.bookSearchError,
                onSearchBooks: { viewModel.onSearchBooks($0) },
                onSelectBook: { viewModel.onSelectBook($0) },
                onClearBook: { viewModel.onClearSelectedBook() },
                hasDueDate: $createSessionHasDueDate,
                dueDate: $createSessionDueDate
            )
        } footer: {
            BottomSheetFooter(
                actionLabel: "Create",
                onAction: {
                    guard let book = viewModel.selectedBook else { return }
                    let resolvedDueDateIso = createSessionHasDueDate ? createSessionDueDate.toIsoString() : nil
                    viewModel.onCreateSession(book: book, dueDateIso: resolvedDueDateIso)
                },
                onCancel: { viewModel.onDismissSheet() },
                actionEnabled: viewModel.selectedBook != nil
            )
        }
        // Edit session
        .kluvsBottomSheet(isPresented: viewModel.isShowing(ClubDetailsSheet.EditSession.shared), header: "Edit Session") {
            EditSessionFields(
                hasDueDate: $editSessionHasDueDate,
                dueDate: $editSessionDueDate
            )
        } footer: {
            let hasChanges = editSessionHasDueDate != (viewModel.sessionDueDateIso != nil) ||
                (editSessionHasDueDate && editSessionDueDate != (viewModel.sessionDueDateIso?.toSwiftDate() ?? Date()))
            BottomSheetFooter(
                actionLabel: "Save",
                onAction: {
                    let resolvedDueDateIso = editSessionHasDueDate ? editSessionDueDate.toIsoString() : nil
                    viewModel.onUpdateSession(book: nil, dueDateIso: resolvedDueDateIso)
                },
                onCancel: { viewModel.onDismissSheet() },
                actionEnabled: hasChanges
            )
        }
        // Update reading progress
        .kluvsBottomSheet(
            isPresented: viewModel.isShowing(ClubDetailsSheet.UpdateProgress.shared),
            header: viewModel.ownProgress != nil ? "Update Progress" : "Track Progress"
        ) {
            if let session = viewModel.activeSession {
                ReadingProgressFields(
                    bookTitle: session.book.title,
                    pageCount: session.book.pageCount?.int32Value,
                    progressType: $progressType,
                    currentPageText: $progressCurrentPageText,
                    percentText: $progressPercentText,
                    markFinished: $progressMarkFinished,
                    lastAutoTriggerValue: $progressLastAutoTriggerValue
                )
            }
        } footer: {
            let canSave: Bool = {
                switch progressType {
                case .page: return Int32(progressCurrentPageText) != nil
                case .percent: return Float(progressPercentText) != nil
                default: return false
                }
            }()
            BottomSheetFooter(
                actionLabel: "Save Progress",
                onAction: {
                    let page = progressType == .page ? Int32(progressCurrentPageText) : nil
                    let percent: Int32? = progressType == .percent
                        ? Float(progressPercentText).map { Int32(Swift.min(100, Swift.max(0, $0.rounded()))) }
                        : nil
                    viewModel.onSaveProgress(type: progressType, currentPage: page, percentComplete: percent, markFinished: progressMarkFinished)
                },
                onCancel: { viewModel.onDismissSheet() },
                actionEnabled: canSave
            )
        }
        // Create discussion
        .kluvsBottomSheet(isPresented: viewModel.isShowing(ClubDetailsSheet.CreateDiscussion.shared), header: "Add Discussion") {
            DiscussionFields(
                title: $discussionTitle,
                location: $discussionLocation,
                selectedDate: $discussionDate,
                bookTitle: viewModel.activeSession?.book.title
            )
        } footer: {
            let trimmedTitle = discussionTitle.trimmingCharacters(in: .whitespaces)
            BottomSheetFooter(
                actionLabel: "Add Discussion",
                onAction: {
                    viewModel.onCreateDiscussion(
                        title: trimmedTitle,
                        location: discussionLocation.trimmingCharacters(in: .whitespaces),
                        dateIso: discussionDate.toIsoString()
                    )
                },
                onCancel: { viewModel.onDismissSheet() },
                actionEnabled: !trimmedTitle.isEmpty
            )
        }
        // Edit discussion
        .kluvsBottomSheet(
            item: viewModel.sheetItem(ClubDetailsSheet.EditDiscussion.self) { $0.discussionId },
            header: "Edit Discussion"
        ) { _ in
            DiscussionFields(
                title: $discussionTitle,
                location: $discussionLocation,
                selectedDate: $discussionDate,
                bookTitle: viewModel.activeSession?.book.title
            )
        } footer: { wrapper in
            let discussionId = wrapper.id
            let discussion = viewModel.activeSession?.discussions.first { $0.id == discussionId }
            let trimmedTitle = discussionTitle.trimmingCharacters(in: .whitespaces)
            let trimmedLocation = discussionLocation.trimmingCharacters(in: .whitespaces)
            let hasChanges = trimmedTitle != (discussion?.title ?? "") ||
                trimmedLocation != (discussion?.location ?? "") ||
                discussionDate != (viewModel.discussionDateIso(for: discussionId)?.toSwiftDate() ?? Date())
            BottomSheetFooter(
                actionLabel: "Update Discussion",
                onAction: {
                    viewModel.onUpdateDiscussion(
                        discussionId: discussionId,
                        title: trimmedTitle,
                        location: trimmedLocation,
                        dateIso: discussionDate.toIsoString()
                    )
                },
                onCancel: { viewModel.onDismissSheet() },
                actionEnabled: !trimmedTitle.isEmpty && hasChanges
            )
        }
        // Discussion note
        .kluvsBottomSheet(
            item: viewModel.sheetItem(ClubDetailsSheet.DiscussionNote.self) { $0.discussionId },
            header: "Notes"
        ) { wrapper in
            let discussionId = wrapper.id
            DiscussionNoteFields(
                discussionTitle: viewModel.activeSession?.discussions.first { $0.id == discussionId }?.title,
                note: viewModel.discussionNotes[discussionId],
                onSave: { content in viewModel.onSaveDiscussionNote(discussionId: discussionId, content: content) },
                onDelete: { viewModel.onOpenDialog(ClubDetailsDialog.DeleteDiscussionNote(discussionId: discussionId)) }
            )
            .onAppear { viewModel.onLoadDiscussionNote(discussionId: discussionId) }
        }
        // Change role
        .kluvsBottomSheet(
            item: viewModel.sheetItem(ClubDetailsSheet.ChangeRole.self) { $0.memberId },
            header: "Change Role"
        ) { wrapper in
            let member = viewModel.members.first { $0.memberId == wrapper.id }
            ChangeRoleFields(memberName: member?.name ?? "", selectedRole: $selectedRole)
        } footer: { wrapper in
            let memberId = wrapper.id
            let member = viewModel.members.first { $0.memberId == memberId }
            BottomSheetFooter(
                actionLabel: "Save",
                onAction: {
                    if let mid = currentMemberId {
                        viewModel.onUpdateMemberRole(memberId: memberId, currentMemberId: mid, newRole: selectedRole)
                    }
                },
                onCancel: { viewModel.onDismissSheet() },
                actionEnabled: selectedRole != (member?.role ?? .member)
            )
        }
        // ---- Dialogs: applied after every sheet so a dialog opened over a sheet (delete note) draws on top ----
        // Delete club confirmation
        .kluvsConfirmationDialog(
            isPresented: viewModel.isShowing(ClubDetailsDialog.DeleteClub.self),
            title: "Delete Club",
            message: "Are you sure you want to delete \"\(viewModel.clubDetails?.clubName ?? "this club")\"? This action cannot be undone.",
            confirmLabel: "Delete",
            isDestructive: true,
            onDismiss: { viewModel.onDismissDialog() },
            onConfirm: { viewModel.onDeleteClub() }
        )
        // End session confirmation
        .kluvsConfirmationDialog(
            isPresented: viewModel.isShowing(ClubDetailsDialog.EndSession.self),
            title: "End Session",
            message: {
                let readingCount = viewModel.activeSession?.participants.filter { $0.isReading }.count ?? 0
                let creditMessage = readingCount > 0
                    ? "\(readingCount) member\(readingCount != 1 ? "s" : "") will receive credit."
                    : "No members are marked as reading — no credit will be awarded."
                return "Are you sure you want to end the current reading session for \"\(viewModel.activeSession?.book.title ?? "")\"?\n\n\(creditMessage)"
            }(),
            confirmLabel: "Confirm End",
            isDestructive: true,
            onDismiss: { viewModel.onDismissDialog() },
            onConfirm: { viewModel.onEndSession() }
        )
        // Delete discussion confirmation
        .kluvsConfirmationDialog(
            isPresented: viewModel.isShowing(ClubDetailsDialog.DeleteDiscussion.self),
            title: "Delete Discussion",
            message: "Are you sure you want to delete this discussion?",
            confirmLabel: "Delete",
            isDestructive: true,
            onDismiss: { viewModel.onDismissDialog() },
            onConfirm: {
                if let dialog = viewModel.activeDialog as? ClubDetailsDialog.DeleteDiscussion {
                    viewModel.onDeleteDiscussion(discussionId: dialog.discussionId)
                }
            }
        )
        // Delete note confirmation, over the Notes sheet
        .kluvsConfirmationDialog(
            isPresented: viewModel.isShowing(ClubDetailsDialog.DeleteDiscussionNote.self),
            title: "Delete Note",
            message: "Are you sure you want to delete this note?",
            confirmLabel: "Delete",
            isDestructive: true,
            onDismiss: { viewModel.onDismissDialog() },
            onConfirm: {
                if let dialog = viewModel.activeDialog as? ClubDetailsDialog.DeleteDiscussionNote {
                    viewModel.onDeleteDiscussionNote(discussionId: dialog.discussionId)
                }
            }
        )
        // Remove member confirmation
        .kluvsConfirmationDialog(
            isPresented: viewModel.isShowing(ClubDetailsDialog.RemoveMember.self),
            title: "Remove Member",
            message: {
                let memberId = (viewModel.activeDialog as? ClubDetailsDialog.RemoveMember)?.memberId
                let memberName = viewModel.members.first { $0.memberId == memberId }?.name ?? "this member"
                return "Are you sure you want to remove \(memberName) from the club?"
            }(),
            confirmLabel: "Remove",
            isDestructive: true,
            onDismiss: { viewModel.onDismissDialog() },
            onConfirm: {
                // Without our own member ID there's nothing to send; just close the dialog
                if let dialog = viewModel.activeDialog as? ClubDetailsDialog.RemoveMember, let mid = currentMemberId {
                    viewModel.onRemoveMember(memberId: dialog.memberId, currentMemberId: mid)
                } else {
                    viewModel.onDismissDialog()
                }
            }
        )
        // Leave club confirmation
        .kluvsConfirmationDialog(
            isPresented: viewModel.isShowing(ClubDetailsDialog.LeaveClub.self),
            title: "Leave Club",
            message: "Are you sure you want to leave \"\(viewModel.clubDetails?.clubName ?? "this club")\"? You'll need a new invite to rejoin.",
            confirmLabel: "Leave",
            isDestructive: true,
            onDismiss: { viewModel.onDismissDialog() },
            onConfirm: { viewModel.onLeaveClub() }
        )
        // Transfer ownership confirmation
        .kluvsConfirmationDialog(
            isPresented: viewModel.isShowing(ClubDetailsDialog.TransferOwnership.self),
            title: "Transfer Ownership",
            message: {
                let memberId = (viewModel.activeDialog as? ClubDetailsDialog.TransferOwnership)?.memberId
                let memberName = viewModel.members.first { $0.memberId == memberId }?.name ?? "this member"
                let clubName = viewModel.clubDetails?.clubName ?? "this club"
                return "Are you sure you want to make \(memberName) the new owner of \"\(clubName)\"? You'll become an Admin."
            }(),
            confirmLabel: "Transfer",
            isDestructive: true,
            onDismiss: { viewModel.onDismissDialog() },
            onConfirm: {
                if let dialog = viewModel.activeDialog as? ClubDetailsDialog.TransferOwnership {
                    viewModel.onTransferOwnership(newOwnerId: dialog.memberId)
                }
            }
        )
    }
}

// MARK: - Club Meta Row
private struct ClubMetaRow: View {
    let userRole: Shared.Role?
    let foundedYear: String?
    let memberCount: Int

    var body: some View {
        HStack(spacing: 8) {
            if let userRole {
                RoleEyebrow(role: userRole)
                metaDot
            }
            if let foundedYear {
                Text("FOUNDED \(foundedYear)".uppercased())
                    .kluvsStyle(KluvsTheme.typography.eyebrow)
                    .foregroundColor(KluvsTheme.colors.contentMuted)
                metaDot
            }
            Text("\(memberCount) MEMBERS".uppercased())
                .kluvsStyle(KluvsTheme.typography.eyebrow)
                .foregroundColor(KluvsTheme.colors.contentMuted)
        }
    }

    private var metaDot: some View {
        Circle()
            .fill(KluvsTheme.colors.contentMuted)
            .frame(width: 3, height: 3)
    }
}
