package com.potados.geomms.feature.license

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.potados.geomms.common.base.SingleFragmentActivity

class LicenseActivity : SingleFragmentActivity() {
    override val fragment: Fragment = LicenseFragment()

    companion object {
        fun callingIntent(context: Context): Intent {
            return Intent(context, LicenseActivity::class.java)
        }
    }
}