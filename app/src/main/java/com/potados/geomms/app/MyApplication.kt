package com.potados.geomms.app

import android.app.Application
import com.potados.geomms.core.di.myModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(myModules)
        }

        Timber.plant(Timber.DebugTree())
    }
}