package com.potados.geomms.common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.potados.geomms.R
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.injection.permissions
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import kotlinx.android.synthetic.main.permission_activity.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.system.exitProcess

/**
 * Explain user why we need these permissions.
 * After that, make a permission request to system.
 */
class GiveMePermissionActivity: AppCompatActivity() {

    private val navigator: Navigator by inject()
    private val permissionManager: com.potados.geomms.manager.PermissionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (permissionManager.isAllGranted()) {
            navigator.showMain()
            this.finish()
        }

        setContentView(R.layout.permission_activity)

        allow.setOnClickListener {
            requirePermissions(permissions)
        }
    }

    private fun requirePermissions(permissions: Array<String>) {
        if (permissionManager.isAllGranted()) {
            onPermissionSuccess()
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                permissionManager.ungrantedPermissions(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != PERMISSION_REQUEST_CODE) return
        if (permissions.isEmpty()) return
        if (grantResults.isEmpty()) return

        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Timber.i("All permission secured :)")

            onPermissionSuccess()
        }
        else {
            Timber.i("Request failed :(")

            onPermissionFail()
        }
    }

    private fun onPermissionSuccess() {
        navigator.showMain()
        this.finish()
    }

    private fun onPermissionFail() {
        Popup(this)
            .withTitle(getString(R.string.title_without_permissions))
            .withMessage(getString(R.string.dialog_please_permission))
            .withPositiveButton(R.string.button_ok)
            .show()
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, GiveMePermissionActivity::class.java)

        private const val PERMISSION_REQUEST_CODE = 99
    }
}