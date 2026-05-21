import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    private let observer = DefaultShakeDeviceObserver()
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        KoinKt.doInitKoin(shakeDeviceObserver: observer, appDeclaration: { _ in })
    }

    var body: some Scene {
        WindowGroup {
            ContentView(observer: observer)
        }
    }
}
