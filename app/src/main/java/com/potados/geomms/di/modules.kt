package com.potados.geomms.di

import com.potados.geomms.data.*
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
        MessageRepositoryImpl(androidContext().contentResolver, get()) as MessageRepository /* 타입캐스팅 필쑤!! */
    }
}