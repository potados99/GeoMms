package com.potados.geomms.core.platform.interfaces

import androidx.appcompat.widget.Toolbar

interface HasToolbar {
    fun toolbar(): Toolbar?
    fun toolbarMenuId(): Int?
}