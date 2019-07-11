package com.potados.geomms.util

import java.io.Serializable

/**
 * Bundle에 넣어서 전달하기 위한 직렬화 가능한 컨테이너 클래스입니다.
 */
class SerializableContainer<T> : Serializable {

    private var data: T? = null

    fun setData(data: T) {
        this.data = data
    }

    fun getData(): T {
        return this.data ?: throw IllegalStateException("Data is not set yet.")
    }

}