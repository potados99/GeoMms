/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

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