package com.potados.geomms.activity

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.potados.geomms.fragment.MapFragment
import com.potados.geomms.fragment.MessageListFragment
import com.potados.geomms.R
import com.potados.geomms.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 시작 액티비티입니다.
 * 탭 수만큼의 프래그먼트를 가지고 있습니다.
 */
class MainActivity : AppCompatActivity() {

    /**
     * 시작할 때에 프래그먼트를 모두 만들어 가지고 있음.
     */
    private val mapFragment = MapFragment()
    private val messageListFragment = MessageListFragment()

    /**
     * 메인 액티비티 수명주기동안 함께할 뷰모델.
     */
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requirePermission(Manifest.permission.READ_SMS)

        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                do {
                    var str = ""
                    for (i in (0 until it.columnCount)) {
                        str += " " + cursor.getColumnName(i) + ":" + cursor.getString(i) + "\n"
                    }
                    Log.d("Message start:", str)
                    Log.d("Messages end:","\n\n")

                } while (it.moveToNext())
            }
        }



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
            /*
            R.id.navigation_setting -> {
                settingFragment
            }
            */

            else -> return false
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        return true
    }


    /**
     * 권한을 얻어냅니다.
     */
    private fun requirePermission(permission: String) {
        val permissionCheck = ContextCompat.checkSelfPermission(this@MainActivity, permission)
        val granted = (permissionCheck == PackageManager.PERMISSION_GRANTED)
        val userDenied = ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

        if (!granted) {
            if (userDenied) {
                val permissionBagging = "Please allow access to $permission"
                Toast.makeText(this@MainActivity, permissionBagging, Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        }
    }

    companion object {
        val TAB_IDS = arrayOf(
            R.id.menu_item_navigation_map,
            R.id.menu_item_navigation_message
        )
    }
}
