package com.potados.geomms.feature.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import com.potados.geomms.BuildConfig
import com.potados.geomms.R
import com.potados.geomms.common.base.NavigationActivity
import com.potados.geomms.common.base.NavigationFragment
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.feature.conversations.ConversationsFragment
import com.potados.geomms.feature.location.MapFragment
import com.potados.geomms.functional.Result
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.usecase.SyncMessages
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * 권한 획득과 기본 앱 설정 후 나타나는 주 액티비티입니다.
 */
class MainActivity : NavigationActivity(), KoinComponent {

    override val fragments: List<NavigationFragment> = listOf(ConversationsFragment(), MapFragment())

    override val navigationMenuId: Int = R.menu.bottom_nav
    override val defaultMenuItemId: Int = R.id.menu_item_navigation_message
    override val layoutId: Int = R.layout.main_activity

    private val syncMessages: SyncMessages by inject()
    private val service: LocationSupportService by inject()

    private val navigator: Navigator by inject()

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDrawer()
        setVersionView()
        setService()
    }

    private fun setDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            root_layout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        toggle.syncState()

        // TODO dirty!
        sync.setOnClickListener {
            Popup(this)
                .withTitle("Sync messages")
                .withMessage("Do you want to sync messages?")
                .withPositiveButton("Sync") { _, _ ->
                    if (service.getConnections()?.isNotEmpty() == true
                        || service.getIncomingRequests()?.isNotEmpty() == true
                        || service.getOutgoingRequests()?.isNotEmpty() == true) {
                        Popup(this)
                            .withTitle("Warning")
                            .withMessage("You will lose all location connections and requests if you sync messages. Do you want to continue?")
                            .withPositiveButton("Confirm") { _, _ ->
                                syncMessages(Unit) {
                                    if (it is Result.Success){
                                        Notify(this).short("Sync completed.")
                                    }
                                    else {
                                        Notify(this).short("Sync failed.")
                                    }
                                }
                            }
                            .withNegativeButton("Cancel") { _, _ -> }
                            .show()
                    } else {
                        syncMessages(Unit) {
                            if (it is Result.Success){
                                Notify(this).short("Sync completed.")
                            }
                            else {
                                Notify(this).short("Sync failed.")
                            }
                        }
                    }
                }
                .withNegativeButton("Cancel") { _, _ -> }
                .show()

            root_layout.closeDrawers()
        }

        settings.setOnClickListener {
            navigator.showSettings()
            root_layout.closeDrawers()
        }

        help.setOnClickListener {
            // TODO Implement it
            Notify(this).short("Not implemented yet.")
        }
    }

    private fun setVersionView() {
        val currentVersion = getString(R.string.version, BuildConfig.VERSION_NAME)

        current_version.text = currentVersion
    }

    /**
     * Until user tab the Map tab, this service does not get started.
     * So do it here manually.
     */
    private fun setService() {
        service.start()
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}

