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
        if (Auth.auth().currentUser == nil) {
            authorize(connectWithFirebase: true)
        } else {
            updateIsAuthorized(isAuthorized: true)
            if (Auth.auth().currentUser?.providerData.contains(where: { $0.providerID == "password" }) == false) {
                authorize(connectWithFirebase: false)
            }
        }
        
    }
    
    override func logIn(email: String, password: String, completionHandler: @escaping ((any Error)?) -> Void) {
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
            
            let name = user.displayName ?? user.email?.components(separatedBy: "@").first ?? user.uid
            self?.saveFirebaseUser(uid: user.uid, name: name)
            completionHandler(nil)
        }
    }
    
    override func signUp(email: String, password: String, completionHandler: @escaping ((any Error)?) -> Void) {
        Auth.auth().createUser(withEmail: email, password: password) { [weak self] result, error in
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
            
            let name = user.displayName ?? user.email?.components(separatedBy: "@").first ?? user.uid
            self?.saveFirebaseUser(uid: user.uid, name: name)
            completionHandler(nil)
        }
    }
    
    private func authorize(connectWithFirebase: Bool) {
        GKLocalPlayer.local.authenticateHandler = { [weak self] viewController, error in
            DispatchQueue.main.async {
                self?.handleAuthentication(
                    viewController: viewController,
                    error: error,
                    connectWithFirebase: connectWithFirebase
                )
            }
        }
    }
    
    private func handleAuthentication(
        viewController: UIViewController?,
        error: Error?,
        connectWithFirebase: Bool
    ) {
        if let viewController {
            present(viewController: viewController)
            return
        }
        
        if let error {
            handleAuthenticationError(error: error)
            return
        }
        
        let hideMultiplayer = GKLocalPlayer.local.isUnderage ||
            GKLocalPlayer.local.isMultiplayerGamingRestricted
        updateHideMultiplayer(hideMultiplayer: hideMultiplayer)
        updateAuthorizedUserName(name: GKLocalPlayer.local.displayName)
        if connectWithFirebase {
            authorizeFirebase()
        }
    }
    
    private func authorizeFirebase() {
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
                
                self?.saveFirebaseUser(uid: user.uid, name: user.displayName ?? GKLocalPlayer.local.displayName)
            }
        }
    }
    
    private func saveFirebaseUser(uid: String, name: String) {
        updateAuthorizedUserName(name: name)
        updateIsAuthorized(isAuthorized: true)
        Database.database().reference().child("users").child(uid).setValue(["username": name])
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
