package com.potados.geomms.core.di

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.telephony.SmsManager
import com.potados.geomms.core.navigation.Navigator
import com.potados.geomms.core.util.PermissionChecker
import com.potados.geomms.feature.data.implementation.ContactRepositoryImpl
import com.potados.geomms.feature.data.implementation.MessageRepositoryImpl
import com.potados.geomms.feature.data.implementation.QueryInfoRepositoryImpl
import com.potados.geomms.feature.data.repository.*
import com.potados.geomms.feature.protocol.LocationSupportService
import com.potados.geomms.feature.protocol.LocationSupportServiceImpl
import com.potados.geomms.feature.usecase.GetConversations
import com.potados.geomms.feature.usecase.GetMessages
import com.potados.geomms.feature.usecase.ReadConversation
import com.potados.geomms.feature.usecase.SendSms
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

    /** 안드로이드 컨텐츠 제공자 */
    single { androidContext().contentResolver }

    /** 권한 checker */
    single { PermissionChecker(androidContext(), permissions) }

    /** 네비게이터 */
    single { Navigator(get()) }

    /** 연락처 저장소 */
    single { ContactRepositoryImpl(get()) as ContactRepository }

    /** 쿼리 저장소 */
    single { QueryInfoRepositoryImpl() as QueryInfoRepository }

    /** 메시지 저장소 */
    single { MessageRepositoryImpl(get(), get()) as MessageRepository }

    /** 대화목록 가져오는 use case */
    single { GetConversations(get()) }

    /** 대화방 메시지 가져오는 use case */
    single { GetMessages(get()) }

    /** SMS 보내는 use case */
    single { SendSms(androidContext(), SmsManager.getDefault()) }

    /** 대화 읽는 use case */
    single { ReadConversation(get()) }

    /** LocationSupportService 객체 */
    single {
        LocationSupportServiceImpl(
            androidContext(),
            get(),
            androidContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            ) as LocationSupportService
    }
}