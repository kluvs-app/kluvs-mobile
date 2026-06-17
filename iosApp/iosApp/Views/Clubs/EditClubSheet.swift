import SwiftUI

struct EditClubSheet: View {
    let currentName: String
    let onSave: (String) -> Void
    let onDismiss: () -> Void

    @State private var name: String

    init(currentName: String, onSave: @escaping (String) -> Void, onDismiss: @escaping () -> Void) {
        self.currentName = currentName
        self.onSave = onSave
        self.onDismiss = onDismiss
        _name = State(initialValue: currentName)
    }

    private var trimmedName: String { name.trimmingCharacters(in: .whitespaces) }
    private var hasChanges: Bool { trimmedName != currentName }

    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("Club Name", text: $name)
                }
            }
            .navigationTitle("Edit Club Name")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", action: onDismiss)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { onSave(trimmedName) }
                        .disabled(!hasChanges || trimmedName.isEmpty)
                }
            }
        }
        .presentationDetents([.medium])
    }
}

#Preview {
    EditClubSheet(currentName: "My Book Club", onSave: { _ in }, onDismiss: {})
}
