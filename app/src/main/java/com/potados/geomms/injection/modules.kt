package com.potados.geomms.injection

import android.Manifest
import androidx.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.potados.geomms.common.manager.NotificationManagerImplTest
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.filter.ContactFilter
import com.potados.geomms.filter.ConversationFilter
import com.potados.geomms.filter.PhoneNumberFilter
import com.potados.geomms.filter.RecipientFilter
import com.potados.geomms.manager.*
import com.potados.geomms.mapper.*
import com.potados.geomms.preference.Preferences
import com.potados.geomms.repository.*
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.service.LocationSupportServiceImpl
import com.potados.geomms.usecase.*
import com.potados.geomms.util.Scheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val permissions = arrayOf(
    Manifest.permission.READ_SMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.SEND_SMS,
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.INTERNET,
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.READ_PHONE_STATE
)

val myModules = module {

    /**********************************************************
     * Common
     **********************************************************/

    /** Content Resolver */
    single { androidContext().contentResolver }

    /** Navigator */
    single { Navigator(context = get(), permissionManager = get()) }


    /** Scheduler */
    single { Scheduler() }



    /**********************************************************
     * Service
     **********************************************************/

    /** Location Support Service */
    single {
        LocationSupportServiceImpl(
            context = get(),
            conversationRepo = get(),
            locationRepo = get(),
            scheduler = get(),
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

    /** Notification Manager */
    single {
        NotificationManagerImplTest(
            context = get(),
            conversationRepo = get(),
            messageRepo = get(),
            permissionManager = get()
    ) as NotificationManager
    }

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

    /** SharedPreference */
    single { PreferenceManager.getDefaultSharedPreferences(get()) }

    /** Preferences */
    single { Preferences(RxSharedPreferences.create(get())) }


    /**********************************************************
     * Repository
     **********************************************************/

    /** Contact Repository */
    single {
        ContactRepositoryImpl(
            context = get()
        ) as ContactRepository
    }

    /** Conversation Repository */
    single {
        ConversationRepositoryImpl(
            context = get(),
            cursorToConversation = get(),
            cursorToRecipient = get(),
            conversationFilter = get()
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

    /** Delete Conversations */
    single {
        DeleteConversations(
            conversationRepo = get(),
            notificationManager = get(),
            updateBadge = get()
        )
    }

    /** Delete Messages */

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

    /** Mark Seen */

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

    /** Send Update */
    single {
        SendUpdate(
            service = get()
        )
    }

    /** Sync Message */

    /** Sync Messages */
    single {
        SyncMessages(
            syncRepo = get(),
            service = get(),
            updateBadge = get()
        )
    }

    /** Update Badges */
    single { UpdateBadge() }



    /**********************************************************
     * Util
     **********************************************************/

    /** Date Formatter */
    single { DateFormatter(get()) }



    /**********************************************************
     * Filter
     **********************************************************/

    /** Contact Filter */
    single { ContactFilter(phoneNumberFilter = get()) }

    /** Conversation Filter */
    single { ConversationFilter(recipientFilter = get()) }

    /** Phone Number Filter */
    single { PhoneNumberFilter() }

    /** Recipient Filter */
    single {
        RecipientFilter(
            contactFilter = get(),
            phoneNumberFilter = get()
        )
    }



    /**********************************************************
     * Location Support
     **********************************************************/

    /** Location Repository */
    single { LocationRepositoryImpl(context = get()) as LocationRepository }

}