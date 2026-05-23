import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    private let observer = DefaultShakeDeviceObserver()
    private let accountService = IOSAccountService()
    private let multiplayerRepository = IOSMultiplayerRepository()
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        KoinKt.doInitKoin(
            shakeDeviceObserver: observer,
            nativeAccountService: accountService,
            multiplayerRepository: multiplayerRepository,
            appDeclaration: { _ in }
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView(observer: observer)
        }
    }
}
