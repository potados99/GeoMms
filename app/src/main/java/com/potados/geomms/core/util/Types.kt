package com.potados.geomms.core.util

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Types {
    companion object {
        /**
         * 특정 타입에 대한 타입 객체를 만들어서 던져줍니다.
         */
        inline fun <reified T> typeOf(): Type = object: TypeToken<T>() {}.type
    }
}