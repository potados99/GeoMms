package com.potados.geomms.core.platform

import androidx.appcompat.widget.Toolbar

interface HasToolbar {
    fun toolbar(): Toolbar?
    fun toolbarMenuId(): Int?
}