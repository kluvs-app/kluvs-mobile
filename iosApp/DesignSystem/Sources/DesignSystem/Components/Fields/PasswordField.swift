import SwiftUI

/// Password variant of `InputField` — a dedicated component, not a boolean flag on
/// `InputField`, matching this codebase's established convention of separate named components
/// for a categorically different input mode (see `InputField` vs `PickerField`). Masked by
/// default, with a trailing show/hide reveal toggle (design-system `docs/inputs.md`).
///
/// Reveal state is local to each instance (starts masked, not persisted, not hoisted), so every
/// password field in a form toggles independently. Mirrors Android's `PasswordField`.
public struct PasswordField: View {
    let label: String
    @Binding var value: String
    var error: String?
    var helperText: String?
    var enabled: Bool
    var submitLabel: SubmitLabel
    var onSubmit: () -> Void

    @State private var revealed = false
    @FocusState private var focused: Bool

    public init(
        label: String,
        value: Binding<String>,
        error: String? = nil,
        helperText: String? = nil,
        enabled: Bool = true,
        submitLabel: SubmitLabel = .done,
        onSubmit: @escaping () -> Void = {}
    ) {
        self.label = label
        self._value = value
        self.error = error
        self.helperText = helperText
        self.enabled = enabled
        self.submitLabel = submitLabel
        self.onSubmit = onSubmit
    }

    private var borderColor: Color { error != nil ? KluvsTheme.colors.danger : KluvsTheme.colors.divider }
    private var labelColor: Color { error != nil ? KluvsTheme.colors.danger : KluvsTheme.colors.contentMuted }

    @ViewBuilder
    private var inputView: some View {
        if revealed {
            TextField("", text: $value)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
        } else {
            SecureField("", text: $value)
        }
    }

    /// Swapping `SecureField` <-> `TextField` replaces the underlying view, which drops focus —
    /// restore it so the keyboard doesn't disappear mid-edit.
    private func toggleReveal() {
        let wasFocused = focused
        revealed.toggle()
        if wasFocused {
            DispatchQueue.main.async { focused = true }
        }
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .kluvsStyle(KluvsTheme.typography.caption)
                .foregroundColor(labelColor)

            inputView
                .kluvsStyle(KluvsTheme.typography.body.medium)
                .foregroundColor(KluvsTheme.colors.content)
                .tint(KluvsTheme.colors.accent)
                .focused($focused)
                .submitLabel(submitLabel)
                .onSubmit(onSubmit)
                .disabled(!enabled)
                .padding(.trailing, 36)
                // Overlay (not a sibling) so the 44pt tap target doesn't make this field taller
                // than `InputField`; the negative inset lets the glyph sit near the border.
                .overlay(alignment: .trailing) {
                    IconButton(
                        type: revealed ? .visibilityOff : .visibility,
                        contentDescription: revealed ? "Hide password" : "Show password",
                        action: toggleReveal,
                        enabled: enabled,
                        tint: KluvsTheme.colors.contentMuted
                    )
                    .padding(.trailing, -12)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(enabled ? KluvsTheme.colors.card : KluvsTheme.colors.background)
                .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(borderColor, lineWidth: 1))

            if let supporting = error ?? helperText {
                Text(supporting)
                    .kluvsStyle(KluvsTheme.typography.caption)
                    .foregroundColor(error != nil ? KluvsTheme.colors.danger : KluvsTheme.colors.contentMuted)
            }
        }
    }
}

#Preview {
    StatefulFieldPreview(["", ""]) { values in
        VStack(spacing: 12) {
            PasswordField(label: "Password", value: values[0], helperText: "At least 8 characters.")
            PasswordField(label: "Password", value: values[1], error: "Passwords don't match.")
        }
        .padding()
    }
}
