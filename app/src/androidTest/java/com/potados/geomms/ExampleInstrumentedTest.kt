package com.potados.geomms

import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.potados.geomms.protocol.LocationSupportManagerImpl
import com.potados.geomms.protocol.LocationSupportProtocol

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

}
