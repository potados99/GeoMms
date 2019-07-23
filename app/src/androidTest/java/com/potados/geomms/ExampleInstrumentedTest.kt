package com.potados.geomms

import android.net.Uri
import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.potados.geomms.core.util.Popup
import com.potados.geomms.core.util.QueryHelper
import com.potados.geomms.feature.protocol.LocationSupportProtocol

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    //@Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.potados.geomms", appContext.packageName)
    }

    @Test
    fun locationSupportTest() {

        val locationData = LocationSupportProtocol.createDataPacket(32767, 127.12345, 37.12345)

        val serialized = LocationSupportProtocol.serialize(locationData)
        if (serialized == null) {
            Log.d("locationSupportTest", "serialization failed.")
            return
        }

        Log.d("locationSupportTest", "serialized: $serialized")



        val parsed = LocationSupportProtocol.parse(serialized)
        if (parsed == null) {
            Log.d("locationSupportTest", "parse failed.")
            return
        }

        parsed.let {
            Log.d("locationSupportTest", "type: ${it.type}")
            Log.d("locationSupportTest", "id: ${it.id}")

            Log.d("locationSupportTest", "span: ${it.span}")
            Log.d("locationSupportTest", "lat: ${it.latitude}")
            Log.d("locationSupportTest", "long: ${it.longitude}")
        }
    }



    /**
     * TODO: 출시할 때에는 없애기
     * 실험용 함수입니다.
     */
    /*
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
    */
}
