package com.potados.geomms.di

import com.potados.geomms.data.MessageRepository
import com.potados.geomms.data.MessageRepositoryImpl
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
         * 메시지 저장소
         */
        MessageRepositoryImpl(androidContext().contentResolver) as MessageRepository /* 타입캐스팅 필쑤!! */
    }
}