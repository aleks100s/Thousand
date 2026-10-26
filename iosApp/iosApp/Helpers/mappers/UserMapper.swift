import ComposeApp
import FirebaseAuth
import Foundation

extension IOSMultiplayerRepository {
    func currentPlayer() -> ComposeApp.User {
        let currentUser = Auth.auth().currentUser
        return accountService.playerForAccount(
            id: currentUser?.uid ?? UUID().uuidString,
            name: currentUser?.displayName ?? "Без имени"
        )
    }

    func dictionary(from user: ComposeApp.User) -> [String: Any] {
        swiftDictionary(from: FirebaseUserMapper.shared.dictionary(from: user))
    }

    func user(from dictionary: [String: Any]) -> ComposeApp.User {
        FirebaseUserMapper.shared.user(from: dictionary)
    }

    func lobbyPlayer(from dictionary: [String: Any]) -> ComposeApp.User {
        FirebaseUserMapper.shared.lobbyPlayer(from: dictionary)
    }
}
