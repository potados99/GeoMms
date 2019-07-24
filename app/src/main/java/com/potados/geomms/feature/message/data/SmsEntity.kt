package com.potados.geomms.feature.message.data

class SmsEntity {
    var body: String = ""
        private set

    var address: String = ""
        private set

    var date: Long = 0
        private set

    fun body(body: String): SmsEntity = this.apply { this.body = body }
    fun address(address: String): SmsEntity = this.apply { this.address = address }
    fun date(address: String): SmsEntity = this.apply { this.date = date }
}
