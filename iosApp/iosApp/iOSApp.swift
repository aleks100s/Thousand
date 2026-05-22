import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    private let observer = DefaultShakeDeviceObserver()
    private let accountService = IOSAccountService()
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        KoinKt.doInitKoin(
            shakeDeviceObserver: observer,
            nativeAccountService: accountService,
            appDeclaration: { _ in }
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView(observer: observer)
        }
    }
}
