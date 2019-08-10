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
import kotlinx.android.synthetic.main.main_activity.*

/**
 * 대화방 액티비티입니다.
 */
class ComposeActivity : AppCompatActivity() {

    private val fragments by lazy {
        arrayOf(ComposeFragment.ofConversation(
            intent.getLongExtra(
                INTENT_PARAM_CONVERSATION,
                0
            )
        ))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_activity)

        addFragments(savedInstanceState)
    }

    private fun addFragments(savedInstanceState: Bundle?) =
        savedInstanceState ?:
        supportFragmentManager.inImmediateTransaction {
            addAll(R.id.fragment_container, fragments)
            this
        }

    companion object {
        private const val INTENT_PARAM_CONVERSATION = "com.potados.INTENT_PARAM_CONVERSATION"

        fun callingIntent(context: Context, conversationId: Long) =
            Intent(context, ComposeActivity::class.java).apply {
                putExtra(INTENT_PARAM_CONVERSATION, conversationId)
            }
    }
}
