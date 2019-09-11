
package com.potados.geomms.filter

import com.potados.geomms.base.FailableComponent

abstract class Filter<in T> : FailableComponent() {

    abstract fun filter(item: T, query: CharSequence): Boolean
}