package com.potados.geomms.data

import android.content.ContentResolver
import android.content.ContentUris
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.util.QueryHelper

class MessageRepositoryImpl(
    private val resolver: ContentResolver,
    private val queryRepo: QueryInfoRepository
) : MessageRepository {

    override fun getSmsThreads(): List<SmsThread> =
        QueryHelper.queryToCollection(
            resolver,
            queryRepo.getConversationsUri(),
            queryRepo.getThreadsColumns(),
            queryRepo.getConversationsQuerySelection(),
            queryRepo.getConversationsQueryOrder()
        )

    override fun getSmsThreadById(id: Long): SmsThread =
        QueryHelper.queryToCollection<Collection<SmsThread>>(
            resolver,
            queryRepo.getConversationsUri(),
            queryRepo.getThreadsColumns(),
            queryRepo.getConversationsQuerySelection(id),
            queryRepo.getConversationsQueryOrder()
        ).first()

    override fun getMessagesFromSmsThread(thread: SmsThread): List<ShortMessage> =
        QueryHelper.queryToCollection(
            resolver,
            queryRepo.getMessagesUriOfThread(thread.id),
            queryRepo.getSmsColumns(),
            queryRepo.getMessagesQuerySelection(),
            queryRepo.getMessageQueryOrder()
        )
}