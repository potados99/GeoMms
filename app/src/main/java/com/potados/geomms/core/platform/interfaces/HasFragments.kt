package com.potados.geomms.core.platform.interfaces

import com.potados.geomms.core.platform.BaseFragment

/**
 * Indicate that the child has fragment(s).
 */
interface HasFragments {
    fun fragments(): Collection<BaseFragment>
}