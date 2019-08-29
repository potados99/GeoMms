package com.potados.geomms.feature.location

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.potados.geomms.common.base.SingleFragmentActivity

/**
 * Toolbar at [InviteFragment]
 */
class InviteActivity : SingleFragmentActivity() {
    override val fragment: Fragment = InviteFragment()

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, InviteActivity::class.java)
    }
}