package com.potados.geomms.activity

import android.Manifest
import android.content.DialogInterface
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
import com.potados.geomms.data.MessageRepositoryImpl
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import com.potados.geomms.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setting_list_item.*
import kotlin.system.exitProcess

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

    private val mapFragment = MapFragment()
    private val messageListFragment = MessageListFragment()

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

    private fun onPermissionSuccess() {
        setupViewModelAndUI()

        /**
         * 메시지가 먼저이니까 메시지 탭 선택해줍니다.
         */
        mainViewModel.setSelectedTabMenuItemId(R.id.menu_item_navigation_message)
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

    private fun setupViewModelAndUI() {
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        mainViewModel.getSelectedTabMenuItemId().observe(this, object: Observer<Int> {
            override fun onChanged(t: Int) {
                switchFragmentByNavigationItemId(t)


                when (t) {
                    R.id.menu_item_navigation_message -> {

                    }

                    R.id.menu_item_navigation_map -> {

                    }
                }


                // TODO: 테스트 코드입니다.
                /*
                if (t == TAB_IDS[1]) {
                     usingRepository()
                }
                */
            }
        })

        nav_view.setOnNavigationItemSelectedListener { item ->
            mainViewModel.setSelectedTabMenuItemId(item.itemId)
        }

    }

    private fun switchFragmentByNavigationItemId(navigationItemId: Int): Boolean {
        val fragment = when (navigationItemId) {
            R.id.menu_item_navigation_message -> {
                messageListFragment
            }
            R.id.menu_item_navigation_map -> {
                mapFragment
            }

            else -> return false
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        return true
    }




    /**
     * TODO: 출시할 때에는 없애기
     * 실험용 함수입니다.
     */
    private fun showSmsInbox() {
        val p = Popup(this).withTitle("Messages")
        val uri = Uri.parse("content://sms")
        val projection = arrayOf("thread_id", "_id", "type", "address", "body")
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
        cursor.close()

        p.show()
    }
    private fun usingRepository() {
        val repo = MessageRepositoryImpl(contentResolver)

        val p = Popup(this).withTitle("Heads")
        repo.getConversationHeads().forEach {
            p.withMoreMessage("${it.address}\n${it.body}\n\n")
        }

        p.show()
    }




    companion object {
        /**
         * 메뉴 아이템의 id를 식별자로 직접 사용합니다.
         * TAB_IDS는 이러한 식별자들의 전체 집합입니다.
         */
        val TAB_IDS = arrayOf(
            R.id.menu_item_navigation_message,
            R.id.menu_item_navigation_map
        )

        /**
         * 권한 요청할때 식별자로 사용합니다. 제가 99라는 숫자를 좋아해서 99입니다.
         */
        const val PERMISSION_REQUEST_CODE = 99
    }
}
