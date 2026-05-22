import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    private let observer = DefaultShakeDeviceObserver()
    private let authenticator = IOSAuthenticatorService()
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        KoinKt.doInitKoin(
            shakeDeviceObserver: observer,
            nativeAuthenticatorService: authenticator,
            appDeclaration: { _ in }
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView(observer: observer)
        }
    }
}
