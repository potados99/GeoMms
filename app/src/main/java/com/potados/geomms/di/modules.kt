package com.potados.geomms.di

import com.potados.geomms.data.MessageRepository
import com.potados.geomms.data.MessageRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val myModules = module {

    single {
        androidContext().contentResolver
    }

    single {
        MessageRepositoryImpl(androidContext().contentResolver) as MessageRepository /* 타입캐스팅 필쑤!! */
    }
}