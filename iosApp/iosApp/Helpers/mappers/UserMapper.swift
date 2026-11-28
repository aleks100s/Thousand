import ComposeApp
import FirebaseAuth
import Foundation

extension IOSMultiplayerRepository {
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
