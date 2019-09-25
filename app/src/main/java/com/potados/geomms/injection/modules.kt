/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.injection

import android.Manifest
import com.potados.geomms.common.manager.BottomSheetManagers
import com.potados.geomms.common.manager.NotificationManagerImplTest
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.filter.ContactFilter
import com.potados.geomms.filter.ConversationFilter
import com.potados.geomms.filter.PhoneNumberFilter
import com.potados.geomms.filter.RecipientFilter
import com.potados.geomms.manager.*
import com.potados.geomms.mapper.*
import com.potados.geomms.preference.MyPreferences
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
    single {
        Navigator(
            context = get(),
            permissionManager = get(),
            preferences = get(),
            syncRepo = get(),
            syncMessages = get()
        )
    }

    /** Scheduler */
    single { Scheduler() }

    /** Bottom Sheet Managers */
    single { BottomSheetManagers() }



    /**********************************************************
     * Service
     **********************************************************/

    /** Location Support Service */
    single {
        LocationSupportServiceImpl(
            context = get(),
            deleteMessages = get(),
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

    /** MyPreferences */
    single { MyPreferences(get()) }



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

    /** Clear All */
    single { ClearAll(service = get()) }

    /** Delete Conversations */
    single {
        DeleteConversations(
            conversationRepo = get(),
            notificationManager = get(),
            updateBadge = get()
        )
    }

    /** Delete Messages */
    single {
        DeleteMessages(
            conversationRepo = get(),
            messageRepo = get(),
            notificationManager = get(),
            updateBadge = get()
        )
    }

    /** Mark Delivered */
    single {
        MarkDelivered(messageRepo = get())
    }

    /** Mark Delivery Failed */
    single {
        MarkDeliveryFailed(messageRepo = get())
    }

    /** Mark Failed */
    single {
        MarkFailed(
            messageRepo = get(),
            notificationMgr = get()
        )
    }

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
    single {
        MarkSeen(messageRepo = get())
    }

    /** Mark Sent */
    single { MarkSent(messageRepo = get()) }

    /** Process Messages */
    single { ProcessMessages(service = get()) }

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
            service = get(),
            preference = get()
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
    single { SendUpdate(service = get()) }

    /** Sync Message */
    single {
        SyncMessage(
            conversationRepo = get(),
            syncManager = get(),
            updateBadge = get()
        )
    }

    /** Sync contacts */
    single { SyncContacts( syncRepo = get() ) }

    /** Sync Messages */
    single {
        SyncMessages(
            syncRepo = get(),
            clearAll = get(),
            processMessages = get(),
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