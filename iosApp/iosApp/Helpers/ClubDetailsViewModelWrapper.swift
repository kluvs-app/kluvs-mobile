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
    @Published var members: [Shared.MemberListItemInfo] = []
    @Published var userRole: Shared.Role? = nil
    @Published var isOperationInProgress: Bool = false
    @Published var operationMessage: String? = nil
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
            DispatchQueue.main.async {
                self?.screenState = {
                    if !state.availableClubs.isEmpty { return .content }
                    if state.isLoading { return .loading }
                    if let error = state.error { return .error(error) }
                    return .empty
                }()
                self?.isLoading = state.isLoading
                self?.availableClubs = state.availableClubs
                self?.selectedClubId = state.selectedClubId
                self?.clubDetails = state.currentClubDetails
                self?.activeSession = state.activeSession
                self?.members = state.members
                self?.userRole = state.userRole
                self?.isOperationInProgress = state.isOperationInProgress
                self?.operationMessage = self?.helper.operationResultMessage(result: state.operationResult)
                self?.sessionDueDateIso = self?.helper.localDateTimeToIso(dateTime: state.activeSession?.rawDueDate)
            }
        }
        cancellables.append(stateCancellable)
    }

    func loadUserClubs(userId: String) { helper.loadUserClubs(userId: userId) }
    func loadClubData(clubId: String) { helper.loadClubData(clubId: clubId) }
    func selectClub(clubId: String) { helper.selectClub(clubId: clubId) }
    func refresh() { helper.refresh() }

    // General tab
    func onUpdateClubName(_ newName: String) { helper.onUpdateClubName(newName: newName) }
    func onDeleteClub() { helper.onDeleteClub() }

    // Session tab — dates passed as ISO strings
    func onCreateSession(book: Shared.Book, dueDateIso: String?) {
        helper.onCreateSession(book: book, dueDateIso: dueDateIso)
    }
    func onUpdateSession(book: Shared.Book?, dueDateIso: String?) {
        helper.onUpdateSession(book: book, dueDateIso: dueDateIso)
    }
    func onDeleteSession() { helper.onDeleteSession() }

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
