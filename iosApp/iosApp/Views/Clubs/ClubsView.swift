import SwiftUI
import Shared

private struct IDWrapper: Identifiable {
    let id: String
}

struct ClubsView: View {
    let userId: String
    @StateObject private var viewModel = ClubDetailsViewModelWrapper()
    @State private var selectedTab = 0
    @State private var showClubSelector = false

    // Sheet / alert state
    @State private var showEditClubSheet = false
    @State private var showDeleteClubAlert = false
    @State private var showCreateSessionSheet = false
    @State private var showEditSessionSheet = false
    @State private var showCreateDiscussionSheet = false
    @State private var editingDiscussionId: IDWrapper? = nil
    @State private var deletingDiscussionId: String? = nil
    @State private var changingRoleMemberId: IDWrapper? = nil
    @State private var removingMemberId: String? = nil

    /// Returns the current user's memberId from the members list (needed for role/remove calls).
    private var currentMemberId: String? {
        viewModel.members.first { $0.userId == userId }?.memberId
    }

    var body: some View {
        ZStack {
            switch viewModel.screenState {
            case .loading:
                LoadingView()
                    .transition(.opacity)
            case .error(let message):
                ErrorView(message: message, onRetry: {
                    viewModel.loadUserClubs(userId: userId)
                })
                .transition(.opacity)
            case .empty:
                VStack(spacing: 8) {
                    Text(String(localized: "empty_no_clubs"))
                        .font(.title2)
                        .fontWeight(.semibold)

                    Text(String(localized: "empty_no_clubs_hint"))
                        .font(.body)
                        .foregroundColor(.secondary)
                }
                .transition(.opacity)
            case .content:
                VStack(spacing: 0) {
                    // Operation in-progress indicator
                    if viewModel.isOperationInProgress {
                        ProgressView()
                            .progressViewStyle(LinearProgressViewStyle())
                            .tint(.brandOrange)
                    }

                    ClubSelectorRow(
                        clubName: viewModel.clubDetails?.clubName ?? "",
                        hasMultipleClubs: viewModel.availableClubs.count > 1,
                        onTap: { showClubSelector = true }
                    )

                    // Tab selector
                    Picker("", selection: $selectedTab) {
                        Text("tab_general").tag(0)
                        Text("tab_active_session").tag(1)
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
                            GeneralTab(
                                clubDetails: viewModel.clubDetails,
                                userRole: viewModel.userRole,
                                onEditClub: { showEditClubSheet = true },
                                onDeleteClub: { showDeleteClubAlert = true }
                            )
                            .tag(0)

                            ActiveSessionTab(
                                sessionDetails: viewModel.activeSession,
                                userRole: viewModel.userRole,
                                onCreateSession: { showCreateSessionSheet = true },
                                onEditSession: { showEditSessionSheet = true },
                                onCreateDiscussion: { showCreateDiscussionSheet = true },
                                onEditDiscussion: { id in editingDiscussionId = IDWrapper(id: id) },
                                onDeleteDiscussion: { id in deletingDiscussionId = id }
                            )
                            .tag(1)

                            MembersTab(
                                members: viewModel.members,
                                currentUserId: userId,
                                userRole: viewModel.userRole,
                                onChangeRole: { memberId in changingRoleMemberId = IDWrapper(id: memberId) },
                                onRemoveMember: { memberId in removingMemberId = memberId }
                            )
                            .tag(2)
                        }
                        .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
                    }
                }
                .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: viewModel.availableClubs.isEmpty)
        .onAppear {
            viewModel.loadUserClubs(userId: userId)
        }
        // Operation result message
        .alert("Result", isPresented: Binding(
            get: { viewModel.operationMessage != nil },
            set: { if !$0 { viewModel.onConsumeOperationResult() } }
        )) {
            Button("OK") { viewModel.onConsumeOperationResult() }
        } message: {
            if let message = viewModel.operationMessage {
                Text(message)
            }
        }
        // Club selector
        .sheet(isPresented: $showClubSelector) {
            ClubSelectorSheet(
                clubs: viewModel.availableClubs,
                selectedClubId: viewModel.selectedClubId,
                onClubSelected: { clubId in
                    viewModel.selectClub(clubId: clubId)
                }
            )
        }
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

#Preview {
    ClubsView(userId: "1")
}

// MARK: - Club Selector Row
private struct ClubSelectorRow: View {
    let clubName: String
    let hasMultipleClubs: Bool
    let onTap: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            if hasMultipleClubs {
                Image(systemName: "chevron.up.chevron.down")
                    .foregroundColor(.primary)
            }

            Text(clubName)
                .font(.headline)
                .foregroundColor(.primary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .id(clubName)
                .transition(.asymmetric(
                    insertion: .move(edge: .bottom).combined(with: .opacity),
                    removal: .move(edge: .top).combined(with: .opacity)
                ))
                .animation(.easeInOut(duration: 0.3), value: clubName)

            // TODO: Impl once we have club/create feature w/uiux
            // Image(systemName: "plus")
            //     .foregroundColor(.primary)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .contentShape(Rectangle())
        .onTapGesture {
            if hasMultipleClubs {
                onTap()
            }
        }
    }
}
