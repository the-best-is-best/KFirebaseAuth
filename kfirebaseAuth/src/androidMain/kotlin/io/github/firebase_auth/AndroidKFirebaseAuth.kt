package io.github.firebase_auth

import android.app.Activity
import java.lang.ref.WeakReference

object AndroidKFirebaseAuth {
    private var activityWeakRef: WeakReference<Activity>? = null
    internal var webClientId: String? = null

    fun init(activity: Activity, webClientId: String) {
        activityWeakRef = WeakReference(activity)
        this.webClientId = webClientId
    }

    internal fun getActivity(): Activity? {
        return activityWeakRef?.get()
    }
}