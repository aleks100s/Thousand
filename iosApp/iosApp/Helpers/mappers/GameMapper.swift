import ComposeApp

extension IOSMultiplayerManager {
    func dictionary(from game: RemoteGame) -> [String: Any] {
        swiftDictionary(from: FirebaseGameMapper.shared.dictionary(from: game))
    }

    func game(from value: Any?, key: String?) -> RemoteGame? {
        FirebaseGameMapper.shared.game(from: firebaseDictionary(from: value), key: key)
    }

    func gamePlayers(from value: Any?, key: String?) -> [Player] {
        guard let dictionary = firebaseDictionary(from: ["players": value as Any]),
              let game = FirebaseGameMapper.shared.game(from: dictionary, key: key) else {
            return []
        }

        return game.players
    }

    func gamePlayer(from dictionary: [String: Any]) -> Player {
        FirebasePlayerMapper.shared.player(from: dictionary)
    }
}
