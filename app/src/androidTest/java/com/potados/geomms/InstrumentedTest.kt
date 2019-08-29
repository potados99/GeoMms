package com.potados.geomms

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.potados.geomms.injection.myModules
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Packet
import com.potados.geomms.model.Recipient
import com.potados.geomms.repository.*
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.service.LocationSupportServiceImpl
import com.potados.geomms.usecase.*
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.inject
import org.koin.test.KoinTest
import timber.log.Timber


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest : KoinTest {

    var started = false

    fun getContext(): Context = ApplicationProvider.getApplicationContext()

    fun initOnce() {
        if (started) return
        started = true

        Timber.plant(Timber.DebugTree())
        Timber.i("Hi!")

        Realm.init(getContext())
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build())

        stopKoin()
        startKoin {
            androidContext(getContext())
            modules(myModules)
        }
    }

    init {
        initOnce()
    }

    @Test
    fun injectTest() {
        val service : LocationSupportService by inject()

        val isValid = service.isValidPacket(LocationSupportServiceImpl.GEO_MMS_PREFIX)
        assert(isValid == true)

        val isNotValid = service.isValidPacket("blah..") == false
        assert(isNotValid)
    }

    @Test
    fun requestAcceptTest() {
        val service : LocationSupportService by inject()

        service.disconnectAll()

        // receive request
        service.receivePacket("01029222661", "[GEOMMS]1:12345:1800000")

        val req = service.getRequest(12345L, inbound = true)

        assert(req != null)
        if (req == null) return

        service.acceptConnectionRequest(req)

        val connection = service.getConnection(12345L, false)

        assert(connection != null)
        if (connection == null) return

        service.requestDisconnect(12345L)

        val connections = service.getConnections()

        assert(connections != null && connections.isEmpty())
    }

}