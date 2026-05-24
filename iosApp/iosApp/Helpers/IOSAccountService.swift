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
        observeGameCenterAuthorization()
    }
    
    override func logIn(
        email: String,
        password: String,
        completionHandler: @escaping ((any Error)?) -> Void
    ) {
        Auth.auth().signIn(withEmail: email, password: password) { [weak self] result, error in
            if let error {
                self?.handleAuthenticationError(error: error)
                completionHandler(error)
                return
            }
            
            guard let user = result?.user else {
                self?.handleAuthenticationError(error: nil)
                completionHandler(nil)
                return
            }
            
            self?.saveFirebaseUser(name: user.displayName ?? user.uid)
            completionHandler(nil)
        }
    }
    
    override func signUp(
        email: String,
        password: String,
        name: String,
        completionHandler: @escaping ((any Error)?) -> Void
    ) {
        Auth.auth().createUser(withEmail: email, password: password) { [weak self] result, error in
            if let error {
                self?.handleAuthenticationError(error: error)
                completionHandler(error)
                return
            }
            
            guard result?.user != nil else {
                self?.handleAuthenticationError(error: nil)
                completionHandler(nil)
                return
            }
            
            self?.saveFirebaseUser(name: name)
            completionHandler(nil)
        }
    }
    
    override func updatePlayerName(name: String) {
        guard Auth.auth().currentUser != nil else {
            return
        }
        
        saveFirebaseUser(name: name)
    }
    
    private func observeGameCenterAuthorization() {
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
            handleAuthenticationError(error: error)
            if Auth.auth().currentUser != nil {
                updateIsAuthorized(isAuthorized: true)
            }
            return
        }
        
        let hideMultiplayer = GKLocalPlayer.local.isUnderage ||
            GKLocalPlayer.local.isMultiplayerGamingRestricted
        updateHideMultiplayer(hideMultiplayer: hideMultiplayer)
        if Auth.auth().currentUser == nil {
            connectFirebaseWithGameCenter()
        } else {
            updateIsAuthorized(isAuthorized: true)
        }
    }
    
    private func connectFirebaseWithGameCenter() {
        // Get Firebase credentials from the player's Game Center credentials
        GameCenterAuthProvider.getCredential() { [weak self] (credential, error) in
            if let error = error {
                self?.handleAuthenticationError(error: error)
                return
            }
            
            guard let credential else {
                self?.handleAuthenticationError(error: nil)
                return
            }
            
            // The credential can be used to sign in, or re-auth, or link or unlink.
            Auth.auth().signIn(with: credential) { (user, error) in
                if let error = error {
                    self?.handleAuthenticationError(error: error)
                    return
                }
                
                guard let user = user?.user else {
                    self?.handleAuthenticationError(error: nil)
                    return
                }
                
                self?.saveFirebaseUser(name: user.displayName ?? GKLocalPlayer.local.displayName)
            }
        }
    }
    
    private func saveFirebaseUser(name: String) {
        updateAuthorizedUserName(name: name)
        updateIsAuthorized(isAuthorized: true)
        Auth.auth().currentUser?.displayName = name
    }
    
    private func handleAuthenticationError(error: Error?) {
        updateIsAuthorized(isAuthorized: false)
        if let error {
            Crashlytics.crashlytics().record(error: error)
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
