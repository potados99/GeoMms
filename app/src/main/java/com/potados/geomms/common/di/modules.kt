package com.potados.geomms.common.di

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.telephony.SmsManager
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.feature.location.domain.LSService
import com.potados.geomms.feature.location.domain.LSServiceImpl
import com.potados.geomms.feature.location.data.LocationRepository
import com.potados.geomms.feature.location.data.LocationRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val permissions = arrayOf(
    Manifest.permission.READ_SMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.SEND_SMS,
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.INTERNET,
    Manifest.permission.READ_PHONE_STATE
)

val myModules = module {

    /**********************************************************
     * 공통
     **********************************************************/

    /** 권한 확인 */
    single { com.potados.geomms.manager.PermissionManager(androidContext(), permissions) }

    /** 네비게이터 */
    single { Navigator(get()) }

    /** 연락처 저장소 */
    single { ContactRepositoryImpl(get()) as ContactRepository }

    /** 메시지 서비스 */
    single { MessageServiceImpl(get(), SmsManager.getDefault()) as MessageService }


    /**********************************************************
     * 메시지
     **********************************************************/

    /** 쿼리 저장소 */
    single { QueryInfoRepositoryImpl() as QueryInfoRepository }

    /** 메시지 저장소 */
    single { MessageRepositoryImpl(get(), get(), get()) as MessageRepository }

    /** 대화목록 가져오는 use case */
    single { GetConversations(get()) }

    /** id로 대화목록 가져오는 use case */
    single { GetConversation(get()) }

    /** 대화방 메시지 가져오는 use case */
    single { GetMessages(get()) }

    /** SMS 보내는 use case */
    single { SendSms(get()) }

    /** 대화 읽는 use case */
    single { ReadConversation(get()) }



    /**********************************************************
     * 위치공유
     **********************************************************/

    /** 현재 위치 저장소 */
    single {
        LocationRepositoryImpl(
            androidContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager,
            5000,
            5.0f
        ) as LocationRepository
    }

    /** 위치공유서비스 */
    single { LSServiceImpl(get(), get()) as LSService }

    /** USE CASE 1 */
    single { HandleLocationMessage(get()) }

    /** USE CASE 2 */
    single { RequestConnection(get()) }

    /** USE CASE 3 */
    single { AcceptNewConnection(get()) }

    /** USE CASE 4 */
    single { RequestLocation(get()) }

    /** USE CASE 5 */
    single { SendLocation(get()) }

    /** USE CASE 6 */
    single { GetOutboundRequests(get()) }

    /** USE CASE 7 */
    single { GetInboundRequests(get()) }

    /** USE CASE 8 */
    single { GetConnections(get()) }
}