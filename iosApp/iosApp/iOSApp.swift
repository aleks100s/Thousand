import ComposeApp
import SwiftUI

@main
struct iOSApp: App {
    private let observer = DefaultShakeDeviceObserver()
    private let accountService = IOSAccountService()
    private let multiplayerManager = IOSMultiplayerManager()
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        KoinKt.doInitKoin(
            shakeDeviceObserver: observer,
            nativeAccountService: accountService,
            multiplayerManager: multiplayerManager,
            appDeclaration: { _ in }
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView(observer: observer)
        }
    }
}
