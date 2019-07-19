package com.potados.geomms.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.data.entity.LocationSupportConnection
import com.potados.geomms.data.entity.LocationSupportPerson
import com.potados.geomms.data.repository.ContactRepository
import com.potados.geomms.protocol.LocationSupportManager
import com.potados.geomms.protocol.LocationSupportProtocol
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * MapFragment를 보조할 뷰모델입니다.
 * 지도에 꽂히는 핀 정보, 친구 목록, 연결 정보 등을 가집니다.
 */
class MapViewModel : ViewModel(), KoinComponent {

    /**
     * LocationSupport 매니저
     */
    private val locationSupportManager: LocationSupportManager by inject()

    private val contactRepo: ContactRepository by inject()

    fun onMessageReceived(address: String, body: String) {
        val packet = LocationSupportProtocol.parse(body) ?: return

        locationSupportManager.onPacketReceived(packet, address)

        Log.d("MapViewModel:onMessageReceived", "$body from $address")
    }

    fun getConnections(): LiveData<List<LocationSupportConnection>> =
        locationSupportManager.getConnections()

    fun requestNewConnection(phoneNumber: String) {
        locationSupportManager.requestNewConnection(
            LocationSupportPerson(
                contactRepo.getContactNameByPhoneNumber(phoneNumber) ?: phoneNumber,
                phoneNumber
            )
        )
    }


}