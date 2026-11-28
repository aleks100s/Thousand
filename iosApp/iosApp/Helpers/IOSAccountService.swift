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
import Foundation

final class IOSAccountService: MutableNativeAccountService {
    override init() {
        super.init()
        FirebaseApp.configure()
        Auth.auth().addStateDidChangeListener { [weak self] auth, _ in
            if auth.currentUser?.isAnonymous != true {
                self?.clearUserId()
            }
        }
    }

    override func signInAnonymouslyPlatform(completionHandler: @escaping ((any Error)?) -> Void) {
        let auth = Auth.auth()
        if let currentUser = auth.currentUser, currentUser.isAnonymous {
            updateUserId(id: currentUser.uid)
            completionHandler(nil)
            return
        }

        clearUserId()
        if auth.currentUser != nil {
            do {
                try auth.signOut()
            } catch {
                Crashlytics.crashlytics().record(error: error)
                completionHandler(error)
                return
            }
        }

        auth.signInAnonymously { [weak self] result, error in
            if let error {
                self?.clearUserId()
                Crashlytics.crashlytics().record(error: error)
                completionHandler(error)
                return
            }

            guard let user = result?.user, user.isAnonymous else {
                self?.clearUserId()
                let error = NSError(
                    domain: "IOSAccountService",
                    code: 1,
                    userInfo: [NSLocalizedDescriptionKey: "Firebase anonymous user is missing after sign-in."]
                )
                Crashlytics.crashlytics().record(error: error)
                completionHandler(error)
                return
            }

            self?.updateUserId(id: user.uid)
            completionHandler(nil)
        }
    }

    override func currentAnonymousUserId() -> String? {
        guard let user = Auth.auth().currentUser, user.isAnonymous else { return nil }
        return user.uid
    }

    override func deleteAccountPlatform(completionHandler: @escaping ((any Error)?) -> Void) {
        guard let user = Auth.auth().currentUser, user.isAnonymous else {
            completionHandler(nil)
            return
        }

        user.delete { error in
            if let error {
                Crashlytics.crashlytics().record(error: error)
            }
            completionHandler(error)
        }
    }
}
