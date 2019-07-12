package com.potados.geomms

import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.potados.geomms.protocol.LocationSupport

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
        val message = "[GEOMMS]12j7.24524:37.523414:235971849"

        val locationData = LocationSupport.parseMessasge(message)

        locationData?.let {
            Log.d("locationSupportTest", "lat: ${locationData.latitude}")
            Log.d("locationSupportTest", "long: ${locationData.longitude}")
            Log.d("locationSupportTest", "date: ${locationData.createDate}")

        } ?: Log.d("locationSupportTest", "failed to parse.")

        assertNotNull(locationData)
    }

}
