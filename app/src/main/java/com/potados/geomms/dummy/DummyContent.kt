package com.potados.geomms.dummy

import com.potados.geomms.data.entity.LocationSupportConnection
import com.potados.geomms.data.entity.LocationSupportPerson
import com.potados.geomms.util.DateTime
import com.potados.geomms.util.Metric
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 */
object DummyContent {

        fun getLocationSupportConnectionDummy(): List<LocationSupportConnection> {
            return listOf(
                LocationSupportConnection(
                    LocationSupportPerson("하늘이"),
                    DateTime.getCurrentTimeStamp() - 15000)
                    .apply {
                        lastSeenDistance = Metric(120)
                        lastReceivedTime = DateTime(DateTime.getCurrentTimeStamp() - 12000)
                    },

                LocationSupportConnection(
                    LocationSupportPerson("지은이"),
                    DateTime.getCurrentTimeStamp() - 9000)
                    .apply {
                        lastSeenDistance = Metric(350)
                        lastReceivedTime = DateTime(DateTime.getCurrentTimeStamp() - 62000)
                    }
            )
        }

}
