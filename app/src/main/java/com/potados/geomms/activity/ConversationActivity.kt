package com.potados.geomms.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.potados.geomms.R
import com.potados.geomms.data.MessageRepository
import com.potados.geomms.util.ContactHelper
import com.potados.geomms.util.Popup
import com.potados.geomms.util.ShortDate

import kotlinx.android.synthetic.main.activity_conversation.*
import org.koin.android.ext.android.inject

class ConversationActivity : AppCompatActivity() {

    private val messageRepo: MessageRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isInstantiated()) finish()
        instantiated = true

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        setSupportActionBar(activity_conversation_toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        with(intent) {
            val address = getStringExtra(ARG_ADDRESS)
            val threadId = getLongExtra(ARG_THREAD_ID, 0)

            val contactName = ContactHelper.getContactName(this@ConversationActivity, address)
            setToolbarTitle(contactName ?: address)

            val thread = messageRepo.getSmsThreadByThreadId(threadId)
            val p = Popup(this@ConversationActivity).withTitle("Conversation with $address")

            thread.allMessages().forEach {
                p.withMoreMessage("${ShortDate.of(it.date)}\n${it.body}\n\n")
            }

            p.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instantiated = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setToolbarTitle(title: String) {
        activity_conversation_toolbar_title.text = title
    }


    companion object {
        const val ARG_ADDRESS = "arg_address"
        const val ARG_THREAD_ID = "arg_thread_id"

        /**
         * 나만 건드릴 수 있지
         */
        private var instantiated = false
        fun isInstantiated() = instantiated
    }
}
