import ComposeApp
import UIKit

final class IOSGamePresenceObserver: GamePresenceObserver {
    var delegate: GamePresenceObserverDelegate?
    private var observers: [NSObjectProtocol] = []

    init() {
        observers.append(NotificationCenter.default.addObserver(
            name: UIApplication.didEnterBackgroundNotification,
            object: nil,
            queue: nil
        ) { [weak self] _ in
            self?.notifyUserLeftGame()
        })
        observers.append(NotificationCenter.default.addObserver(
            name: UIApplication.willEnterForegroundNotification,
            object: nil,
            queue: nil
        ) { [weak self] _ in
            self?.notifyUserReturnedToGame()
        })
    }

    deinit {
        observers.forEach { observer in
            NotificationCenter.default.removeObserver(observer)
        }
    }

    func notifyUserLeftGame() {
        delegate?.userDidLeaveGame()
    }

    func notifyUserReturnedToGame() {
        delegate?.userDidReturnToGame()
    }
}
