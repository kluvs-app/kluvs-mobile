import SwiftUI
import Shared

struct EditSessionSheet: View {
    let currentBook: Shared.BookInfo?
    /// ISO-8601 string from KMP, or nil if no due date is set.
    let initialDueDateIso: String?
    let onSave: (Shared.Book?, String?) -> Void
    let onDismiss: () -> Void

    @State private var bookTitle: String
    @State private var bookAuthor: String
    @State private var hasDueDate: Bool
    @State private var dueDate: Date

    private let initialBookTitle: String
    private let initialBookAuthor: String
    private let initialHasDueDate: Bool
    private let initialDueDateAsSwift: Date

    init(
        currentBook: Shared.BookInfo?,
        initialDueDateIso: String?,
        onSave: @escaping (Shared.Book?, String?) -> Void,
        onDismiss: @escaping () -> Void
    ) {
        self.currentBook = currentBook
        self.initialDueDateIso = initialDueDateIso
        self.onSave = onSave
        self.onDismiss = onDismiss
        let title = currentBook?.title ?? ""
        let author = currentBook?.author ?? ""
        let hasDate = initialDueDateIso != nil
        let date = initialDueDateIso?.toSwiftDate() ?? Date()
        self.initialBookTitle = title
        self.initialBookAuthor = author
        self.initialHasDueDate = hasDate
        self.initialDueDateAsSwift = date
        _bookTitle = State(initialValue: title)
        _bookAuthor = State(initialValue: author)
        _hasDueDate = State(initialValue: hasDate)
        _dueDate = State(initialValue: date)
    }

    private var hasChanges: Bool {
        bookTitle.trimmingCharacters(in: .whitespaces) != initialBookTitle ||
        bookAuthor.trimmingCharacters(in: .whitespaces) != initialBookAuthor ||
        hasDueDate != initialHasDueDate ||
        (hasDueDate && dueDate != initialDueDateAsSwift)
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
            .navigationTitle("Edit Session")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", action: onDismiss)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        let trimmedTitle = bookTitle.trimmingCharacters(in: .whitespaces)
                        let trimmedAuthor = bookAuthor.trimmingCharacters(in: .whitespaces)
                        let book: Shared.Book? = (!trimmedTitle.isEmpty && !trimmedAuthor.isEmpty)
                            ? Shared.Book(
                                id: "",
                                title: trimmedTitle,
                                author: trimmedAuthor,
                                edition: nil,
                                year: nil,
                                isbn: nil,
                                pageCount: nil,
                                imageUrl: nil,
                                externalGoogleId: nil
                            )
                            : nil
                        let resolvedDueDateIso = hasDueDate ? dueDate.toIsoString() : nil
                        onSave(book, resolvedDueDateIso)
                    }
                    .disabled(!hasChanges)
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

#Preview {
    EditSessionSheet(
        currentBook: nil,
        initialDueDateIso: nil,
        onSave: { _, _ in },
        onDismiss: {}
    )
}
