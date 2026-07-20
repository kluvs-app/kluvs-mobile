import SwiftUI
import Shared

/// Sheet for tracking/updating the signed-in member's reading progress on the active
/// session. Mirrors the web app's `ReadingProgressModal` / Android's `ReadingProgressBottomSheet` —
/// Page/Percent toggle, value input, auto "mark as finished" at the end of the book,
/// and a manual finished toggle.
struct ReadingProgressSheet: View {
    let bookTitle: String
    let pageCount: Int32?
    var initialType: Shared.ProgressType = .page
    var initialCurrentPage: Int32? = nil
    var initialPercentComplete: Float? = nil
    var initialMarkFinished: Bool = false
    let onSave: (Shared.ProgressType, Int32?, Int32?, Bool) -> Void
    let onDismiss: () -> Void

    @State private var progressType: Shared.ProgressType
    @State private var currentPageText: String
    @State private var percentText: String
    @State private var markFinished: Bool
    @State private var lastAutoTriggerValue: String?

    init(
        bookTitle: String,
        pageCount: Int32?,
        initialType: Shared.ProgressType = .page,
        initialCurrentPage: Int32? = nil,
        initialPercentComplete: Float? = nil,
        initialMarkFinished: Bool = false,
        onSave: @escaping (Shared.ProgressType, Int32?, Int32?, Bool) -> Void,
        onDismiss: @escaping () -> Void
    ) {
        self.bookTitle = bookTitle
        self.pageCount = pageCount
        self.initialType = initialType
        self.initialCurrentPage = initialCurrentPage
        self.initialPercentComplete = initialPercentComplete
        self.initialMarkFinished = initialMarkFinished
        self.onSave = onSave
        self.onDismiss = onDismiss
        _progressType = State(initialValue: initialType)
        _currentPageText = State(initialValue: initialCurrentPage.map { String($0) } ?? "")
        _percentText = State(initialValue: initialPercentComplete.map(Self.formatPercent) ?? "")
        _markFinished = State(initialValue: initialMarkFinished)
    }

    private var previewPercent: Int? {
        guard progressType == .page, let pageCount, pageCount > 0,
              let page = Int32(currentPageText) else { return nil }
        return Swift.min(100, Int((Float(page) * 100 / Float(pageCount)).rounded()))
    }

    private var canSave: Bool {
        switch progressType {
        case .page: return Int32(currentPageText) != nil
        case .percent: return Float(percentText) != nil
        default: return false
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(initialCurrentPage != nil || initialPercentComplete != nil ? "Update Progress" : "Track Progress")
                .font(.kluvsSectionHeading)

            Text(bookTitle)
                .font(.kluvsBody)
                .foregroundColor(.secondary)

            HStack(spacing: 8) {
                trackByButton(label: "Page", selected: progressType == .page) { progressType = .page }
                trackByButton(label: "Percent", selected: progressType == .percent) { progressType = .percent }
            }

            if progressType == .page {
                VStack(alignment: .leading, spacing: 4) {
                    TextField(
                        pageCount != nil ? "Current Page (of \(pageCount!))" : "Current Page",
                        text: $currentPageText
                    )
                    .keyboardType(.numberPad)
                    .textFieldStyle(.roundedBorder)
                    .onChange(of: currentPageText) { _, newValue in
                        currentPageText = newValue.filter { $0.isNumber }
                        autoToggleFinished(currentPageText)
                    }
                    if let previewPercent {
                        Text("That's about \(previewPercent)% complete.")
                            .font(.kluvsHelperSm)
                            .foregroundColor(.secondary)
                    }
                }
            } else {
                TextField("Percent Complete", text: $percentText)
                    .keyboardType(.decimalPad)
                    .textFieldStyle(.roundedBorder)
                    .onChange(of: percentText) { _, newValue in
                        percentText = newValue.filter { $0.isNumber || $0 == "." }
                        autoToggleFinished(percentText)
                    }
            }

            Toggle("Mark as finished", isOn: $markFinished)
                .font(.kluvsBody)
                .tint(.brandOrange)

            Button(action: save) {
                Text("Save Progress")
                    .font(.kluvsButtonPrimary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(canSave ? Color.brandOrange : Color.brandOrange.opacity(0.4))
                    .foregroundColor(.white)
                    .cornerRadius(12)
            }
            .disabled(!canSave)
        }
        .padding(20)
        .padding(.bottom, 20)
    }

    private func trackByButton(label: String, selected: Bool, onTap: @escaping () -> Void) -> some View {
        Button(action: onTap) {
            Text(label)
                .font(.kluvsButtonSecondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(selected ? Color.brandOrange : Color.clear)
                .foregroundColor(selected ? .white : .primary)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .strokeBorder(selected ? Color.clear : Color(uiColor: .separator), lineWidth: 1)
                )
                .cornerRadius(12)
        }
    }

    private func autoToggleFinished(_ newValue: String) {
        guard newValue != lastAutoTriggerValue else { return }
        let atEnd: Bool
        if progressType == .page, let pageCount, pageCount > 0 {
            atEnd = (Int32(newValue) ?? 0) >= pageCount
        } else if progressType == .percent {
            atEnd = (Float(newValue) ?? 0) >= 100
        } else {
            return
        }
        if atEnd != markFinished {
            markFinished = atEnd
            lastAutoTriggerValue = newValue
        }
    }

    private func save() {
        let page = progressType == .page ? Int32(currentPageText) : nil
        let percent: Int32? = progressType == .percent
            ? Float(percentText).map { Int32(Swift.min(100, Swift.max(0, $0.rounded()))) }
            : nil
        onSave(progressType, page, percent, markFinished)
    }

    private static func formatPercent(_ value: Float) -> String {
        value == value.rounded() ? String(Int(value)) : String(value)
    }
}

#Preview {
    ReadingProgressSheet(
        bookTitle: "1984",
        pageCount: 328,
        initialCurrentPage: 42,
        onSave: { _, _, _, _ in },
        onDismiss: {}
    )
}
