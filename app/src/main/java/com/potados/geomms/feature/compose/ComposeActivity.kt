package com.potados.geomms.feature.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.base.SingleFragmentActivity
import com.potados.geomms.common.extension.addAll
import com.potados.geomms.common.extension.inImmediateTransaction

/**
 * 대화방 액티비티입니다.
 */
class ComposeActivity : SingleFragmentActivity() {

    override fun fragment(): Fragment = ComposeFragment.ofConversation(
        intent.getLongExtra(
            INTENT_PARAM_CONVERSATION,
            0
        )
    )

    companion object {
        private const val INTENT_PARAM_CONVERSATION = "com.potados.INTENT_PARAM_CONVERSATION"

        fun callingIntent(context: Context, conversationId: Long) =
            Intent(context, ComposeActivity::class.java).apply {
                putExtra(INTENT_PARAM_CONVERSATION, conversationId)
            }
    }
}
