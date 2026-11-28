import SwiftUI
import Shared
import DesignSystem

struct MeView: View {
    let userId: String
    @StateObject private var viewModel = MeViewModelWrapper()
    @State private var showSettings = false
    @State private var progressType: Shared.ProgressType = .page
    @State private var progressCurrentPageText = ""
    @State private var progressPercentText = ""
    @State private var progressMarkFinished = false
    @State private var progressLastAutoTriggerValue: String? = nil
    @State private var progressSheetHeader = "Track Progress"

    /// The shelf item whose progress sheet is open, from the shared ViewModel's `activeSheet`.
    private var editingShelfItem: Binding<Shared.ShelfItem?> {
        Binding(
            get: {
                guard let sheet = viewModel.activeSheet as? MeSheet.UpdateProgress else { return nil }
                return viewModel.shelf.first { $0.sessionId == sheet.sessionId }
            },
            set: { if $0 == nil { viewModel.onDismissSheet() } }
        )
    }

    var body: some View {
        VStack(spacing: 0) {
            MeTopBar(
                onReadingLogClick: { viewModel.onOpenSheet(MeSheet.ReadingLog.shared) },
                onSettingsClick: { showSettings = true },
                onSignOutClick: { viewModel.onOpenDialog(MeDialog.SignOut.shared) }
            )

            ZStack {
                if viewModel.isLoading {
                    MeScreenSkeleton()
                        .transition(.opacity)
                } else if let error = viewModel.error {
                    ErrorView(message: error, onRetry: {
                        viewModel.loadUserData(userId: userId)
                    })
                    .transition(.opacity)
                }
                // statistics.clubsCount (already the source of the "4 CLUBS" stat above) is the
                // real "no clubs" signal — nil means stats haven't loaded or failed to load,
                // not that the count is zero, so it deliberately does NOT count as empty here.
                // Mirrors Android's MeScreenContent. The empty case skips ScrollView entirely
                // (rather than just adding a frame inside it) — a ScrollView also proposes
                // unbounded height to its content, so the EmptyState could never actually fill
                // the remaining space if it stayed nested in one.
                else if viewModel.statistics?.clubsCount == 0 {
                    VStack(spacing: 0) {
                        if let profile = viewModel.profile {
                            ProfileSection(profile: profile)
                        }

                        Divider()
                            .padding(.vertical, 8)

                        if let statistics = viewModel.statistics {
                            StatisticsSection(statistics: statistics, joinDate: viewModel.profile?.joinDate)

                            Divider()
                                .padding(.vertical, 8)
                        }

                        EmptyState(
                            heading: "Nothing to track yet.",
                            body: "Join or start a club and your next read will show up here."
                        )
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    }
                    .padding(.horizontal, 16)
                    .transition(.opacity)
                } else {
                    ScrollView {
                        VStack(spacing: 0) {
                            if let profile = viewModel.profile {
                                ProfileSection(profile: profile)
                            }

                            Divider()
                                .padding(.vertical, 8)

                            if let statistics = viewModel.statistics {
                                StatisticsSection(statistics: statistics, joinDate: viewModel.profile?.joinDate)

                                Divider()
                                    .padding(.vertical, 8)
                            }

                            if viewModel.upNext != nil {
                                UpNextSection(upNext: viewModel.upNext)

                                Divider()
                                    .padding(.vertical, 8)
                            }

                            ShelfSection(
                                shelf: viewModel.shelf,
                                onUpdateProgress: { sessionId in
                                    let item = viewModel.shelf.first { $0.sessionId == sessionId }
                                    progressType = item?.ownProgress?.type ?? .page
                                    let currentPage: Int32? = item?.ownProgress?.currentPage?.int32Value
                                    progressCurrentPageText = currentPage.map { String($0) } ?? ""
                                    let percentComplete: Float? = item?.ownProgress?.percentComplete?.floatValue
                                    progressPercentText = percentComplete.map { formatPercent($0) } ?? ""
                                    progressMarkFinished = item?.ownProgress?.isCompleted ?? false
                                    progressLastAutoTriggerValue = nil
                                    progressSheetHeader = item?.ownProgress != nil ? "Update Progress" : "Track Progress"
                                    viewModel.onOpenSheet(MeSheet.UpdateProgress(sessionId: sessionId))
                                }
                            )
                        }
                        .padding(.horizontal, 16)
                    }
                    .kluvsPullToRefresh(isRefreshing: viewModel.isLoading) {
                        viewModel.refresh(forceRefresh: true)
                    }
                    .transition(.opacity)
                }
            }
            .animation(.easeInOut(duration: 0.3), value: viewModel.isLoading)
            .animation(.easeInOut(duration: 0.3), value: viewModel.error)
        }
        .overlay(alignment: .bottom) {
            if let snackbarError = viewModel.snackbarError {
                SnackbarView(message: snackbarError) {
                    viewModel.clearSnackbarError()
                }
                .padding()
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: viewModel.snackbarError)
        .onAppear {
            viewModel.loadUserData(userId: userId)
        }
        .kluvsConfirmationDialog(
            isPresented: Binding(get: { viewModel.activeDialog == MeDialog.SignOut.shared }, set: { _ in }),
            title: NSLocalizedString("logout_confirmation_title", comment: ""),
            message: NSLocalizedString("logout_confirmation_message", comment: ""),
            confirmLabel: NSLocalizedString("yes", comment: ""),
            dismissLabel: NSLocalizedString("no", comment: ""),
            isDestructive: true,
            onDismiss: { viewModel.onDismissDialog() },
            onConfirm: { viewModel.onSignOutDialogConfirmed() }
        )
        .sheet(isPresented: $showSettings) {
            NavigationStack {
                SettingsView(userId: userId)
            }
        }
        .kluvsBottomSheet(item: editingShelfItem, header: progressSheetHeader) { item in
            ReadingProgressFields(
                bookTitle: item.bookTitle,
                pageCount: item.bookPageCount?.int32Value,
                progressType: $progressType,
                currentPageText: $progressCurrentPageText,
                percentText: $progressPercentText,
                markFinished: $progressMarkFinished,
                lastAutoTriggerValue: $progressLastAutoTriggerValue
            )
        } footer: { item in
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
                    viewModel.onSaveProgress(
                        sessionId: item.sessionId,
                        type: progressType,
                        currentPage: page,
                        percentComplete: percent,
                        markFinished: progressMarkFinished
                    )
                },
                onCancel: { viewModel.onDismissSheet() },
                actionEnabled: canSave
            )
        }
        .kluvsBottomSheet(
            isPresented: Binding(
                get: { viewModel.activeSheet == MeSheet.ReadingLog.shared },
                set: { if !$0 { viewModel.onDismissSheet() } }
            ),
            header: "Reading Log"
        ) {
            ReadingLogFields(log: viewModel.readingLog, isLoading: viewModel.isReadingLogLoading)
        }
    }
}

extension Shared.ShelfItem: @retroactive Identifiable {
    public var id: String { sessionId }
}

// MARK: - Profile Section
// Read-only — editing (including the avatar image) happens in Settings.
struct ProfileSection: View {
    let profile: Shared.UserProfile

    var body: some View {
        HStack(alignment: .center, spacing: 16) {
            MemberAvatar(
                avatarUrl: profile.avatarUrl,
                size: 64,
                name: profile.name,
                isLoading: false,
                onClick: nil
            )

            VStack(alignment: .leading, spacing: 4) {
                Text(profile.name)
                    .kluvsStyle(KluvsTheme.typography.headline.small)
                    .foregroundColor(KluvsTheme.colors.content)

                Text("@\(profile.handle ?? "")")
                    .kluvsStyle(KluvsTheme.typography.body.medium)
                    .foregroundColor(KluvsTheme.colors.contentMuted)
            }

            Spacer()
        }
        // Not `.padding()` — the outer VStack in `MeView` already applies horizontal padding to
        // every section; adding it again here doubled it (16 + 16), leaving this section visibly
        // more indented than StatisticsSection right below it, which has none of its own.
        .padding(.vertical, 16)
    }
}

// MARK: - Snackbar View
struct SnackbarView: View {
    let message: String
    let onDismiss: () -> Void

    var body: some View {
        HStack {
            Text(message)
                .font(.body)
                .foregroundColor(.white)
                .lineLimit(2)

            Spacer()

            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .foregroundColor(.white)
            }
        }
        .padding()
        .background(KluvsTheme.colors.danger.opacity(0.9))
        .cornerRadius(8)
        .shadow(radius: 4)
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 5) {
                onDismiss()
            }
        }
    }
}

#Preview {
    MeView(userId: "1")
}
