import ComposeApp
import Foundation

extension IOSMultiplayerRepository {
    func remoteUserInfo(from dictionary: [String: Any]?, userId: String) -> RemoteUserInfo? {
        FirebaseRemoteUserInfoMapper.shared.remoteUserInfo(from: dictionary, key: userId)
    }
}
