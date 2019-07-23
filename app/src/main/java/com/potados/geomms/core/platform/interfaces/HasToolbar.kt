package com.potados.geomms.core.platform.interfaces

/**
 * Indicate that the child might has toolbar.
 */
interface HasToolbar {
    fun toolbarId(): Int?
    fun toolbarMenuId(): Int?
}