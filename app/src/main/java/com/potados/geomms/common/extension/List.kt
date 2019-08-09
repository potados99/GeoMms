package com.potados.geomms.common.extension

fun List<*>.serialize(): String {
    val builder = StringBuilder()

    this.forEach {
        builder.append("${it.toString()}, ")
    }

    return builder.trim(',', ' ').toString()
}