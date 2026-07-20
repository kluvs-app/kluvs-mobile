import SwiftUI

/// Transient, non-blocking message banner — mirrors Android's `SnackbarHost` for
/// `operationResult` messages. Auto-dismisses; no tap-to-confirm required, unlike `.alert`.
private struct ToastModifier: ViewModifier {
    @Binding var message: String?
    var duration: TimeInterval = 2.0

    func body(content: Content) -> some View {
        content.overlay(alignment: .bottom) {
            if let message {
                Text(message)
                    .font(.kluvsBody)
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(Color.black.opacity(0.85))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.bottom, 24)
                    .padding(.horizontal, 16)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
                            withAnimation { self.message = nil }
                        }
                    }
            }
        }
        .animation(.easeInOut(duration: 0.25), value: message)
    }
}

extension View {
    /// Shows `message` as a transient bottom toast, then clears the binding after `duration`.
    func toast(message: Binding<String?>, duration: TimeInterval = 2.0) -> some View {
        modifier(ToastModifier(message: message, duration: duration))
    }
}
