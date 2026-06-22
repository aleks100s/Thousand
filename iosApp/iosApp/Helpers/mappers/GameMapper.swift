import ComposeApp

extension IOSMultiplayerRepository {
    func dictionary(from game: RemoteGame) -> [String: Any] {
        swiftDictionary(from: FirebaseGameMapper.shared.dictionary(from: game))
    }

    func updateDictionary(from game: RemoteGame) -> [String: Any] {
        var dictionary = dictionary(from: game)
        dictionary.removeValue(forKey: FirebaseGameKey.onlinePlayerIds)

        if game.reaction == nil {
            dictionary[FirebaseGameKey.reaction] = NSNull()
        }

        return dictionary
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
