import SwiftUI
import Shared

struct CreateSessionSheet: View {
    let onSave: (Shared.Book, String?) -> Void
    let onDismiss: () -> Void

    @State private var bookTitle = ""
    @State private var bookAuthor = ""
    @State private var hasDueDate = false
    @State private var dueDate = Date()

    private var canCreate: Bool {
        !bookTitle.trimmingCharacters(in: .whitespaces).isEmpty &&
        !bookAuthor.trimmingCharacters(in: .whitespaces).isEmpty
    }

    var body: some View {
        NavigationView {
            Form {
                Section("Book") {
                    TextField("Title", text: $bookTitle)
                    TextField("Author", text: $bookAuthor)
                }

                Section("Due Date") {
                    Toggle("Set a due date", isOn: $hasDueDate)
                    if hasDueDate {
                        DatePicker(
                            "Due Date",
                            selection: $dueDate,
                            displayedComponents: [.date, .hourAndMinute]
                        )
                        .datePickerStyle(.compact)
                    }
                }
            }
            .navigationTitle("Create Session")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", action: onDismiss)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Create") {
                        let book = Shared.Book(
                            id: "",
                            title: bookTitle.trimmingCharacters(in: .whitespaces),
                            author: bookAuthor.trimmingCharacters(in: .whitespaces),
                            edition: nil,
                            year: nil,
                            isbn: nil,
                            pageCount: nil,
                            imageUrl: nil,
                            externalGoogleId: nil
                        )
                        let resolvedDueDateIso = hasDueDate ? dueDate.toIsoString() : nil
                        onSave(book, resolvedDueDateIso)
                    }
                    .disabled(!canCreate)
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

#Preview {
    CreateSessionSheet(onSave: { _, _ in }, onDismiss: {})
}
