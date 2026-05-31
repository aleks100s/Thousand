import ComposeApp
import FirebaseAuth
import Foundation

extension IOSMultiplayerRepository {
    func currentPlayer() -> ComposeApp.User {
        let currentUser = Auth.auth().currentUser
        return User(
            id: currentUser?.uid ?? UUID().uuidString,
            name: currentUser?.displayName ?? "Без имени",
            kind: UserKind.remote
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
