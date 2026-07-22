import SwiftUI
import Shared

private struct IDWrapper: Identifiable {
    let id: String
}

/// Entry point for the Clubs tab: owns list -> detail navigation (mirrors web's
/// `/clubs` -> `/clubs/:id`) and the single `ClubDetailsViewModelWrapper` shared
/// across both, matching Android's `ClubsScreen`.
struct ClubsView: View {
    let userId: String
    @StateObject private var viewModel = ClubDetailsViewModelWrapper()
    @State private var path = NavigationPath()
    @State private var showCreateClubSheet = false

    var body: some View {
        NavigationStack(path: $path) {
            Group {
                switch viewModel.screenState {
                case .loading:
                    LoadingView()
                case .error(let message):
                    ErrorView(message: message, onRetry: {
                        viewModel.loadUserClubs(userId: userId)
                    })
                case .empty:
                    VStack(spacing: 8) {
                        Text(String(localized: "empty_no_clubs"))
                            .font(.kluvsSectionHeading)
                        Text(String(localized: "empty_no_clubs_hint"))
                            .font(.kluvsBody)
                            .foregroundColor(.secondary)
                    }
                case .content:
                    ClubsListView(
                        clubs: viewModel.availableClubs,
                        onClubSelected: { clubId in
                            viewModel.selectClub(clubId: clubId)
                            path.append(clubId)
                        },
                        onAddClub: { showCreateClubSheet = true }
                    )
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
                path.append(clubId)
                viewModel.onConsumeCreatedClubId()
            }
        }
        .sheet(isPresented: $showCreateClubSheet) {
            CreateClubSheet(
                onCreate: { name in
                    viewModel.onCreateClub(userId: userId, name: name)
                    showCreateClubSheet = false
                },
                onDismiss: { showCreateClubSheet = false }
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

    // Sheet / alert state
    @State private var showEditClubSheet = false
    @State private var showDeleteClubAlert = false
    @State private var showCreateSessionSheet = false
    @State private var showEditSessionSheet = false
    @State private var showEndSessionAlert = false
    @State private var showProgressSheet = false
    @State private var showCreateDiscussionSheet = false
    @State private var editingDiscussionId: IDWrapper? = nil
    @State private var deletingDiscussionId: String? = nil
    @State private var openNoteDiscussionId: IDWrapper? = nil
    @State private var changingRoleMemberId: IDWrapper? = nil
    @State private var removingMemberId: String? = nil

    /// Returns the current user's memberId from the members list (needed for role/remove calls).
    private var currentMemberId: String? {
        viewModel.members.first { $0.userId == userId }?.memberId
    }

    var body: some View {
        VStack(spacing: 0) {
            // Operation in-progress indicator
            if viewModel.isOperationInProgress {
                ProgressView()
                    .progressViewStyle(LinearProgressViewStyle())
                    .tint(.brandOrange)
            }

            // Back row: chevron + "CLUB" eyebrow, mirrors Android's masthead back row
            HStack(spacing: 4) {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .foregroundColor(.primary)
                }
                Text("CLUB")
                    .font(.kluvsEyebrow)
                    .foregroundColor(.secondary)
                Spacer()
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 12)
            .padding(.vertical, 8)

            // Masthead: club name + role/founded/member-count meta row, + owner overflow
            if let clubDetails = viewModel.clubDetails {
                VStack(alignment: .leading, spacing: 12) {
                    Text(clubDetails.clubName)
                        .font(.kluvsPageHeading)
                        .foregroundColor(.primary)

                    HStack(alignment: .top) {
                        ClubMetaRow(
                            userRole: viewModel.userRole,
                            foundedYear: clubDetails.foundedYear,
                            memberCount: Int(clubDetails.memberCount)
                        )
                        Spacer()
                        if viewModel.userRole == .owner {
                            Menu {
                                Button("Edit") { showEditClubSheet = true }
                                Button("Delete", role: .destructive) { showDeleteClubAlert = true }
                            } label: {
                                Image(systemName: "ellipsis")
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 8)
            }

            // Tab selector
            Picker("", selection: $selectedTab) {
                Text("tab_general").tag(0)
                Text("tab_discussions").tag(1)
                Text("tab_members").tag(2)
            }
            .pickerStyle(SegmentedPickerStyle())
            .tint(.brandOrange)
            .padding(.horizontal)
            .padding(.top, 8)

            // Tab content
            if viewModel.isLoading {
                Spacer()
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
                    .scaleEffect(1.5)
                Spacer()
            } else {
                TabView(selection: $selectedTab) {
                    OverviewTab(
                        clubDetails: viewModel.clubDetails,
                        sessionDetails: viewModel.activeSession,
                        ownProgress: viewModel.ownProgress,
                        userRole: viewModel.userRole,
                        members: viewModel.members,
                        currentUserId: userId,
                        onEditSession: { showEditSessionSheet = true },
                        onEndSession: { showEndSessionAlert = true },
                        onUpdateProgress: { showProgressSheet = true },
                        onCreateSession: { showCreateSessionSheet = true },
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
                        onCreateSession: { showCreateSessionSheet = true },
                        onCreateDiscussion: { showCreateDiscussionSheet = true },
                        onEditDiscussion: { id in editingDiscussionId = IDWrapper(id: id) },
                        onDeleteDiscussion: { id in deletingDiscussionId = id },
                        onOpenNote: { id in openNoteDiscussionId = IDWrapper(id: id) },
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
                        onChangeRole: { memberId in changingRoleMemberId = IDWrapper(id: memberId) },
                        onRemoveMember: { memberId in removingMemberId = memberId }
                    )
                    .tag(2)
                }
                .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
                .background(Color.kluvsBackground)
            }
        }
        .background(Color.kluvsBackground)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.hidden, for: .navigationBar)
        // Operation result message — transient toast, not a blocking alert (matches
        // Android's Snackbar; a tap-to-dismiss dialog on every read-toggle was obnoxious)
        .toast(message: Binding(
            get: { viewModel.operationMessage },
            set: { if $0 == nil { viewModel.onConsumeOperationResult() } }
        ))
        // Edit club name
        .sheet(isPresented: $showEditClubSheet) {
            EditClubSheet(
                currentName: viewModel.clubDetails?.clubName ?? "",
                onSave: { newName in
                    viewModel.onUpdateClubName(newName)
                    showEditClubSheet = false
                },
                onDismiss: { showEditClubSheet = false }
            )
        }
        // Delete club confirmation
        .alert("Delete Club", isPresented: $showDeleteClubAlert) {
            Button("Delete", role: .destructive) { viewModel.onDeleteClub() }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Are you sure you want to delete \"\(viewModel.clubDetails?.clubName ?? "this club")\"? This action cannot be undone.")
        }
        // Create session
        .sheet(isPresented: $showCreateSessionSheet) {
            CreateSessionSheet(
                onSave: { book, dueDateIso in
                    viewModel.onCreateSession(book: book, dueDateIso: dueDateIso)
                    showCreateSessionSheet = false
                },
                onDismiss: { showCreateSessionSheet = false }
            )
        }
        // Edit session
        .sheet(isPresented: $showEditSessionSheet) {
            EditSessionSheet(
                currentBook: viewModel.activeSession?.book,
                initialDueDateIso: viewModel.sessionDueDateIso,
                onSave: { book, dueDateIso in
                    viewModel.onUpdateSession(book: book, dueDateIso: dueDateIso)
                    showEditSessionSheet = false
                },
                onDismiss: { showEditSessionSheet = false }
            )
        }
        // End session confirmation
        .alert("End Session", isPresented: $showEndSessionAlert) {
            Button("Confirm End", role: .destructive) { viewModel.onEndSession() }
            Button("Cancel", role: .cancel) {}
        } message: {
            let readingCount = viewModel.activeSession?.participants.filter { $0.isReading }.count ?? 0
            let creditMessage = readingCount > 0
                ? "\(readingCount) member\(readingCount != 1 ? "s" : "") will receive credit."
                : "No members are marked as reading — no credit will be awarded."
            Text("Are you sure you want to end the current reading session for \"\(viewModel.activeSession?.book.title ?? "")\"?\n\n\(creditMessage)")
        }
        // Update reading progress
        .sheet(isPresented: $showProgressSheet) {
            if let session = viewModel.activeSession {
                ReadingProgressSheet(
                    bookTitle: session.book.title,
                    pageCount: session.book.pageCount?.int32Value,
                    initialType: viewModel.ownProgress?.type ?? .page,
                    initialCurrentPage: viewModel.ownProgress?.currentPage?.int32Value,
                    initialPercentComplete: viewModel.ownProgress?.percentComplete?.floatValue,
                    initialMarkFinished: viewModel.ownProgress?.isCompleted ?? false,
                    onSave: { type, currentPage, percentComplete, markFinished in
                        viewModel.onSaveProgress(type: type, currentPage: currentPage, percentComplete: percentComplete, markFinished: markFinished)
                        showProgressSheet = false
                    },
                    onDismiss: { showProgressSheet = false }
                )
                .presentationDetents([.medium])
                .presentationDragIndicator(.visible)
            }
        }
        // Create discussion
        .sheet(isPresented: $showCreateDiscussionSheet) {
            DiscussionSheet(
                onSave: { title, location, dateIso in
                    viewModel.onCreateDiscussion(title: title, location: location, dateIso: dateIso)
                    showCreateDiscussionSheet = false
                },
                onDismiss: { showCreateDiscussionSheet = false }
            )
        }
        // Edit discussion
        .sheet(item: $editingDiscussionId) { wrapper in
            let discussionId = wrapper.id
            let discussion = viewModel.activeSession?.discussions.first { $0.id == discussionId }
            DiscussionSheet(
                initialTitle: discussion?.title ?? "",
                initialLocation: discussion?.location ?? "",
                initialDateIso: viewModel.discussionDateIso(for: discussionId),
                onSave: { title, location, dateIso in
                    viewModel.onUpdateDiscussion(discussionId: discussionId, title: title, location: location, dateIso: dateIso)
                    editingDiscussionId = nil
                },
                onDismiss: { editingDiscussionId = nil }
            )
        }
        // Delete discussion confirmation
        .alert("Delete Discussion", isPresented: Binding(
            get: { deletingDiscussionId != nil },
            set: { if !$0 { deletingDiscussionId = nil } }
        )) {
            Button("Delete", role: .destructive) {
                if let id = deletingDiscussionId {
                    viewModel.onDeleteDiscussion(discussionId: id)
                }
                deletingDiscussionId = nil
            }
            Button("Cancel", role: .cancel) { deletingDiscussionId = nil }
        } message: {
            Text("Are you sure you want to delete this discussion?")
        }
        // Discussion note
        .sheet(item: $openNoteDiscussionId) { wrapper in
            let discussionId = wrapper.id
            DiscussionNoteSheet(
                note: viewModel.discussionNotes[discussionId],
                onSave: { content in viewModel.onSaveDiscussionNote(discussionId: discussionId, content: content) },
                onDelete: {
                    viewModel.onDeleteDiscussionNote(discussionId: discussionId)
                    openNoteDiscussionId = nil
                },
                onDismiss: { openNoteDiscussionId = nil }
            )
            .onAppear { viewModel.onLoadDiscussionNote(discussionId: discussionId) }
        }
        // Change role
        .sheet(item: $changingRoleMemberId) { wrapper in
            let memberId = wrapper.id
            let member = viewModel.members.first { $0.memberId == memberId }
            ChangeRoleSheet(
                memberName: member?.name ?? "",
                currentRole: member?.role ?? .member,
                onSave: { newRole in
                    if let mid = currentMemberId {
                        viewModel.onUpdateMemberRole(memberId: memberId, currentMemberId: mid, newRole: newRole)
                    }
                    changingRoleMemberId = nil
                },
                onDismiss: { changingRoleMemberId = nil }
            )
        }
        // Remove member confirmation
        .alert("Remove Member", isPresented: Binding(
            get: { removingMemberId != nil },
            set: { if !$0 { removingMemberId = nil } }
        )) {
            Button("Remove", role: .destructive) {
                if let memberId = removingMemberId, let mid = currentMemberId {
                    viewModel.onRemoveMember(memberId: memberId, currentMemberId: mid)
                }
                removingMemberId = nil
            }
            Button("Cancel", role: .cancel) { removingMemberId = nil }
        } message: {
            let memberName = viewModel.members.first { $0.memberId == removingMemberId }?.name ?? "this member"
            Text("Are you sure you want to remove \(memberName) from the club?")
        }
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
                    .font(.plexSansMedium(size: 11))
                    .foregroundColor(.secondary)
                metaDot
            }
            Text("\(memberCount) MEMBERS".uppercased())
                .font(.plexSansMedium(size: 11))
                .foregroundColor(.secondary)
        }
    }

    private var metaDot: some View {
        Circle()
            .fill(Color.secondary)
            .frame(width: 3, height: 3)
    }
}
