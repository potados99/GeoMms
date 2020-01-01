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

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.model.Message
import com.potados.geomms.preference.MyPreferences
import com.potados.geomms.service.LocationSupportService

/**
 * Process MMS messages that contains geo-mms data.
 */
class ReceiveMMSPacket(
    private val service: LocationSupportService,
    private val preference: MyPreferences
) : UseCase<Message>() {

    override fun run(params: Message): Result<*> =
        Result.of {
            if (!preference.receiveGeoMms) return@of

            val body = params.parts
                .filter { it.type == "text/plain" }
                .map { it.text }
                .reduce { acc, part -> acc + part } ?: return@of

            service.receivePacket(address = params.address, body = body)
        }
}