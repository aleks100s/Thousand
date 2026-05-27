import ComposeApp
import FirebaseAuth
import Foundation

extension IOSMultiplayerManager {
    func currentPlayer() -> ComposeApp.User {
        let currentUser = Auth.auth().currentUser
        return User(
            id: currentUser?.uid ?? UUID().uuidString,
            name: currentUser?.displayName ?? "Без имени",
            kind: UserKind.remote
        )
    }

    func dictionary(from user: ComposeApp.User) -> [String: Any] {
        [
            "id": user.id,
            "name": user.name,
            "kind": user.kind.name
        ]
    }

    func user(from dictionary: [String: Any]) -> ComposeApp.User {
        User(
            id: dictionary["id"] as? String ?? UUID().uuidString,
            name: dictionary["name"] as? String ?? "Без имени",
            kind: userKind(from: dictionary["kind"])
        )
    }

    func lobbyPlayer(from dictionary: [String: Any]) -> ComposeApp.User {
        let id = dictionary["id"] as? String ?? UUID().uuidString
        return User(
            id: id,
            name: dictionary["name"] as? String ?? "Без имени",
            kind: UserKind.remote
        )
    }

    func userKind(from value: Any?) -> UserKind {
        if let name = value as? String {
            switch name {
            case UserKind.bot.name:
                return UserKind.bot
            case UserKind.mainuser.name:
                return UserKind.mainuser
            case UserKind.remote.name:
                return UserKind.remote
            default:
                return UserKind.localuser
            }
        }

        if let ordinal = int32(from: value) {
            switch ordinal {
            case UserKind.bot.ordinal:
                return UserKind.bot
            case UserKind.mainuser.ordinal:
                return UserKind.mainuser
            case UserKind.remote.ordinal:
                return UserKind.remote
            default:
                return UserKind.localuser
            }
        }

        return UserKind.localuser
    }
}
