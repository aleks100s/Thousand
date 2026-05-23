//
//  IOSAccountService.swift
//  iosApp
//
//  Created by Alexander on 21.05.2026.
//

import ComposeApp
import FirebaseAuth
import FirebaseCrashlytics
import FirebaseCrashlyticsSwift
import FirebaseDatabase
import GameKit
import UIKit

final class IOSAccountService: MutableNativeAccountService {
    override init() {
        super.init()
        GKLocalPlayer.local.authenticateHandler = { [weak self] viewController, error in
            DispatchQueue.main.async {
                self?.handleAuthentication(viewController: viewController, error: error)
            }
        }
    }
    
    private func handleAuthentication(
        viewController: UIViewController?,
        error: Error?
    ) {
        if let viewController {
            present(viewController: viewController)
            return
        }
        
        if let error {
            Crashlytics.crashlytics().record(error: error)
            updateIsAuthorized(isAuthorized: false)
            return
        }
        
        let hideMultiplayer = GKLocalPlayer.local.isUnderage ||
            GKLocalPlayer.local.isMultiplayerGamingRestricted
        updateHideMultiplayer(hideMultiplayer: hideMultiplayer)
        updateAuthorizedUserName(name: GKLocalPlayer.local.displayName)
        authorizeFirebase()
    }
    
    private func authorizeFirebase() {
        // Get Firebase credentials from the player's Game Center credentials
        GameCenterAuthProvider.getCredential() { [weak self] (credential, error) in
            if let error = error {
                self?.updateIsAuthorized(isAuthorized: false)
                Crashlytics.crashlytics().record(error: error)
                return
            }
            
            guard let credential else {
                self?.updateIsAuthorized(isAuthorized: false)
                return
            }
            
            // The credential can be used to sign in, or re-auth, or link or unlink.
            Auth.auth().signIn(with: credential) { (user, error) in
                if let error = error {
                    Crashlytics.crashlytics().record(error: error)
                    return
                }
                
                guard let user = user?.user else {
                    return
                }
                
                self?.updateIsAuthorized(isAuthorized: true)
                Database.database().reference().child("users").child(user.uid).setValue(["username": user.displayName ?? GKLocalPlayer.local.displayName])
            }
        }
    }
    
    private func present(viewController: UIViewController) {
        guard let presenter = UIApplication.shared.activeTopViewController else {
            return
        }
        
        presenter.present(viewController, animated: true)
    }
}

private extension UIApplication {
    var activeTopViewController: UIViewController? {
        connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }?
            .windows
            .first { $0.isKeyWindow }?
            .rootViewController?
            .topViewController
    }
}

private extension UIViewController {
    var topViewController: UIViewController {
        if let presentedViewController {
            return presentedViewController.topViewController
        }
        
        if let navigationController = self as? UINavigationController,
           let visibleViewController = navigationController.visibleViewController {
            return visibleViewController.topViewController
        }
        
        if let tabBarController = self as? UITabBarController,
           let selectedViewController = tabBarController.selectedViewController {
            return selectedViewController.topViewController
        }
        
        return self
    }
}
