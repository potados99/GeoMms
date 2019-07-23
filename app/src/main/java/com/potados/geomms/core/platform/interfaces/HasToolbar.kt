package com.potados.geomms.core.platform.interfaces

import androidx.appcompat.widget.Toolbar

/**
 * Indicate that the child might has toolbar.
 */
interface HasToolbar {
    fun toolbar(): Toolbar?
    fun toolbarMenuId(): Int?
}