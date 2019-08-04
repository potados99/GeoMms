package com.potados.geomms.feature.message.representation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.potados.geomms.core.platform.BaseActivity
import com.potados.geomms.core.platform.BaseFragment
import com.potados.geomms.feature.message.data.ConversationEntity
import com.potados.geomms.feature.message.domain.Conversation

/**
 * 대화방 액티비티입니다.
 */
class ConversationActivity : BaseActivity() {

    private val fragment by lazy {
        ConversationFragment.ofConversation(
            intent.getLongExtra(INTENT_PARAM_CONVERSATION, 0)
        )
    }

    /**
     * BaseActivity 설정들.
     */
    override fun toolbarId(): Int? = null
    override fun toolbarMenuId(): Int?  = null
    override fun fragments(): Array<out BaseFragment> = arrayOf(fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isInstantiated()) {
            this.finish()
        }
        instantiated = true

        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        instantiated = false
    }


    companion object {
        private const val INTENT_PARAM_CONVERSATION = "com.potados.INTENT_PARAM_CONVERSATION"

        fun callingIntent(context: Context, conversationId: Long) =
            Intent(context, ConversationActivity::class.java).apply {
                putExtra(INTENT_PARAM_CONVERSATION, conversationId)
            }

        private var instantiated = false
        fun isInstantiated() =
            instantiated
    }
}
