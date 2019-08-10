package com.potados.geomms.common.injection

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.location.LocationManager
import androidx.preference.PreferenceManager
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.feature.location.domain.LSService
import com.potados.geomms.feature.location.domain.LSServiceImpl
import com.potados.geomms.feature.location.data.LocationRepository
import com.potados.geomms.feature.location.data.LocationRepositoryImpl
import com.potados.geomms.preference.Preferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.potados.geomms.manager.*
import com.potados.geomms.mapper.*
import com.potados.geomms.repository.*

val permissions = arrayOf(
    Manifest.permission.READ_SMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.SEND_SMS,
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.INTERNET,
    Manifest.permission.READ_PHONE_STATE
)

val myModules = module {

    /**********************************************************
     * Common
     **********************************************************/

    /** Content Resolver */
    single { androidContext().contentResolver }

    /** Navigator */
    single { Navigator(get()) }



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
    single { CursorToPartImpl(context = get()) as CursorToPartImpl }

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

    /** Conversation Repository */
    single { ConversationRepositoryImpl(context = get(), cursorToConversation = get(), cursorToRecipient = get()) }

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

    /** 위치공유서비스 */
    single { LSServiceImpl(get(), get()) as LSService }

}