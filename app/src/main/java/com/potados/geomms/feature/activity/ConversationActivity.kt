package com.potados.geomms.feature.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.potados.geomms.core.platform.BaseActivity
import com.potados.geomms.core.platform.BaseFragment
import com.potados.geomms.feature.fragment.ConversationFragment

/**
 * 대화방 액티비티입니다.
 */
class ConversationActivity : BaseActivity() {

    /**
     * BaseActivity 설정들.
     */
    override fun toolbarId(): Int? = null
    override fun toolbarMenuId(): Int?  = null
    override fun fragments(): Collection<BaseFragment> = listOf(mFragment)

    /** 사용할 프래그먼트 */
    private val mFragment = ConversationFragment()


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        if (isInstantiated()) {
            this.finish()
        }
        instantiated = true

        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onDestroy() {
        super.onDestroy()
        instantiated = false
    }


    companion object {
        fun callingIntent(context: Context) = Intent(context, ConversationActivity::class.java)

        const val ARG_SMS_THREAD = "arg_sms_thread"

        /**
         * 나만 건드릴 수 있지
         */
        private var instantiated = false
        fun isInstantiated() = instantiated
    }
}
