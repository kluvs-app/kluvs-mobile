//
//  ClubDetailsViewModelWrapper.swift
//  iosApp
//
//  Created by Ivan Garza Bermea on 12/4/25.
//
import Swift
import Shared

enum ClubScreenState {
    case loading
    case error(String)
    case empty
    case content
}

@MainActor
class ClubDetailsViewModelWrapper: ObservableObject {
    @Published var screenState: ClubScreenState = .loading
    @Published var isLoading: Bool = false
    @Published var availableClubs: [Shared.ClubListItem] = []
    @Published var selectedClubId: String? = nil
    @Published var clubDetails: Shared.ClubDetails? = nil
    @Published var activeSession: Shared.ActiveSessionDetails? = nil
    @Published var ownProgress: Shared.OwnProgressInfo? = nil
    @Published var members: [Shared.MemberListItemInfo] = []
    @Published var userRole: Shared.Role? = nil
    @Published var discussionRosters: [String: Shared.AttendanceRoster] = [:]
    @Published var discussionNotes: [String: Shared.DiscussionNoteInfo] = [:]
    @Published var isOperationInProgress: Bool = false
    @Published var operationMessage: String? = nil
    @Published var createdClubId: String? = nil
    /// ISO-8601 string for the active session's due date, or nil if none.
    @Published var sessionDueDateIso: String? = nil

    private let helper: ClubDetailsViewModelHelper
    private var cancellables: [Shared.Closeable] = []

    init() {
        self.helper = ClubDetailsViewModelHelper()
        startObserving()
    }

    private func startObserving() {
        let stateCancellable = helper.observeState { [weak self] state in
            Task { @MainActor [weak self] in
                guard let self else { return }
                self.screenState = {
                    if !state.availableClubs.isEmpty { return .content }
                    if state.isLoading { return .loading }
                    if let error = state.error { return .error(error) }
                    return .empty
                }()
                self.isLoading = state.isLoading
                self.availableClubs = state.availableClubs
                self.selectedClubId = state.selectedClubId
                self.clubDetails = state.currentClubDetails
                self.activeSession = state.activeSession
                self.ownProgress = state.ownProgress
                self.members = state.members
                self.userRole = state.userRole
                self.discussionRosters = state.discussionRosters
                self.discussionNotes = state.discussionNotes
                self.isOperationInProgress = state.isOperationInProgress
                self.operationMessage = self.helper.operationResultMessage(result: state.operationResult)
                self.createdClubId = state.createdClubId
                self.sessionDueDateIso = self.helper.localDateTimeToIso(dateTime: state.activeSession?.rawDueDate)
            }
        }
        cancellables.append(stateCancellable)
    }

    func loadUserClubs(userId: String) { helper.loadUserClubs(userId: userId) }
    func loadClubData(clubId: String) { helper.loadClubData(clubId: clubId) }
    func selectClub(clubId: String) { helper.selectClub(clubId: clubId) }
    func refresh() { helper.refresh() }

    func onCreateClub(userId: String, name: String) { helper.onCreateClub(userId: userId, name: name) }
    func onConsumeCreatedClubId() { helper.onConsumeCreatedClubId() }

    // General tab
    func onUpdateClubName(_ newName: String) { helper.onUpdateClubName(newName: newName) }
    func onDeleteClub() { helper.onDeleteClub() }
    func onUpdateJoinPolicy(_ joinPolicy: Shared.JoinPolicy) { helper.onUpdateJoinPolicy(joinPolicy: joinPolicy) }
    func onRotateInviteLink() { helper.onRotateInviteLink() }

    // Session tab — dates passed as ISO strings
    func onCreateSession(book: Shared.Book, dueDateIso: String?) {
        helper.onCreateSession(book: book, dueDateIso: dueDateIso)
    }
    func onUpdateSession(book: Shared.Book?, dueDateIso: String?) {
        helper.onUpdateSession(book: book, dueDateIso: dueDateIso)
    }
    func onDeleteSession() { helper.onDeleteSession() }
    func onEndSession() { helper.onEndSession() }
    func onToggleParticipation(memberId: String, isReading: Bool) {
        helper.onToggleParticipation(memberId: memberId, isReading: isReading)
    }
    /// `percentComplete` is a plain Int (0-100) — the KMP helper converts it to Float internally.
    func onSaveProgress(type: Shared.ProgressType, currentPage: Int32?, percentComplete: Int32?, markFinished: Bool) {
        helper.onSaveProgress(
            type: type,
            currentPage: currentPage.map { KotlinInt(int: $0) },
            percentComplete: percentComplete.map { KotlinInt(int: $0) },
            markFinished: markFinished
        )
    }

    // Discussion operations — dates passed as ISO strings
    func onCreateDiscussion(title: String, location: String, dateIso: String) {
        helper.onCreateDiscussion(title: title, location: location, dateIso: dateIso)
    }
    func onUpdateDiscussion(discussionId: String, title: String?, location: String?, dateIso: String?) {
        helper.onUpdateDiscussion(discussionId: discussionId, title: title, location: location, dateIso: dateIso)
    }
    func onDeleteDiscussion(discussionId: String) { helper.onDeleteDiscussion(discussionId: discussionId) }

    /// Returns the ISO-8601 date string for a specific discussion, read from current KMP state.
    func discussionDateIso(for discussionId: String) -> String? {
        helper.getDiscussionDateIso(discussionId: discussionId)
    }

    // Attendance operations
    func onLoadAttendanceRoster(discussionId: String) {
        helper.onLoadAttendanceRoster(discussionId: discussionId)
    }
    func onSetAttendance(discussionId: String, status: Shared.AttendanceStatus) {
        helper.onSetAttendance(discussionId: discussionId, status: status)
    }

    // Discussion note operations
    func onLoadDiscussionNote(discussionId: String) {
        helper.onLoadDiscussionNote(discussionId: discussionId)
    }
    func onSaveDiscussionNote(discussionId: String, content: String) {
        helper.onSaveDiscussionNote(discussionId: discussionId, content: content)
    }
    func onDeleteDiscussionNote(discussionId: String) {
        helper.onDeleteDiscussionNote(discussionId: discussionId)
    }

    // Member operations
    func onUpdateMemberRole(memberId: String, currentMemberId: String, newRole: Shared.Role) {
        helper.onUpdateMemberRole(memberId: memberId, currentMemberId: currentMemberId, newRole: newRole)
    }
    func onRemoveMember(memberId: String, currentMemberId: String) {
        helper.onRemoveMember(memberId: memberId, currentMemberId: currentMemberId)
    }

    func onConsumeOperationResult() { helper.onConsumeOperationResult() }

    deinit {
        cancellables.forEach { $0.close() }
    }
}
