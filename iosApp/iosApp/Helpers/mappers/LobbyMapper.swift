import ComposeApp

extension IOSMultiplayerManager {
    func dictionary(from lobby: Lobby) -> [String: Any] {
        swiftDictionary(from: FirebaseLobbyMapper.shared.dictionary(from: lobby))
    }

    func lobby(from value: Any?, key: String?) -> Lobby? {
        FirebaseLobbyMapper.shared.lobby(from: firebaseDictionary(from: value), key: key)
    }

    func lobbyPlayers(from value: Any?, key: String?) -> [ComposeApp.User] {
        guard let dictionary = firebaseDictionary(from: ["players": value as Any]),
              let lobby = FirebaseLobbyMapper.shared.lobby(from: dictionary, key: key) else {
            return []
        }

        return lobby.players
    }
}
