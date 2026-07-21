import SwiftUI
import Shared

/// The signed-in member's progress on a session book: thin bar, status label,
/// and the entry point to `ReadingProgressSheet`. Shared between the Clubs
/// screen and the Me screen's shelf rows.
struct OwnProgressRow: View {
    let ownProgress: Shared.OwnProgressInfo?
    let onUpdateProgress: () -> Void
    /// Caption shown left of the status label, e.g. "Next · Thu, Dec 31" on
    /// Me screen shelf rows. Defaults to "Your progress" (Clubs convention).
    var leftLabel: String = "Your progress"

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 12) {
                ProgressView(value: Double(ownProgress?.percent ?? 0), total: 100)
                    .tint(.brandOrange)
                GhostButton(text: ownProgress != nil ? "Update" : "Track Progress", onClick: onUpdateProgress)
            }
            HStack {
                Text(leftLabel)
                    .font(.kluvsHelperSm)
                    .foregroundColor(.secondary)
                Spacer()
                Text(ownProgress?.label ?? "Not started")
                    .font(.kluvsHelperSm)
                    .foregroundColor(.brandOrange)
            }
        }
    }
}

#Preview {
    OwnProgressRow(ownProgress: nil, onUpdateProgress: {}, leftLabel: "Next · Thu, Dec 31")
        .padding()
}
