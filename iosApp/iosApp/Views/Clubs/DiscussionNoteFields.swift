import SwiftUI
import Shared
import DesignSystem

private let noteMaxLength = 4000

/// Discussion note sheet body — presented via `.kluvsBottomSheet` at the call site (`ClubsView`),
/// not as its own `View` wrapping a `.sheet`.
///
/// A nil `note` means it hasn't finished loading yet. A non-nil `note` with
/// a nil `noteId` means no note exists yet, so the sheet opens straight into
/// an editable/create state.
///
/// `onDelete` fires when Delete is tapped; the caller asks for confirmation before deleting.
///
/// No web equivalent exists for this one (mobile-only feature) — nothing to check against.
///
/// Deliberately renders its own Edit/Delete or Cancel/Save button row as part of its content,
/// rather than using `BottomSheetFooter`'s slot: this component has two distinct modes (viewing,
/// editing) with different action pairs depending on internal state, not the fixed Cancel/Action
/// shape `BottomSheetFooter` assumes — forcing it into that slot would mean lifting this
/// component's editing state up to the call site for no real benefit.
struct DiscussionNoteFields: View {
    var discussionTitle: String? = nil
    let note: Shared.DiscussionNoteInfo?
    let onSave: (String) -> Void
    let onDelete: () -> Void

    @State private var isEditing: Bool = false
    @State private var content: String = ""
    @State private var lastLoadedNoteId: String??

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            if let discussionTitle {
                VStack(alignment: .leading, spacing: 4) {
                    Text("DISCUSSION")
                        .kluvsStyle(KluvsTheme.typography.eyebrow)
                        .foregroundColor(KluvsTheme.colors.contentMuted)
                    Text(discussionTitle)
                        .kluvsStyle(KluvsTheme.typography.body.medium)
                        .foregroundColor(KluvsTheme.colors.content)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(KluvsTheme.colors.cardAlt)
                .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(KluvsTheme.colors.divider, lineWidth: 1))
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }

            Group {
                if let note {
                    if isEditing {
                        editingView(note: note)
                    } else {
                        viewingView(note: note)
                    }
                } else {
                    ProgressView()
                        .frame(maxWidth: .infinity, minHeight: 120)
                }
            }
        }
        .onChange(of: note?.noteId) { _, newNoteId in
            // Sync local edit state whenever a distinct note identity loads —
            // avoids clobbering in-progress typing on unrelated state updates
            // (e.g. isSaving flipping) from the same note.
            guard lastLoadedNoteId != .some(newNoteId) else { return }
            lastLoadedNoteId = .some(newNoteId)
            content = note?.content ?? ""
            isEditing = newNoteId == nil
        }
    }

    @ViewBuilder
    private func viewingView(note: Shared.DiscussionNoteInfo) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(note.content)
                .kluvsStyle(KluvsTheme.typography.body.medium)
                .foregroundColor(KluvsTheme.colors.content)
                .frame(maxWidth: .infinity, alignment: .leading)

            HStack {
                TextButton(text: "Edit", action: { isEditing = true })
                Spacer()
                TextButton(text: "Delete", action: onDelete)
            }
        }
    }

    @ViewBuilder
    private func editingView(note: Shared.DiscussionNoteInfo) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            TextEditor(text: Binding(
                get: { content },
                set: { content = String($0.prefix(noteMaxLength)) }
            ))
            .kluvsStyle(KluvsTheme.typography.body.medium)
            .frame(minHeight: 160)
            .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(KluvsTheme.colors.divider, lineWidth: 1))

            if let error = note.error {
                Text(error)
                    .kluvsStyle(KluvsTheme.typography.caption)
                    .foregroundColor(KluvsTheme.colors.danger)
            }

            HStack {
                if note.noteId != nil {
                    TextButton(text: "Cancel", action: {
                        content = note.content
                        isEditing = false
                    })
                }
                Spacer()
                PrimaryButton(
                    text: note.isSaving ? "Saving…" : "Save",
                    action: { onSave(content.trimmingCharacters(in: .whitespacesAndNewlines)) },
                    enabled: canSave(note: note)
                )
            }
        }
    }

    private func canSave(note: Shared.DiscussionNoteInfo) -> Bool {
        let trimmed = content.trimmingCharacters(in: .whitespacesAndNewlines)
        return !trimmed.isEmpty && trimmed != note.content.trimmingCharacters(in: .whitespacesAndNewlines) && !note.isSaving
    }
}

#Preview {
    DiscussionNoteFields(
        discussionTitle: "Chapters 5-8",
        note: Shared.DiscussionNoteInfo(
            noteId: "n1",
            content: "Bring snacks next time and discuss chapter 5.",
            isSaving: false,
            error: nil
        ),
        onSave: { _ in },
        onDelete: {}
    )
    .padding()
    .background(KluvsTheme.colors.bar)
}
