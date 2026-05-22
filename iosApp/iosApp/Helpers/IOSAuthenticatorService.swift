//
//  IOSAuthenticatorService.swift
//  iosApp
//
//  Created by Alexander on 21.05.2026.
//

import ComposeApp
import GameKit

final class IOSAuthenticatorService: MutableNativeAuthenticatorService {
    override init() {
        super.init()
        GKLocalPlayer.local.authenticateHandler = { viewController, error in
            self.handleAuthentication(viewController: viewController, error: error)
        }
    }
    
    private func handleAuthentication(
        viewController: UIViewController?,
        error: Error?
    ) {
        if let viewController {
            UIApplication.shared.delegate?.window??.rootViewController?.present(viewController, animated: true)
            return
        }
        
        if let error {
            print(error.localizedDescription)
            updateAuthenticationStatus(status: AuthenticationStatusLoggedOut())
            return
        }
        
        let hideMultiplayer = GKLocalPlayer.local.isUnderage ||
            GKLocalPlayer.local.isMultiplayerGamingRestricted
        updateHideMultiplayer(hideMultiplayer: hideMultiplayer)
        
        if GKLocalPlayer.local.isAuthenticated {
            updateAuthenticationStatus(status: AuthenticationStatusLoggedIn(name: GKLocalPlayer.local.displayName))
        } else {
            updateAuthenticationStatus(status: AuthenticationStatusLoggedOut())
        }
    }
}
