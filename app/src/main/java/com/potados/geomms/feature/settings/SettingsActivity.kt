package com.potados.geomms.feature.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.base.SingleFragmentActivity
import com.potados.geomms.extension.withNonNull
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : SingleFragmentActivity() {

    override val fragment: Fragment = SettingsFragment()
    override val layoutId: Int = R.layout.settings_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView()
    }

    private fun initializeView() {
        setSupportActionBar(toolbar)

        // Here, activity has the toolbar.
        withNonNull(supportActionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)

            toolbar_title.text = getString(R.string.title_settings)
        }
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}