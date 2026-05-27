import ComposeApp

extension IOSMultiplayerManager {
    func dictionary(from lobby: Lobby) -> [String: Any] {
        swiftDictionary(from: FirebaseLobbyMapper.shared.dictionary(from: lobby))
    }

    func lobby(from value: Any?, fallbackId: String = "") -> Lobby? {
        FirebaseLobbyMapper.shared.lobby(from: firebaseDictionary(from: value), fallbackId: fallbackId)
    }

    func lobbyPlayers(from value: Any?) -> [ComposeApp.User] {
        guard let dictionary = firebaseDictionary(from: ["players": value as Any]),
              let lobby = FirebaseLobbyMapper.shared.lobby(from: dictionary, fallbackId: "") else {
            return []
        }

        return lobby.players
    }
}
