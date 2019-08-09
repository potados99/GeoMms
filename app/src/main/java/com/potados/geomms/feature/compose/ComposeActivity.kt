package com.potados.geomms.feature.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseActivity
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.addAll
import com.potados.geomms.common.extension.inImmediateTransaction
import com.potados.geomms.feature.message.data.ConversationEntity

/**
 * 대화방 액티비티입니다.
 */
class ComposeActivity : AppCompatActivity() {

    private val fragment by lazy {
        ComposeFragment.ofConversation(
            intent.getLongExtra(
                INTENT_PARAM_CONVERSATION,
                0
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isInstantiated()) {
            this.finish()
        }
        instantiated = true

        super.onCreate(savedInstanceState)
        setContentView(R.layout.con)

        savedInstanceState ?:
        supportFragmentManager.inImmediateTransaction {
            addAll(fragmentContainerId(), fragments())
            this
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instantiated = false
    }


    companion object {
        private const val INTENT_PARAM_CONVERSATION = "com.potados.INTENT_PARAM_CONVERSATION"

        fun callingIntent(context: Context, conversationId: Long) =
            Intent(context, ComposeActivity::class.java).apply {
                putExtra(INTENT_PARAM_CONVERSATION, conversationId)
            }

        private var instantiated = false
        fun isInstantiated() =
            instantiated
    }
}
