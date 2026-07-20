import SwiftUI

/// Name-only club creation sheet — mirrors `EditClubSheet` and Android's `CreateClubBottomSheet`.
struct CreateClubSheet: View {
    let onCreate: (String) -> Void
    let onDismiss: () -> Void

    @State private var name: String = ""

    private var trimmedName: String { name.trimmingCharacters(in: .whitespaces) }

    var body: some View {
        NavigationView {
            Form {
                Section {
                    TextField("Club Name", text: $name)
                }
            }
            .navigationTitle("New Club")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", action: onDismiss)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Create") { onCreate(trimmedName) }
                        .disabled(trimmedName.isEmpty)
                }
            }
        }
        .presentationDetents([.medium])
    }
}

#Preview {
    CreateClubSheet(onCreate: { _ in }, onDismiss: {})
}
