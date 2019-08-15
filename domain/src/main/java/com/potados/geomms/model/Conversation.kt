package com.potados.geomms.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class Conversation(
    @PrimaryKey var id: Long = 0,
    @Index var archived: Boolean = false,
    @Index var blocked: Boolean = false,
    @Index var pinned: Boolean = false,
    var recipients: RealmList<Recipient> = RealmList(),
    var count: Int = 0,
    var date: Long = 0,
    var snippet: String = "",
    var read: Boolean = true,
    var me: Boolean = false,
    var draft: String = "",
    var name: String = "" /* 그룹 채팅의 경우, 사용자가 별도로 이름 지정 가능. */

) : RealmObject() {

    fun getTitle(): String = name.takeIf { it.isNotBlank() }
            ?: recipients.joinToString { recipient -> recipient.getDisplayName() }

}
