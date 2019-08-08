package com.potados.geomms.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.potados.geomms.R
import com.potados.geomms.core.navigation.Navigator
import com.potados.geomms.util.Notify
import com.potados.geomms.util.PermissionChecker
import com.potados.geomms.util.Popup
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess

/**
 * Explain user why we need these permissions.
 * After that, make a permission request to system.
 */
class GiveMePermissionActivity: AppCompatActivity() {

    private val navigator: Navigator by inject()
    private val permissionChecker: PermissionChecker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (permissionChecker.isAllGranted()) {
            navigator.showMain(this)
            this.finish()
        }

        Popup(this)
            .withTitle(getString(R.string.need_permission))
            .withMessage(getString(R.string.please_allow_permissions))
            .withPositiveButton(getString(R.string.ok)) { _, _ ->
                requirePermissions(permissionChecker.ungrantedPermissions())
            }
            .withNegativeButton(getString(R.string.cancel)) { _, _ ->
                Notify(this).short("why... :(")
            }
            .show()
    }

    private fun requirePermissions(permissions: Array<String>) {

        if (permissionChecker.isAllGranted()) {
            onPermissionSuccess()
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                permissionChecker.ungrantedPermissions(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != PERMISSION_REQUEST_CODE) return
        if (permissions.isEmpty()) return
        if (grantResults.isEmpty()) return

        // 결과가 전부 성공이면
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            // 다음으로 넘어가고

            Log.d("MainActivity:onRequestPermissionsResult", "all permission secured :)")

            onPermissionSuccess()
        }
        else {
            // 그렇지 않다면 잘 처리해줍니다..

            Log.d("MainActivity:onRequestPermissionsResult", "request failed :(")

            onPermissionFail()
        }
    }

    private fun onPermissionSuccess() {
        navigator.showMain(this)
        this.finish()
    }

    private fun onPermissionFail() {
        Popup(this)
            .withTitle("Alert")
            .withMessage("Failed to get permission.")
            .withPositiveButton("OK") { _, _ ->
                exitProcess(1)
            }
            .show()
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, GiveMePermissionActivity::class.java)

        private const val PERMISSION_REQUEST_CODE = 99
    }
}