package com.potados.geomms.core.platform.interfaces

import com.potados.geomms.core.platform.BaseFragment

interface HasFragments {
    fun fragments(): Collection<BaseFragment>
}