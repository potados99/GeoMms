package com.potados.geomms.util

import com.google.gson.annotations.SerializedName
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

class Reflection {
    companion object {
        /**
         * https://stackoverflow.com/a/35539628
         * 땡큐
         */
        @Suppress("UNCHECKED_CAST")
        fun <R> readInstanceProperty(instance: Any, propertyName: String): R {
            val property = instance::class.memberProperties
                .first { it.name == propertyName } as KProperty1<Any, *>

            return property.get(instance) as R
        }
    }
}