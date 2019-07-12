package com.potados.geomms

import android.util.Log
import com.potados.geomms.protocol.LocationSupport
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun locationSupportTest() {
        val message = "[GEOMMS]127.24524:37.523414:235971849"

        val locationData = LocationSupport.parseMessasge(message)

        locationData?.let {
            println("lat: ${locationData.latitude}")
            println("long: ${locationData.longitude}")
            println("date: ${locationData.createDate}")

        } ?: println("failed to parse.")

        assertNotNull(locationData)
    }
}
