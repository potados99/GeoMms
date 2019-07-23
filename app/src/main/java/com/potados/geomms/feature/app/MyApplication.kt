package com.potados.geomms.feature.app

import android.app.Application
import com.potados.geomms.core.di.myModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(myModules)
        }
    }
}