package com.potados.geomms.feature.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.potados.geomms.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
    }
}