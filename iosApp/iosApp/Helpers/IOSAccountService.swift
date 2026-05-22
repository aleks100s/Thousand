//
//  IOSAccountService.swift
//  iosApp
//
//  Created by Alexander on 21.05.2026.
//

import ComposeApp
import GameKit

final class IOSAccountService: MutableNativeAccountService {
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
        let hideMultiplayer = GKLocalPlayer.local.isUnderage ||
            GKLocalPlayer.local.isMultiplayerGamingRestricted
        updateHideMultiplayer(hideMultiplayer: hideMultiplayer)
        updateIsAuthorized(isAuthorized: GKLocalPlayer.local.isAuthenticated)
        updateAuthorizedUserName(name: GKLocalPlayer.local.displayName)

        if let viewController {
            UIApplication.shared.delegate?.window??.rootViewController?.present(viewController, animated: true)
            return
        }
        
        if let error {
            print(error.localizedDescription)
            return
        }
    }
}
