//
//  IOSAuthenticatorService.swift
//  iosApp
//
//  Created by Alexander on 21.05.2026.
//

import ComposeApp
import GameKit

final class IOSAuthenticatorService: NativeAuthenticatorService {
    var delegate: (any NativeAuthenticatorDelegate)?
    
    init() {
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
            delegate?.userAuthenticationChanged(status: AuthenticationStatusLoggedOut())
            return
        }
        
        if GKLocalPlayer.local.isUnderage {
            // Hide explicit game content.
        }


        if GKLocalPlayer.local.isMultiplayerGamingRestricted {
            // Disable multiplayer game features.
        }


        if GKLocalPlayer.local.isPersonalizedCommunicationRestricted {
            // Disable in game communication UI.
        }
        
        delegate?.userAuthenticationChanged(status: AuthenticationStatusLoggedIn(name: "Loh"))
    }
}
