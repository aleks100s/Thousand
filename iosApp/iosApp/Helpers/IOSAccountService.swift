//
//  IOSAccountService.swift
//  iosApp
//
//  Created by Alexander on 21.05.2026.
//

import ComposeApp
import FirebaseAuth
import FirebaseCore
import FirebaseCrashlytics
import FirebaseCrashlyticsSwift
import FirebaseDatabase
import GameKit
import UIKit

final class IOSAccountService: MutableNativeAccountService {
    override init() {
        super.init()
        FirebaseApp.configure()
        observeFirebaseAuthorizationState()
        launchGameCenter()
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
            
            guard (result?.user) != nil else {
                self?.handleAuthenticationError(error: nil)
                completionHandler(nil)
                return
            }
            
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
            
            guard (result?.user) != nil else {
                self?.handleAuthenticationError(error: nil)
                completionHandler(nil)
                return
            }
            
            self?.updateFirebaseUser(name: name)
            completionHandler(nil)
        }
    }
    
    override func updateUserName(name: String) {
        updateFirebaseUser(name: name)
    }
    
    override func deleteAccount(completionHandler: @escaping ((any Error)?) -> Void) {
        let userId = Auth.auth().currentUser?.uid ?? ""
        guard !userId.isEmpty else {
            completionHandler(nil)
            return
        }

        Database.database().reference()
            .child(FirebasePath.users)
            .child(userId)
            .removeValue { error, _ in
                if let error {
                    Crashlytics.crashlytics().record(error: error)
                } else {
                    self.signOut()
                }
                completionHandler(error)
            }
    }
    
    override func signOut() {
        do {
            try Auth.auth().signOut()
            clearUserProfile()
        } catch {
            Crashlytics.crashlytics().record(error: error)
        }
    }
}

// MARK: - Firebase user observation

extension IOSAccountService {
    private func observeFirebaseAuthorizationState() {
        Task {
            for try await user in Auth.auth().authStateChanges {
                if let user {
                    updateRemoteUserInfo(user: user)
                } else {
                    clearUserProfile()
                }
            }
        }
    }
    
    private func updateFirebaseUser(name: String, photoURL: URL? = nil) {
        guard let user = Auth.auth().currentUser else {
            return
        }

        let changeRequest = user.createProfileChangeRequest()
        changeRequest.displayName = name
        if let photoURL {
            changeRequest.photoURL = photoURL
        }
        changeRequest.commitChanges { error in
            if let error {
                Crashlytics.crashlytics().record(error: error)
            } else {
                self.updateRemoteUserInfo(user: user)
            }
        }
    }
    
    private func updateRemoteUserInfo(user: FirebaseAuth.User) {
        let userReference = Database.database().reference()
            .child(FirebasePath.users)
            .child(user.uid)

        userReference.getData { error, snapshot in
            guard let snapshot else {
                if let error {
                    Crashlytics.crashlytics().record(error: error)
                }
                return
            }

            var values: [AnyHashable: Any] = [
                FirebaseUserKey.name: user.displayName ?? user.email ?? user.uid,
                FirebaseUserKey.platform: FirebasePlatform.ios
            ]

            if !snapshot.exists() {
                values[FirebaseUserKey.gameCount] = 0
                values[FirebaseUserKey.winCount] = 0
                values[FirebaseUserKey.rating] = 1000
            } else {
                if !snapshot.hasChild(FirebaseUserKey.gameCount) {
                    values[FirebaseUserKey.gameCount] = 0
                }
                if !snapshot.hasChild(FirebaseUserKey.winCount) {
                    values[FirebaseUserKey.winCount] = 0
                }
                if !snapshot.hasChild(FirebaseUserKey.rating) {
                    values[FirebaseUserKey.rating] = 1000
                }
            }

            userReference.updateChildValues(values) { error, _ in
                if let error {
                    Crashlytics.crashlytics().record(error: error)
                } else {
                    self.updateUserProfile(id: user.uid, name: user.displayName ?? user.email ?? user.uid)
                }
            }
        }
    }
}

// MARK: - Game Center Authentication

extension IOSAccountService {
    private func launchGameCenter() {
        GKLocalPlayer.local.authenticateHandler = { [weak self] viewController, error in
            DispatchQueue.main.async {
                self?.handleGameCenterAuthentication(viewController: viewController, error: error)
            }
        }
    }
    
    private func handleGameCenterAuthentication(
        viewController: UIViewController?,
        error: Error?
    ) {
        if let viewController {
            present(viewController: viewController)
            return
        }
        
        if let error {
            Crashlytics.crashlytics().record(error: error)
            return
        }
        
        let hideMultiplayer = GKLocalPlayer.local.isUnderage ||
            GKLocalPlayer.local.isMultiplayerGamingRestricted
        updateHideMultiplayer(hideMultiplayer: hideMultiplayer)
        if Auth.auth().currentUser == nil {
            connectFirebaseWithGameCenter()
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
                
                let name = user.displayName ?? GKLocalPlayer.local.displayName
                self?.updateFirebaseUser(name: name)
            }
        }
    }
}

// MARK: - Helpers

extension IOSAccountService {
    private func handleAuthenticationError(error: Error?) {
        clearUserProfile()
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
