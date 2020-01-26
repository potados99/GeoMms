/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
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

package com.potados.geomms.feature.compose

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable

class HeadlessSmsSendService() : Service(), Parcelable {
    constructor(parcel: Parcel) : this()

    override fun onBind(intent: Intent?): IBinder? {
        // not necessary
        return null
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HeadlessSmsSendService> {
        override fun createFromParcel(parcel: Parcel): HeadlessSmsSendService {
            return HeadlessSmsSendService(parcel)
        }

        override fun newArray(size: Int): Array<HeadlessSmsSendService?> {
            return arrayOfNulls(size)
        }
    }
}