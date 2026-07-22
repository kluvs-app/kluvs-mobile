import SwiftUI
import Shared

private let noteMaxLength = 4000

/// Sheet for viewing, creating, editing, or deleting the signed-in member's
/// note on a discussion.
///
/// A nil `note` means it hasn't finished loading yet. A non-nil `note` with
/// a nil `noteId` means no note exists yet, so the sheet opens straight into
/// an editable/create state.
struct DiscussionNoteSheet: View {
    let note: Shared.DiscussionNoteInfo?
    let onSave: (String) -> Void
    let onDelete: () -> Void
    let onDismiss: () -> Void

    @State private var isEditing: Bool = false
    @State private var content: String = ""
    @State private var showDeleteConfirmation = false
    @State private var lastLoadedNoteId: String??

    var body: some View {
        NavigationView {
            Group {
                if let note {
                    if isEditing {
                        editingView(note: note)
                    } else {
                        viewingView(note: note)
                    }
                } else {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
            .navigationTitle("Note")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close", action: onDismiss)
                }
            }
        }
        .presentationDetents([.medium, .large])
        .onChange(of: note?.noteId) { _, newNoteId in
            // Sync local edit state whenever a distinct note identity loads —
            // avoids clobbering in-progress typing on unrelated state updates
            // (e.g. isSaving flipping) from the same note.
            guard lastLoadedNoteId != .some(newNoteId) else { return }
            lastLoadedNoteId = .some(newNoteId)
            content = note?.content ?? ""
            isEditing = newNoteId == nil
        }
        .alert("Delete Note", isPresented: $showDeleteConfirmation) {
            Button("Delete", role: .destructive, action: onDelete)
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Are you sure you want to delete this note?")
        }
    }

    @ViewBuilder
    private func viewingView(note: Shared.DiscussionNoteInfo) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            ScrollView {
                Text(note.content)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            HStack {
                Button("Edit") { isEditing = true }
                Spacer()
                Button("Delete", role: .destructive) { showDeleteConfirmation = true }
            }
        }
        .padding()
    }

    @ViewBuilder
    private func editingView(note: Shared.DiscussionNoteInfo) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            TextEditor(text: Binding(
                get: { content },
                set: { content = String($0.prefix(noteMaxLength)) }
            ))
            .frame(minHeight: 160)
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(uiColor: .separator)))

            if let error = note.error {
                Text(error)
                    .font(.footnote)
                    .foregroundColor(.red)
            }

            HStack {
                if note.noteId != nil {
                    Button("Cancel") {
                        content = note.content
                        isEditing = false
                    }
                }
                Spacer()
                Button(note.isSaving ? "Saving…" : "Save") {
                    onSave(content.trimmingCharacters(in: .whitespacesAndNewlines))
                }
                .disabled(!canSave(note: note))
            }
        }
        .padding()
    }

    private func canSave(note: Shared.DiscussionNoteInfo) -> Bool {
        let trimmed = content.trimmingCharacters(in: .whitespacesAndNewlines)
        return !trimmed.isEmpty && trimmed != note.content.trimmingCharacters(in: .whitespacesAndNewlines) && !note.isSaving
    }
}

#Preview {
    DiscussionNoteSheet(
        note: Shared.DiscussionNoteInfo(
            noteId: "n1",
            content: "Bring snacks next time and discuss chapter 5.",
            isSaving: false,
            error: nil
        ),
        onSave: { _ in },
        onDelete: {},
        onDismiss: {}
    )
}
