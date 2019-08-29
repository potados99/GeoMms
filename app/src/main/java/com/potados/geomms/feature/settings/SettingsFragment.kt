package com.potados.geomms.feature.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.potados.geomms.R
import com.potados.geomms.functional.Result
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.usecase.SyncMessages
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import org.koin.core.KoinComponent
import org.koin.core.inject

class SettingsFragment : PreferenceFragmentCompat(), KoinComponent {

    private val syncMessages: SyncMessages by inject()
    private val service: LocationSupportService by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        findPreference<Preference>("sync")?.setOnPreferenceClickListener {
            val doSync = {
                syncMessages(Unit) {
                    if (it is Result.Success){
                        Notify(context).short(R.string.notify_sync_completed)
                    }
                    else {
                        Notify(context).short(R.string.notify_sync_failed)
                    }
                }
            }

            Popup(context)
                .withTitle(R.string.dialog_sync_messages)
                .withMessage(R.string.dialog_ask_sync_messages)
                .withPositiveButton(R.string.button_sync) {
                    if (!service.isIdle()) {
                        Popup(context)
                            .withTitle(R.string.dialog_warning)
                            .withMessage(R.string.dialog_sync_warning)
                            .withPositiveButton(R.string.button_confirm) { doSync() }
                            .withNegativeButton(R.string.button_cancel)
                            .show()
                    } else {
                        doSync()
                    }
                }
                .withNegativeButton(R.string.button_cancel)
                .show()

            true
        }

        findPreference<Preference>("clear_all_geomms")?.setOnPreferenceClickListener {
            if (!service.isIdle()) {
                Popup(context)
                    .withTitle(R.string.dialog_warning)
                    .withMessage(R.string.dialog_clear_geomms_warning)
                    .withPositiveButton(R.string.button_confirm) {
                        service.clearAll()
                    }
                    .withNegativeButton(R.string.button_cancel)
                    .show()
            }

            true
        }
    }
}