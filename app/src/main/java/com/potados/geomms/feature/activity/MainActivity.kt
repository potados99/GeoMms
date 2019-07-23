package com.potados.geomms.feature.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.potados.geomms.feature.fragment.MapFragment
import com.potados.geomms.feature.fragment.ConversationListFragment
import com.potados.geomms.R
import com.potados.geomms.core.platform.BaseFragment
import com.potados.geomms.core.platform.NavigationBasedActivity
import com.potados.geomms.core.util.Notify
import com.potados.geomms.core.util.Popup
import com.potados.geomms.core.util.QueryHelper
import com.potados.geomms.feature.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

/**
 * 권한 획득과 기본 앱 설정 후 나타나는 주 액티비티입니다.
 */
class MainActivity : NavigationBasedActivity() {

    /**
     * NavigationBasedActivity 설정들.
     */
    override fun layoutId(): Int = R.layout.activity_main
    override fun toolbar(): Toolbar? = null
    override fun toolbarMenuId(): Int? = null
    override fun fragments(): Collection<BaseFragment> = mFragments
    override fun navigationMenu(): BottomNavigationView = nav_view
    override fun navigationMenuId(): Int = R.menu.bottom_nav_menu

    /** 사용할 프래그먼트들 */
    private val mFragments = listOf(ConversationListFragment(), MapFragment())

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
        fun callingIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}

