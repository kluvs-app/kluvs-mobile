import SwiftUI
import Shared
import DesignSystem

/// The one iOS listener for shared `ScreenEffect`s, mirroring Android's `ScreenEffectHost`. A
/// screen attaches it once with `.screenEffects(viewModel.screenEffectSource)` and never handles
/// screen effects itself.
///
/// It listens only while the view is on screen. The ViewModel drops effects sent while nobody
/// listens, so a screen never shows one from before it reappeared.
private struct ScreenEffectsModifier: ViewModifier {
    let source: ScreenEffectSource
    @State private var snackbar: SnackbarData?
    @State private var subscription: Shared.Closeable?

    func body(content: Content) -> some View {
        content
            .snackbar($snackbar)
            .onAppear {
                subscription?.close()
                subscription = source.observeScreenEffects { effect in
                    Task { @MainActor in show(effect) }
                }
            }
            .onDisappear {
                subscription?.close()
                subscription = nil
            }
    }

    @MainActor private func show(_ effect: ScreenEffect) {
        switch effect {
        case let effect as ScreenEffect.ShowSnackbar:
            // A new snackbar replaces the one showing (design-system/docs/states.md § Snackbar)
            snackbar = SnackbarData(message: text(of: effect.message), variant: variant(of: effect.tone))
        default:
            break
        }
    }

    private func text(of message: SnackbarMessage) -> String {
        switch message {
        case let message as SnackbarMessage.Text:
            return message.text
        case let message as SnackbarMessage.Notice:
            switch message.key {
            case .saveSuccess: return String(localized: "save_success")
            case .passwordUpdated: return String(localized: "password_updated")
            default: return ""
            }
        default:
            return ""
        }
    }

    private func variant(of tone: SnackbarTone) -> SnackbarVariant {
        tone == .danger ? .danger : .success
    }
}

extension View {
    /// Shows the shared ViewModel's screen effects (snackbars) while this view is on screen.
    func screenEffects(_ source: ScreenEffectSource) -> some View {
        modifier(ScreenEffectsModifier(source: source))
    }
}
