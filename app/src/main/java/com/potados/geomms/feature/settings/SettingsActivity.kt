package com.potados.geomms.feature.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.core.platform.BaseActivity

class SettingsActivity : BaseActivity() {

    override fun layoutId(): Int = R.layout.activity_settings
    override fun fragmentContainerId(): Int = R.id.settings_fragment_container
    override fun toolbarId(): Int? = R.id.settings_toolbar
    override fun toolbarMenuId(): Int? = null

    override fun fragments(): Array<out Fragment> = arrayOf(SettingsFragment())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
            else -> {
                // 낫띵..
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}