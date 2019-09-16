package com.potados.geomms.feature.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.potados.geomms.R
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.repository.SyncRepository
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.usecase.ClearAll
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import org.koin.core.KoinComponent
import org.koin.core.inject

class SettingsFragment : PreferenceFragmentCompat(), KoinComponent {

    private val syncRepo: SyncRepository by inject()
    private val clearAll: ClearAll by inject()

    private val service: LocationSupportService by inject()

    private val navigator: Navigator by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        findPreference<Preference>("sync")?.setOnPreferenceClickListener {
            Popup(context)
                .withTitle(R.string.dialog_sync_messages)
                .withMessage(R.string.dialog_ask_sync_messages)
                .withPositiveButton(R.string.button_sync) {
                    if (!service.isIdle()) {
                        Popup(context)
                            .withTitle(R.string.dialog_warning)
                            .withMessage(R.string.dialog_sync_warning)
                            .withPositiveButton(R.string.button_confirm) { navigator.showSyncDialog(activity) }
                            .withNegativeButton(R.string.button_cancel)
                            .show()
                    } else {
                        navigator.showSyncDialog(activity)
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
                        clearAll(Unit) {
                            it.either({
                                Notify(context).short(R.string.notify_cleared)
                            }, {
                                Notify(context).short(R.string.notify_fail_clear)
                            })
                        }
                    }
                    .withNegativeButton(R.string.button_cancel)
                    .show()
            } else {
                Notify(context).short(R.string.notify_nothing_to_delete)
            }

            true
        }
    }
}