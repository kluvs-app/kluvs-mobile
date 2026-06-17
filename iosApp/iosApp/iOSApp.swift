import SwiftUI
import Shared

@main
struct iOSApp: App {

    init() {
        SentrySetupKt.initializeSentry()
        startLogging()
        KoinHelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .tint(.brandOrange)
                .onOpenURL { url in
                    OAuthCallbackHandler.shared.handleCallback(url)
                }
        }
    }
    
    private func startLogging() {
        #if DEBUG
        let volume = Level.verbose
        #else
        let volume = Level.debug
        #endif
        
        Bark.autoTagDisabled = false
        Bark.train(trainer: NSLogTrainer(volume: Level.verbose))
        Bark.train(trainer: SentryTrainer())
        Bark.v("Bark has been initilized for iOS successfully!")
    }
}
