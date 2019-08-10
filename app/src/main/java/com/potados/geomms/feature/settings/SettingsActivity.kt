package com.potados.geomms.feature.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.base.SingleFragmentActivity
import com.potados.geomms.common.extension.supportActionBar
import com.potados.geomms.extension.withNonNull
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : SingleFragmentActivity() {

    override fun fragment(): Fragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView()
    }

    private fun initializeView() {
        setSupportActionBar(toolbar)

        withNonNull(supportActionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}