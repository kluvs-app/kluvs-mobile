import SwiftUI
import UIKit

/// Forces light (white) status-bar icons while `enabled`, e.g. over the dark-red offline banner.
///
/// SwiftUI has no way to do this for us: every screen hides its navigation bar, so
/// `toolbarColorScheme` has no effect, and `preferredColorScheme` would flip the whole app
/// (keyboard, sheets, alerts) just to recolor four icons. The status bar style is decided by the
/// window's root view controller, so this overrides `preferredStatusBarStyle` on that controller's
/// class at runtime (once) and asks it to refresh whenever `enabled` changes. While not enabled the
/// original behavior is untouched.
///
/// If the override ever stops taking effect the failure is soft: the banner is still shown, only
/// the icons stay in the system's choice (dark on the light theme).
extension View {
    func lightStatusBar(_ enabled: Bool) -> some View {
        background(LightStatusBarBridge(enabled: enabled))
            .onDisappear { LightStatusBarOverride.shared.set(false) }
    }
}

private struct LightStatusBarBridge: UIViewRepresentable {
    let enabled: Bool

    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        view.isUserInteractionEnabled = false
        view.alpha = 0
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        // The view is not in a window yet during the first pass; defer so it can be found.
        DispatchQueue.main.async {
            guard let root = uiView.window?.rootViewController else { return }
            LightStatusBarOverride.shared.install(on: root)
            LightStatusBarOverride.shared.set(enabled)
        }
    }
}

final class LightStatusBarOverride {
    static let shared = LightStatusBarOverride()

    private var forceLight = false
    private var installedClasses = Set<ObjectIdentifier>()
    private weak var controller: UIViewController?

    func set(_ enabled: Bool) {
        guard forceLight != enabled else { return }
        forceLight = enabled
        guard let controller else { return }
        UIView.animate(withDuration: 0.25) {
            controller.setNeedsStatusBarAppearanceUpdate()
        }
    }

    func install(on root: UIViewController) {
        controller = root
        let cls: AnyClass = type(of: root)
        guard installedClasses.insert(ObjectIdentifier(cls)).inserted else { return }

        let selector = #selector(getter: UIViewController.preferredStatusBarStyle)
        guard let method = class_getInstanceMethod(cls, selector) else { return }

        typealias Original = @convention(c) (AnyObject, Selector) -> UIStatusBarStyle
        let original = unsafeBitCast(method_getImplementation(method), to: Original.self)
        let override: @convention(block) (AnyObject) -> UIStatusBarStyle = { [weak self] object in
            if self?.forceLight == true { return .lightContent }
            return original(object, selector)
        }
        class_replaceMethod(
            cls,
            selector,
            imp_implementationWithBlock(override),
            method_getTypeEncoding(method)
        )
    }
}
