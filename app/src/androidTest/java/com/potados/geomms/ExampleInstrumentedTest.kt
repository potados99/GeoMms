package com.potados.geomms

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.potados.geomms.feature.message.data.MessageRepository
import com.potados.geomms.feature.message.data.MessageRepositoryImpl
import com.potados.geomms.feature.message.data.QueryInfoRepositoryImpl
import com.potados.geomms.feature.message.data.ConversationEntity

import org.junit.runner.RunWith

import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import com.potados.geomms.common.di.permissions


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val mRuntimePermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(*permissions)

    private fun getContext(): Context = InstrumentationRegistry.getInstrumentation().context

    /**
     * TODO: 출시할 때에는 없애기
     * 실험용 함수입니다.
     */
    /*
    private fun dumpThread() {

        val c = contentResolver.query(Uri.parse("content://mms-sms/conversations?simple=true"), null, null, null, "_id DESC") ?: throw RuntimeException()
        val dump = QueryHelper.dumpCursor(c)
        c.close()

        val p = Popup(this).withTitle("conversation table dump:")

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
        val projection = arrayOf("thread_id", "_id", "type", "address", "body", "recipient")
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
    */
}
