package com.potados.geomms.feature.compose

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.potados.geomms.common.base.SingleFragmentActivity

/**
 * Toolbar at [ComposeFragment]
 */
class ComposeActivity : SingleFragmentActivity() {

    override val fragment: Fragment = ComposeFragment()

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, ComposeActivity::class.java)
    }
}
