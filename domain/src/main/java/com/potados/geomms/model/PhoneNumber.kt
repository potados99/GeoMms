package com.potados.geomms.model

import io.realm.RealmObject

open class PhoneNumber(
    var address: String = "",
    var type: String = ""
) : RealmObject()