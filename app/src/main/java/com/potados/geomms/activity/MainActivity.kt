package com.potados.geomms.activity

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.potados.geomms.fragment.MapFragment
import com.potados.geomms.fragment.ConversationListFragment
import com.potados.geomms.R
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import com.potados.geomms.util.QueryHelper
import com.potados.geomms.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

/**
 * MainActivity
 * 권한 획득과 프래그먼트간 전환을 담당합니다.
 * ConversationListFragment(메시지 리스트), MapFragment(지도) 두 프래그먼트를 사용합니다.
 *
 * 루틴 호출 순서는 다음과 같습니다:
 * onCreate -> requirePermission -> onRequestPermissionsResult -> onPermission[Success|Fail]
 */
class MainActivity : AppCompatActivity() {

    /**
     * 대화방 목록을 보여주는 프래그먼트.
     */
    private val messageListFragment = ConversationListFragment()

    /**
     * 지도와 친구 목록을 보여주는 프래그먼트.
     */
    private val mapFragment = MapFragment()

    /**
     * 뷰모델.
     */
    private lateinit var viewModel: MainViewModel

    /**
     * 진입점입니다.
     * 권한을 요청하는 것으로 시작합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requirePermissions(PERMISSIONS_OF_THIS_APP)

        Log.d("MainActivity: onCreate", "created.")
    }

    /**
     * 앱 사용 권한을 얻어냅니다.
     * 모든 권한이 허용되어 있다면 onPermissionSuccess를 직접 호출하고,
     * 그렇지 않으면 시스템에 권한을 요청하며, 이에 의해 onRequestPermissionsResult 콜백이 실행됩니다.
     */
    private fun requirePermissions(permissions: Array<String>) {

        val notGrantedPermissions = mutableListOf<String>()

        // 필요한 권한 하나씩 돌아가며 확인하는데
        permissions.forEach {

            val permissionCheckResult = ContextCompat.checkSelfPermission(this@MainActivity, it)

            val isGranted = (permissionCheckResult == PackageManager.PERMISSION_GRANTED)
            val isDeniedByUser = ActivityCompat.shouldShowRequestPermissionRationale(this, it)

            // 권한을 획득하지 못한 경우에는
            if (isGranted.not()) {
                Log.d("MainActivity:requirePermissions", "permission \"$it\" is not granted.")

                // 이를 기억해줍니다.
                notGrantedPermissions.add(it)

                // 사용자에 의해 거절당한 경우라면 설명을 내보냅니다.
                if (isDeniedByUser) {
                    Log.d("MainActivity:requirePermissions", "hey user! we need $it")

                    explainUserWhyWeNeedThisPermission(it)
                }

            }
            else {
                Log.d("MainActivity:requirePermissions", "permission \"$it\" is granted.")
            }

        }

        // 획득하지 못한 권한이 없다면
        if (notGrantedPermissions.isEmpty()) {
            // onPermissionSuccess 호출로 넘어가고

            Log.d("MainActivity:requirePermissions", "all permissions granted.")
            onPermissionSuccess()
        }
        else {
            // 그렇지 않다면 시스템에 권한을 요청합니다.

            Log.d("MainActivity:requirePermissions", "asking for ${notGrantedPermissions.size} permissions.")

            ActivityCompat.requestPermissions(
                this@MainActivity,
                notGrantedPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * 이 앱이 왜 이 권한을 가져야 하는지 설명합니다.
     */
    private fun explainUserWhyWeNeedThisPermission(permission: String) {
        // TODO: 제대로 만들기
        Notify(this).short("Please give me $permission")
    }

    /**
     * 시스템에 권한을 요청한 뒤 결과를 넘겨받는 콜백입니다.
     */
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

    /**
     * 모든 권한을 확보했을 때에 수행할 행동을 정의합니다.
     * 실질적인 진입점입니다.
     */
    private fun onPermissionSuccess() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        bindUi()

        setUpUi()

        /**
         * 메시지가 먼저이니까 메시지 탭 선택해줍니다.
         */
        viewModel.setSelectedTabMenuItemId(R.id.menu_item_navigation_message)
    }

    /**
     * 권한을 하나라도 얻지 못했을 때에 수행할 행동을 정의합니다.
     */
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

    /**
     * 뷰모델을 뷰와 연결합니다. (binding)
     */
    private fun bindUi() {
        viewModel.getSelectedTabMenuItemId().observe(this, object: Observer<Int> {
            override fun onChanged(t: Int?) {
                if (t == null) return

                switchFragmentByNavigationItemId(t)
            }
        })
    }

    /**
     * 기타 뷰 설정.
     */
    private fun setUpUi() {
        nav_view.setOnNavigationItemSelectedListener { item ->
            viewModel.setSelectedTabMenuItemId(item.itemId)
        }
    }

    /**
     * 네비게이션 아이템의 id에 맞는 프래그먼트로 교체합니다.
     * id는
     * R.id.menu_item_navigation_message(-> messageListFragment),
     * R.id.menu_item_navigation_map(-> mapFragment)
     * 중 하나입니다.
     *
     * @return 위 두 id가 아닌 것이 인자로 들어오면 false를 반환. 그렇지 않으면 true.
     */
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
    private fun dumpThread() {

        val c = contentResolver.query(Uri.parse("content://mms-sms/conversations?simple=true"), null, null, null, "_id DESC") ?: throw RuntimeException()
        val dump = QueryHelper.dumpCursor(c)
        c.close()

        val p = Popup(this).withTitle("thread table dump:")

        dump.forEach { map ->
            map.forEach { k, v ->
                p.withMoreMessage("$k: $v\n")
            }
            p.withMoreMessage("\n")
        }

        p.show()
    }
    private fun showSmsInbox() {
        val p = Popup(this).withTitle("Messages")
        val uri = Uri.parse("content://sms")
        val projection = arrayOf("thread_id", "_id", "type", "address", "body", "person")
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
        private const val PERMISSION_REQUEST_CODE = 99

        /**
         * 이 앱에서 필요한 권한들.
         */
        private val PERMISSIONS_OF_THIS_APP = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS
        )
    }
}
