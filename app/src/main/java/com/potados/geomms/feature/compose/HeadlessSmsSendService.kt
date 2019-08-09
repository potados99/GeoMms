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