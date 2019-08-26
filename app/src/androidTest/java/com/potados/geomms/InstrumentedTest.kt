package com.potados.geomms

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.potados.geomms.injection.myModules
import com.potados.geomms.manager.*
import com.potados.geomms.mapper.*
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Packet
import com.potados.geomms.model.Recipient
import com.potados.geomms.preference.Preferences
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
import org.koin.dsl.module
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
        Timber.i("inject test")

        val service : LocationSupportService by inject()

        val isValid = service.isValidPacket(LocationSupportServiceImpl.GEO_MMS_PREFIX)
        assert(isValid == true)

        val isNotValid = service.isValidPacket("blah..") == false
        assert(isNotValid)

        Timber.i("test success")
    }

    @Test
    fun receivePacketTest() {
        Timber.i("receive packet test")

        val service : LocationSupportService by inject()
        val receivePacket: ReceivePacket by inject()

        val incomingRequests = service.getIncomingRequests()

        Timber.i("incoming requests: ${incomingRequests?.size}")

        val other = Recipient(
            id = 12345,
            address = "12345678"
        )
        val me = Recipient(
            id = 54321,
            address = "87654321"
        )
        Realm.getDefaultInstance().executeTransaction {
            it.insertOrUpdate(other)
            it.insertOrUpdate(me)
        }

        /**
         * Other side (12345678)
         */
        val othersRequest = ConnectionRequest(
            connectionId = 12345,
            recipient = me,
            isInbound = false,
            date = System.currentTimeMillis(),
            duration = 1800000 // 30 min
        )
        val othresRequestPacket = Packet.ofRequestingNewConnection(othersRequest)
        val serialized = service.serializePacket(othresRequestPacket)
        assert(serialized != null)



        /**
         * Me
         */
        service.receivePacket(other.address, serialized!!)
        Timber.i("incoming requests: ${incomingRequests?.size}")
        val request = incomingRequests?.find { it.recipient?.address == other.address } ?: throw RuntimeException()
        service.acceptConnectionRequest(request)

        Timber.i("established: ${service.getConnection(12345)?.id}")

        Timber.i("test success")
    }

    @Test
    fun searchTest() {
        val conversationRepo: ConversationRepository by inject()

        val query = "hi"

        val result = conversationRepo.searchConversations(query)

        result?.forEach {
            Timber.i(it.conversation.getTitle())
        }
    }

}
