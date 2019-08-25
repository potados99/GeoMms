package com.potados.geomms

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.f2prateek.rx.preferences2.RxSharedPreferences
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

    val permissions = arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.READ_PHONE_STATE
    )

    val modules = module {

        /**********************************************************
         * Common
         **********************************************************/

        /** Content Resolver */
        single { androidContext().contentResolver }


        /**********************************************************
         * Service
         **********************************************************/

        /** Location Support Service */
        single {
            LocationSupportServiceImpl(
                conversationRepo = get(),
                locationRepo = get(),
                keyManager = get()
            ) as LocationSupportService
        }



        /**********************************************************
         * Manager
         **********************************************************/

        /** Active Conversation Manager */
        single { ActiveConversationManagerImpl() as ActiveConversationManager }

        /** Key Manager */
        single { KeyManagerImpl() as KeyManager }

        /** Permission Manager */
        single { PermissionManagerImpl(context = get(), permissions = permissions) as PermissionManager }



        /**********************************************************
         * Mapper
         **********************************************************/

        /** Cursor To Contact */
        single { CursorToContactImpl(context = get(), permissionManager = get()) as CursorToContact }

        /** Cursor To Conversation */
        single { CursorToConversationImpl(context = get(), permissionManager = get()) as CursorToConversation }

        /** Cursor To Message */
        single {
            CursorToMessageImpl(
                context = get(),
                cursorToPart = get(),
                keys = get(),
                permissionManager = get()
            ) as CursorToMessage
        }

        /** Cursor To Part */
        single { CursorToPartImpl(context = get()) as CursorToPart }

        /** Cursor To Recipient */
        single { CursorToRecipientImpl(context = get(), permissionManager = get()) as CursorToRecipient }



        /**********************************************************
         * Preference
         **********************************************************/

        /** Preferences */
        single { Preferences(RxSharedPreferences.create(get())) }


        /**********************************************************
         * Repository
         **********************************************************/

        /** Conversation Repository */
        single {
            ConversationRepositoryImpl(
                context = get(),
                cursorToConversation = get(),
                cursorToRecipient = get()
            ) as ConversationRepository
        }

        /** Image Repository */
        single { ImageRepostoryImpl(context = get()) as ImageRepository }

        /** Message Repository */
        single {
            MessageRepositoryImpl(
                activeConversationManager = get(),
                context = get(),
                messageIds = get(),
                imageRepository = get(),
                prefs = get(),
                syncRepository = get()
            ) as MessageRepository
        }

        /** Sync Repository */
        single {
            SyncRepositoryImpl(
                contentResolver = get(),
                conversationRepo = get(),
                cursorToConversation = get(),
                cursorToMessage = get(),
                cursorToRecipient = get(),
                cursorToContact = get(),
                keys = get()
            ) as SyncRepository
        }


        /**********************************************************
         * Use Case
         **********************************************************/

        /** Mark Delivered */
        /** Mark Delivery Failed */
        /** Mark Failed */

        /** Mark Read */
        single {
            MarkRead(
                conversationRepo = get(),
                messageRepo = get(),
                notificationManager = get(),
                updateBadge = get()
            )
        }

        /** Mark Sent */
        single{ MarkSent(messageRepo = get()) }

        /** Receive Mms */
        single {
            ReceiveMms(
                activeConversationManager = get(),
                conversationRepo = get(),
                syncRepo = get(),
                messageRepo = get(),
                notificationManager = get(),
                updateBadge = get()
            )
        }

        /** Receive Packet */
        single {
            ReceivePacket(
                service = get()
            )
        }

        /** Receive Sms */
        single {
            ReceiveSms(
                conversationRepo = get(),
                messageRepo = get(),
                notificationManager = get(),
                updateBadge = get()
            )
        }

        /** Send Message */
        single {
            SendMessage(
                context = get(),
                conversationRepo = get(),
                messageRepo = get()
            )
        }

        /** Sync Message */

        /** Sync Messages */
        single { SyncMessages(syncRepo = get(), updateBadge = get()) }

        /** Update Badges */
        single { UpdateBadge() }




        /**********************************************************
         * 위치공유 // TODO
         **********************************************************/

        /** 현재 위치 저장소 */
        single {
            LocationRepositoryImpl(
                androidContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager,
                5000,
                5.0f
            ) as LocationRepository
        }
    }

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

        startKoin {
            androidContext(getContext())
            modules(modules)
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
        assert(isValid)

        val isNotValid = !service.isValidPacket("blah..")
        assert(isNotValid)

        Timber.i("test success")
    }

    @Test
    fun receivePacketTest() {
        Timber.i("receive packet test")

        val service : LocationSupportService by inject()
        val receivePacket: ReceivePacket by inject()

        val incomingRequests = service.getIncomingRequests()

        Timber.i("incoming requests: ${incomingRequests.size}")

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
        Timber.i("incoming requests: ${incomingRequests.size}")
        val request = incomingRequests.find { it.recipient?.address == other.address } ?: throw RuntimeException()
        service.acceptConnectionRequest(request)

        Timber.i("established: ${service.getConnection(12345).id}")

        Timber.i("test success")
    }


}
