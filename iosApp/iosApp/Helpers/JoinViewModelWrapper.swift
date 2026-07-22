//
//  JoinViewModelWrapper.swift
//  iosApp
//
import Swift
import Shared

@MainActor
class JoinViewModelWrapper: ObservableObject {
    @Published var tokenInput: String = ""
    @Published var isAuthenticated: Bool = false
    @Published var isLoadingPreview: Bool = false
    @Published var preview: Shared.ClubPreview? = nil
    @Published var previewError: String? = nil
    @Published var isJoining: Bool = false
    @Published var joinedClubId: String? = nil
    @Published var joinError: String? = nil
    @Published var needsSignIn: Bool = false

    private let helper: JoinViewModelHelper
    private var cancellables: [Shared.Closeable] = []

    init() {
        self.helper = JoinViewModelHelper()
        startObserving()
    }

    private func startObserving() {
        let stateCancellable = helper.observeState { [weak self] state in
            Task { @MainActor [weak self] in
                guard let self else { return }
                self.tokenInput = state.tokenInput
                self.isAuthenticated = state.isAuthenticated
                self.isLoadingPreview = state.isLoadingPreview
                self.preview = state.preview
                self.previewError = state.previewError
                self.isJoining = state.isJoining
                self.joinedClubId = state.joinedClubId
                self.joinError = state.joinError
                self.needsSignIn = state.needsSignIn
            }
        }
        cancellables.append(stateCancellable)
    }

    func onTokenChanged(_ token: String) { helper.onTokenChanged(token: token) }
    func previewInvite() { helper.previewInvite() }
    func onJoinClicked() { helper.onJoinClicked() }
    func onConsumeNeedsSignIn() { helper.onConsumeNeedsSignIn() }
    func onConsumeJoinedClubId() { helper.onConsumeJoinedClubId() }
    func onConsumeJoinError() { helper.onConsumeJoinError() }

    deinit {
        cancellables.forEach { $0.close() }
    }
}
