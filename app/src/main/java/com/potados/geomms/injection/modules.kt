package com.potados.geomms.injection

import android.content.Context
import android.location.LocationManager
import android.telephony.SmsManager
import com.potados.geomms.data.implementation.ContactRepositoryImpl
import com.potados.geomms.data.implementation.MessageRepositoryImpl
import com.potados.geomms.data.implementation.QueryInfoRepositoryImpl
import com.potados.geomms.data.repository.*
import com.potados.geomms.protocol.LocationSupportManager
import com.potados.geomms.protocol.LocationSupportManagerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val myModules = module {

    single {
        /**
         * 안드로이드 컨텐츠 제공자
         */
        androidContext().contentResolver
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