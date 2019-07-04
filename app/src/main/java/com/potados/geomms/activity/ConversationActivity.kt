package com.potados.geomms.activity

import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.potados.geomms.R
import com.potados.geomms.util.ContactHelper

import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        setSupportActionBar(activity_conversation_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        with(intent) {
            val address = getStringExtra(ARG_ADDRESS)

            val contactName = ContactHelper.getContactName(this@ConversationActivity, address)
            setToolbarTitle(contactName ?: address)
        }

    }


    private fun setToolbarTitle(title: String) {
        activity_conversation_toolbar_title.text = title
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        const val ARG_ADDRESS = "arg_address"
    }
}
