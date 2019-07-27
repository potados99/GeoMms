package com.potados.geomms.core.extension

fun List<*>.serialize() = StringBuilder().apply {
    this.forEach {
        append("$it, ")
    }
}.trim(',', ' ').toString()