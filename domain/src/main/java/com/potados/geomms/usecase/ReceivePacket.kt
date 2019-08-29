package com.potados.geomms.usecase

import android.telephony.SmsMessage
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.preference.MyPreferences
import com.potados.geomms.service.LocationSupportService

class ReceivePacket(
    private val service: LocationSupportService,
    private val preference: MyPreferences
) : UseCase<Array<SmsMessage>>() {

    override fun run(params: Array<SmsMessage>): Result<*> =
        Result.of {
            if (params.isEmpty()) return@of
            if (!preference.receiveGeoMms) return@of

            val address = params[0].displayOriginatingAddress
            val body = params
                .mapNotNull { message -> message.displayMessageBody }
                .reduce { body, new -> body + new }

            service.receivePacket(address = address, body = body)
        }
}