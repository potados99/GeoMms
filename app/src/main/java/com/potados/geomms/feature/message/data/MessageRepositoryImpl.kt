package com.potados.geomms.feature.message.data

import android.provider.Telephony
import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.core.util.QueryHelper
import kotlin.Exception
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.potados.geomms.feature.common.ContactRepository
import com.potados.geomms.feature.message.domain.Conversation
import com.potados.geomms.feature.message.domain.Sms

class MessageRepositoryImpl(
    private val context: Context,
    private val queryRepository: QueryInfoRepository,
    private val contactRepository: ContactRepository
) : MessageRepository {

    override fun getConversations(): Result<List<Conversation>> =
        try {
            Result.Success(
                QueryHelper.queryToCollection<List<ConversationEntity>>(
                    context.contentResolver,                                            /* 컨텐츠 리졸버. */
                    queryRepository.getConversationsUri(),                                    /* 대화방(thread)이 모여있는 uri. */
                    queryRepository.getThreadsColumns(),                                      /* threads 테이블 중 사용할 column. */
                    queryRepository.getConversationsQuerySelection().selection(),             /* 선택 조건문. */
                    queryRepository.getConversationsQuerySelection().selectionArgs(),         /* 선택 조건문에 쓰일 값들. */
                    queryRepository.getConversationsQueryOrder()                              /* 정렬 조건. */
                ).map { it.toConversations(contactRepository) }
            )
        } catch (e: Exception) {
            Log.e("MessageRepositoryImpl:getSmsThreads", e.message)

            Result.Error(e)
        }

    override fun removeConversations(conversation: Conversation): Result<None> =
        try {
            context.contentResolver.delete(
                queryRepository.getMessagesUriOfThreadId(conversation.id), null, null
            ).let { rowsDeleted ->
                if (rowsDeleted < 1) {
                    throw RuntimeException()
                }
            }

            Result.Success(None())
        } catch (e: Exception) {
            Log.e("MessageRepositoryImpl:removeSmsThread($conversation)", e.message)

            Result.Error(e)
        }

    override fun getConversationById(id: Long): Result<Conversation> =
        try {
            Result.Success(
                QueryHelper.queryToCollection<Collection<ConversationEntity>>(
                    context.contentResolver,                                            /* 컨텐츠 리졸버. */
                    queryRepository.getConversationsUri(),                                    /* 대화방(thread)이 모여있는 uri. */
                    queryRepository.getThreadsColumns(),                                      /* threads 테이블 중 사용할 column. */
                    queryRepository.getConversationsQuerySelection(id).selection(),           /* 특정 id인 대화방만 가져옴. */
                    queryRepository.getConversationsQuerySelection(id).selectionArgs(),       /* 그 특정 id가 이 배열에 들어있을 것임. */
                    queryRepository.getConversationsQueryOrder()                              /* 정렬 조건 */
                ).first().toConversations(contactRepository)
            )
        } catch (e: Exception) {
            Log.e("MessageRepositoryImpl:getSmsThreadById($id)", e.message)

            Result.Error(e)
        }

    override fun getMessagesInConversation(conversation: Conversation): Result<List<Sms>> =
        try {
            Result.Success(
                QueryHelper.queryToCollection(
                    context.contentResolver,                                            /* 컨텐츠 리졸버. */
                    queryRepository.getMessagesUriOfThreadId(conversation.id),                      /* 특정 대화방에 해당하는 메시지들이 모여있는 uri. */
                    queryRepository.getSmsColumns(),                                          /* 사용할 sms 테이블 column. */
                    queryRepository.getMessagesQuerySelection().selection(),                  /* 메시지 선택 조건문. */
                    queryRepository.getMessagesQuerySelection().selectionArgs(),              /* 조건문에 쓰일 값들. */
                    queryRepository.getMessageQueryOrder()                                    /* 정렬 조건. */
                )
            )
        } catch (e: Exception) {
            Log.e("MessageRepositoryImpl:getMessagesFromSmsThread($conversation)", e.message)

            Result.Error(e)
        }

    override fun removeSms(sms: Sms): Result<None> =
        try {
            context.contentResolver.delete(
                queryRepository.getMessageUriOfMessageId(sms.id), null, null
            ).let { rowsDeleted ->
                if (rowsDeleted < 1) {
                    throw RuntimeException()
                }
            }

            Result.Success(None())
        } catch (e: Exception) {
            Log.e("MessageRepositoryImpl:removeSms($sms)", e.message)

            Result.Error(e)
        }

    override fun markConversationAsRead(conversation: Conversation): Result<None> {
        if (conversation.allRead) return Result.Success(None())
        if (conversation.messageCount == 0L) return Result.Success(None())

        return try {
            val numberOfUpdatedRows = context.contentResolver.update(
                queryRepository.getMessagesUriOfThreadId(conversation.id),
                ContentValues().apply { put(Telephony.Sms.READ, true) },
                queryRepository.getUnreadMessagesQuerySelection().selection(),
                queryRepository.getUnreadMessagesQuerySelection().selectionArgs()
            )

            val success = (numberOfUpdatedRows != 0)

            if (success) Result.Success(None())
            else throw IllegalStateException("Number of updated rows is zero.")

        } catch (e: Exception) {
            Log.e("MessageRepositoryImpl:markSmsThreadAsRead(id: ${conversation.id})", e.message)

            Result.Error(e)
        }
    }
}