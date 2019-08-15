package com.potados.geomms.model

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class ConnectionRequest(
    /**
     * Request의 id는 여기에서 파생된 Connection에 사용됩니다.
     */
    @PrimaryKey var connectionId: Long = 0,
    var recipient: Recipient = Recipient(),
    @Index var isInbound: Boolean = false,
    var date: Long = 0,
    var duration: Long = 0

) : RealmObject() {

}