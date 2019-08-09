package com.potados.geomms.common.base.interfaces

import androidx.fragment.app.Fragment

/**
 * Indicate that the child has fragment(s).
 */
interface HasFragments {
    fun fragments(): Array<out Fragment>
    fun fragmentContainerId(): Int
}
