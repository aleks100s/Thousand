package com.alextos.thousand.data.service

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.alextos.thousand.domain.service.GamePresenceObserver
import com.alextos.thousand.domain.service.GamePresenceObserverDelegate

class AndroidGamePresenceObserver(
    application: Application,
) : GamePresenceObserver, Application.ActivityLifecycleCallbacks {
    override var delegate: GamePresenceObserverDelegate? = null

    private var startedActivityCount = 0
    private var wasInBackground = false

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun notifyUserLeftGame() {
        delegate?.userDidLeaveGame()
    }

    override fun notifyUserReturnedToGame() {
        delegate?.userDidReturnToGame()
    }

    override fun onActivityStarted(activity: Activity) {
        if (startedActivityCount == 0 && wasInBackground) {
            wasInBackground = false
            notifyUserReturnedToGame()
        }

        startedActivityCount += 1
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)

        if (startedActivityCount == 0) {
            wasInBackground = true
            notifyUserLeftGame()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
