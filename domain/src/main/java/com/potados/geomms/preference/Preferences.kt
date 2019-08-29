package com.potados.geomms.preference

import com.f2prateek.rx.preferences2.RxSharedPreferences

class Preferences(private val rxPrefs: RxSharedPreferences) {
    val inAppNotification = rxPrefs.getBoolean("in_app_notification")
    val receiveGeoMms = rxPrefs.getBoolean("receive_geomms")
}