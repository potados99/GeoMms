package com.potados.geomms.di

import com.potados.geomms.data.MessageRepository
import com.potados.geomms.data.MessageRepositoryImpl
import com.potados.geomms.data.QueryInfoRepository
import com.potados.geomms.data.QueryInfoRepositoryImpl
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