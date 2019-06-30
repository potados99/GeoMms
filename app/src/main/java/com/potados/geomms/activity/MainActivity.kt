package com.potados.geomms.activity

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.potados.geomms.fragment.MapFragment
import com.potados.geomms.fragment.MessageListFragment
import com.potados.geomms.R
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import com.potados.geomms.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 위치정보 공유에 특화된 sms 클라이언트입니다.
 *
 * UI
 * MainActivity는 유일한 액티비티이며, 두 개의 네비게이션 버튼을 가집니다.
 * 두 네비게이션 탭에 따라 두 가지 프래그먼트가 표시됩니다.
 *
 * Data
 * 사용자에게 메시지가 도착하면 지도에 핑으로 표시되거나 메시지함으로 이동합니다.
 */

/**
 * 호출 흐름: onCreate -> requirePermission -> onRequestPermissionsResult -> onPermission**
 */
class MainActivity : AppCompatActivity() {

    /**
     * 프래그먼트
     */
    private val mapFragment = MapFragment()
    private val messageListFragment = MessageListFragment()

    /**
     * 뷰모델
     */
    private lateinit var mainViewModel: MainViewModel

    /**
     * Toast wrapper인 Notify 객체
     */
    private val n = Notify(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requirePermission(Manifest.permission.READ_SMS)
    }

    /**
     * 권한 설정 이후 결과를 수신하여 처리합니다.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != PERMISSION_REQUEST_CODE) return
        if (permissions.isEmpty()) return
        if (grantResults.isEmpty()) return

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            /**
             * 성공
             */
            onPermissionSuccess()
        }
        else {
            /**
             * 실패
             */
            onPermissionFail()
        }
    }


    /**
     * 권한을 얻어냅니다.
     */

    /**
     * 선택된 네비게이션 버튼의 id에 맞게 프래그먼트를 바꾸어줍니다.
     * @return id가 { navigation_home, navigation_message } 두 개 중 하나가 아니면 false
     */
    private fun switchFragmentByNavigationItemId(navigationItemId: Int): Boolean {
        val fragment = when (navigationItemId) {
            R.id.menu_item_navigation_map -> {
                mapFragment
            }
            R.id.menu_item_navigation_message -> {
                messageListFragment
            }

            else -> return false
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        return true
    }

    private fun requirePermission(permission: String) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(this@MainActivity, permission)
        val granted = (permissionCheckResult == PackageManager.PERMISSION_GRANTED)
        val userDenied = ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

        if (granted) {
            onPermissionSuccess()
        }
        else {
            if (userDenied) {
                n.short("Please allow access to $permission")
            }

            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }

    private fun onPermissionSuccess() {

        showSmsInbox()

        return

        /**
         * 뷰모델은 onCreate에서 만들어야된답니다..
         */
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        /**
         * 뷰모델과 바인딩
         */
        mainViewModel.getSelectedTabMenuItemId().observe(this, object: Observer<Int> {
            override fun onChanged(t: Int) {
                /**
                 * 탭이 바뀌면 해야 할 일들입니다.
                 */
                switchFragmentByNavigationItemId(t)
            }
        })

        nav_view.setOnNavigationItemSelectedListener { item ->
            mainViewModel.setSelectedTabMenuItemId(item.itemId)
        }

        /**
         * 지도 탭 선택해놓기.
         */
        mainViewModel.setSelectedTabMenuItemId(R.id.menu_item_navigation_map)
    }

    private fun onPermissionFail() {
        Popup.show(this, "Failed to get permission.")
    }


    /**
     * TODO: 출시할 때에는 없애기
     * 실험용 함수입니다.
     */
    private fun showSmsInbox() {
        val p = Popup(this).withTitle("Messages")
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "thread_id", "address", "date", "body")
        val cursor = contentResolver.query(uri, projection, null, null, null) ?: return

        if (cursor.moveToFirst()) {
            do {
                var str = ""
                for (i in (0 until cursor.columnCount)) {
                    str += " " + cursor.getColumnName(i) + ":" + cursor.getString(i) + "\n"
                }
                p.withMoreMessage("\n" + str)

            } while (cursor.moveToNext())
        }

        p.show()

        cursor.close()
    }

    companion object {
        /**
         * 메뉴 아이템의 id를 식별자로 직접 사용합니다.
         * TAB_IDS는 이러한 식별자들의 전체 집합입니다.
         */
        val TAB_IDS = arrayOf(
            R.id.menu_item_navigation_map,
            R.id.menu_item_navigation_message
        )

        /**
         * 권한 요청할때 식별자로 사용합니다. 제가 99라는 숫자를 좋아해서 99입니다.
         */
        const val PERMISSION_REQUEST_CODE = 99
    }
}
