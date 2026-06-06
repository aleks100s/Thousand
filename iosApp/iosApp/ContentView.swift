import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let observer: ShakeDeviceObserver
    
    var body: some View {
        GeometryReader { proxy in
            let isLandscapePhone = UIDevice.current.userInterfaceIdiom == .phone
                && proxy.size.width > proxy.size.height

            ComposeView()
                .ignoresSafeArea(.all, edges: isLandscapePhone ? .vertical : .all)
                .background()
                .onShake {
                    observer.shake()
                }
        }
    }
}
