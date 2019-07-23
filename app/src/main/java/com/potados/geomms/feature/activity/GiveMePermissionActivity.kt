package com.potados.geomms.feature.activity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.potados.geomms.core.navigation.Navigator
import com.potados.geomms.core.navigation.RouteActivity
import com.potados.geomms.core.util.Notify
import com.potados.geomms.core.util.PermissionChecker
import com.potados.geomms.core.util.Popup
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess

class GiveMePermissionActivity: AppCompatActivity() {

    private val navigator: Navigator by inject()
    private val permissionChecker: PermissionChecker by inject()

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

        /**
         * 다음에 이어질 행동을 수행하려면
         * 1. 요청 코드가 PERMISSION_REQUEST_CODE이어야 하며,
         * 2. 요청한 권한들(permissions)이 존재해야 하며
         * 3. 그 결과 (grantResults) 또한 존재해야 합니다.
         */
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
    }

    private fun onPermissionFail() {
        Popup(this)
            .withTitle("Alert")
            .withMessage("Failed to get permission.")
            .withPositiveButton("OK", object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    exitProcess(1)
                }
            })
            .show()
    }


    companion object {
        fun callingIntent(context: Context) = Intent(context, GiveMePermissionActivity::class.java)

        private const val PERMISSION_REQUEST_CODE = 99
        private const val CHANGE_SMS_APP_REQUEST_CODE = 999

    }
}