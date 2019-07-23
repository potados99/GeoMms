package com.potados.geomms.feature.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import androidx.appcompat.app.AppCompatActivity
import com.potados.geomms.R
import com.potados.geomms.core.navigation.Navigator
import com.potados.geomms.core.util.Notify
import com.potados.geomms.core.util.Popup
import org.koin.android.ext.android.inject

class MakeMeDefaultAppActivity : AppCompatActivity() {

    private val navigator: Navigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Telephony.Sms.getDefaultSmsPackage(this) == this.packageName) {
            navigator.showMain(this)
            this.finish()
        }

        Popup(this)
            .withTitle(getString(R.string.change_default_app))
            .withMessage(getString(R.string.make_me_default_app))
            .withPositiveButton(getString(R.string.ok)) { _, _ ->
                makeMeDefaultSmsApp()
            }
            .withNegativeButton(getString(R.string.cancel)) { _, _ ->
                Notify(this).short("why...:(")
            }
            .show()
    }

    private fun makeMeDefaultSmsApp() {
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)

        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, this.packageName)
        startActivityForResult(intent, CHANGE_SMS_APP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHANGE_SMS_APP_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
            }
            else {
                Notify(this).short("Got it.")
            }
        }
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, MakeMeDefaultAppActivity::class.java)

        const val CHANGE_SMS_APP_REQUEST_CODE = 99
    }
}