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
import com.potados.geomms.feature.protocol.LocationSupportManager
import com.potados.geomms.feature.protocol.LocationSupportManagerImpl
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

    single {
        /**
         * 안드로이드 컨텐츠 제공자
         */
        androidContext().contentResolver
    }

    single {
        /**
         * 권한 checker
         */
        PermissionChecker(androidContext(), permissions)
    }

    single {
        /**
         * 네비게이터
         */
        Navigator(get())
    }

    single {
        /**
         * 연락처에 접근하기 위한 저장소
         */
        ContactRepositoryImpl(androidContext().contentResolver) as ContactRepository
    }

    single {
        /**
         * 쿼리와 관련된 uri 또는 기타 옵션이 담긴 저장소
         */
        QueryInfoRepositoryImpl() as QueryInfoRepository
    }

    single {
        /**
         * 메시지 저장소
         */
        MessageRepositoryImpl(
            androidContext().contentResolver,
            get()
        ) as MessageRepository /* 타입캐스팅 필쑤!! */
    }

    single {
        /**
         * LocationSupportManager 객체
         */
        LocationSupportManagerImpl(
            androidContext(),
            SmsManager.getDefault(),
            androidContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            ) as LocationSupportManager
    }
}