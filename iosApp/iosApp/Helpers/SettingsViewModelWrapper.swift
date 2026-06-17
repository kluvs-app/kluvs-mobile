//
//  SettingsViewModelWrapper.swift
//  iosApp
//
import Swift
import Shared


@MainActor
class SettingsViewModelWrapper: ObservableObject {
    @Published var isLoading: Bool = true
    @Published var error: String? = nil
    @Published var editedName: String = ""
    @Published var editedHandle: String = ""
    @Published var isSaving: Bool = false
    @Published var saveError: String? = nil
    @Published var saveSuccess: Bool = false
    @Published var hasChanges: Bool = false

    private let helper: SettingsViewModelHelper
    private var cancellables: [Shared.Closeable] = []

    init() {
        self.helper = SettingsViewModelHelper()
        startObserving()
    }

    private func startObserving() {
        let stateCancellable = helper.observeState { [weak self] state in
            DispatchQueue.main.async {
                self?.isLoading = state.isLoading
                self?.error = state.error
                self?.editedName = state.editedName
                self?.editedHandle = state.editedHandle
                self?.isSaving = state.isSaving
                self?.saveError = state.saveError
                self?.saveSuccess = state.saveSuccess
                self?.hasChanges = state.hasChanges
            }
        }
        cancellables.append(stateCancellable)
    }

    func loadProfile(userId: String) {
        helper.loadProfile(userId: userId)
    }

    func onNameChanged(_ name: String) {
        helper.onNameChanged(name: name)
    }

    func onHandleChanged(_ handle: String) {
        helper.onHandleChanged(handle: handle)
    }

    func onSaveProfile() {
        helper.onSaveProfile()
    }

    func onDismissSaveSuccess() {
        helper.onDismissSaveSuccess()
    }

    deinit {
        cancellables.forEach { $0.close() }
    }
}
