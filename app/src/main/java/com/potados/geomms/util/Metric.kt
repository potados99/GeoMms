package com.potados.geomms.util

import android.location.Location

class Metric(private val meter: Long) {

    fun toMeter(): Long = meter

    fun toKiloMeter(): Double = meter.toDouble() / 1000.0

    val kiloMeterPart = meter / 1000
    val meterPart = meter % 1000

    fun toShortenString(): String {
        return if (kiloMeterPart == 0L) meterPart.toString() + "m"
        else toKiloMeter().toString() + "km"
    }

    override fun toString(): String {
        return toShortenString()
    }

    companion object {
        fun fromMeter(meter: Long): Metric {
            return Metric(meter)
        }

        fun fromKiloMeter(kiloMeter: Long): Metric {
            return Metric(kiloMeter * 1000)
        }

        fun fromDistanceBetween(here: Location, there: Location): Double {
            return here.bearingTo(there).toDouble()
        }
    }
}