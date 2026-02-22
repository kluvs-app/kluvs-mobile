import SwiftUI

/// Sheet for creating or editing a discussion.
/// Dates are exchanged as ISO-8601 strings (e.g. "2025-01-15T19:00:00") to avoid
/// exposing KMP's LocalDateTime to Swift.
struct DiscussionSheet: View {
    let initialTitle: String
    let initialLocation: String
    /// ISO-8601 string from KMP, or nil when creating a new discussion.
    let initialDateIso: String?
    let onSave: (String, String, String) -> Void
    let onDismiss: () -> Void

    @State private var title: String
    @State private var location: String
    @State private var selectedDate: Date

    private let initialDateAsSwift: Date

    init(
        initialTitle: String = "",
        initialLocation: String = "",
        initialDateIso: String? = nil,
        onSave: @escaping (String, String, String) -> Void,
        onDismiss: @escaping () -> Void
    ) {
        self.initialTitle = initialTitle
        self.initialLocation = initialLocation
        self.initialDateIso = initialDateIso
        self.onSave = onSave
        self.onDismiss = onDismiss
        let startDate = initialDateIso?.toSwiftDate() ?? Date()
        self.initialDateAsSwift = startDate
        _title = State(initialValue: initialTitle)
        _location = State(initialValue: initialLocation)
        _selectedDate = State(initialValue: startDate)
    }

    private var canSave: Bool {
        !title.trimmingCharacters(in: .whitespaces).isEmpty &&
        !location.trimmingCharacters(in: .whitespaces).isEmpty
    }

    private var hasChanges: Bool {
        title.trimmingCharacters(in: .whitespaces) != initialTitle ||
        location.trimmingCharacters(in: .whitespaces) != initialLocation ||
        selectedDate != initialDateAsSwift
    }

    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("Title", text: $title)
                    TextField("Location", text: $location)
                }

                Section("Date & Time") {
                    DatePicker(
                        "Date",
                        selection: $selectedDate,
                        displayedComponents: [.date, .hourAndMinute]
                    )
                    .datePickerStyle(.compact)
                }
            }
            .navigationTitle("Discussion")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", action: onDismiss)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        onSave(
                            title.trimmingCharacters(in: .whitespaces),
                            location.trimmingCharacters(in: .whitespaces),
                            selectedDate.toIsoString()
                        )
                    }
                    .disabled(!canSave || !hasChanges)
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

#Preview {
    DiscussionSheet(
        initialTitle: "Chapter 3 Discussion",
        initialLocation: "Coffee Shop",
        onSave: { _, _, _ in },
        onDismiss: {}
    )
}
