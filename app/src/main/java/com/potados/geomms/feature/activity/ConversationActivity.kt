package com.potados.geomms.feature.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.potados.geomms.core.platform.BaseActivity
import com.potados.geomms.core.platform.BaseFragment
import com.potados.geomms.feature.data.entity.SmsThread
import com.potados.geomms.feature.fragment.ConversationFragment

/**
 * 대화방 액티비티입니다.
 */
class ConversationActivity : BaseActivity() {

    private val fragment by lazy {
        ConversationFragment.ofConversation(
            intent.getSerializableExtra(INTENT_PARAM_CONVERSATION) as SmsThread
        )
    }

    /**
     * BaseActivity 설정들.
     */
    override fun toolbarId(): Int? = null
    override fun toolbarMenuId(): Int?  = null
    override fun fragments(): Collection<BaseFragment> = listOf(fragment)


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
        private const val INTENT_PARAM_CONVERSATION = "com.potados.INTENT_PARAM_CONVERSATION"

        fun callingIntent(context: Context, conversation: SmsThread) =
            Intent(context, ConversationActivity::class.java).apply {
                putExtra(INTENT_PARAM_CONVERSATION, conversation)
            }

        private var instantiated = false
        fun isInstantiated() = instantiated
    }
}
