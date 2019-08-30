package com.potados.geomms.preference

import android.content.Context
import androidx.preference.PreferenceManager

class MyPreferences(context: Context) {

    private val pref = PreferenceManager.getDefaultSharedPreferences(context)

    val inAppNotification get() = pref.getBoolean("in_app_notification", false)

    val receiveGeoMms get() = pref.getBoolean("receive_geomms", true)

    val showAllError get() = pref.getBoolean("show_all_error", false)
}