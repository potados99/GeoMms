package com.potados.geomms.feature.data.implementation

import android.content.ContentResolver
import android.provider.Telephony
import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.data.entity.ShortMessage
import com.potados.geomms.feature.data.entity.SmsThread
import com.potados.geomms.feature.data.repository.MessageRepository
import com.potados.geomms.feature.data.repository.QueryInfoRepository
import com.potados.geomms.core.util.QueryHelper
import com.potados.geomms.feature.failure.MessageFailure
import kotlin.Exception
import android.content.ContentValues

class MessageRepositoryImpl(
    private val resolver: ContentResolver,
    private val queryRepo: QueryInfoRepository
) : MessageRepository {

    override fun getSmsThreads(): Either<Failure, List<SmsThread>> =
        try {
            Either.Right(
                QueryHelper.queryToCollection(
                    resolver,                                                           /* 컨텐츠 리졸버. */
                    queryRepo.getConversationsUri(),                                    /* 대화방(thread)이 모여있는 uri. */
                    queryRepo.getThreadsColumns(),                                      /* threads 테이블 중 사용할 column. */
                    queryRepo.getConversationsQuerySelection().selection(),          /* 선택 조건문. */
                    queryRepo.getConversationsQuerySelection().selectionArgs(),      /* 선택 조건문에 쓰일 값들. */
                    queryRepo.getConversationsQueryOrder()                              /* 정렬 조건. */
                )
            )
        } catch (e: Exception) {
            Either.Left(MessageFailure.QueryFailure())
        }

    override fun getSmsThreadById(id: Long): Either<Failure, SmsThread> =
        try {
            Either.Right(
                QueryHelper.queryToCollection<Collection<SmsThread>>(
                    resolver,                                                           /* 컨텐츠 리졸버. */
                    queryRepo.getConversationsUri(),                                    /* 대화방(thread)이 모여있는 uri. */
                    queryRepo.getThreadsColumns(),                                      /* threads 테이블 중 사용할 column. */
                    queryRepo.getConversationsQuerySelection(id).selection(),        /* 특정 id인 대화방만 가져옴. */
                    queryRepo.getConversationsQuerySelection(id).selectionArgs(),    /* 그 특정 id가 이 배열에 들어있을 것임. */
                    queryRepo.getConversationsQueryOrder()                              /* 정렬 조건 */
                ).first() /* 어차피 결과는 하나만 나올 것. 없으면 NoSuchElementException 유발. */
            )
        } catch (e: Exception) {
            Either.Left(MessageFailure.QueryFailure())
        }


    override fun getMessagesFromSmsThread(thread: SmsThread): Either<Failure, List<ShortMessage>> =
        try {
            Either.Right(
                QueryHelper.queryToCollection(
                    resolver,                                                           /* 컨텐츠 리졸버. */
                    queryRepo.getMessagesUriOfThread(thread.id),                        /* 특정 대화방에 해당하는 메시지들이 모여있는 uri. */
                    queryRepo.getSmsColumns(),                                          /* 사용할 sms 테이블 column. */
                    queryRepo.getMessagesQuerySelection().selection(),               /* 메시지 선택 조건문. */
                    queryRepo.getMessagesQuerySelection().selectionArgs(),           /* 조건문에 쓰일 값들. */
                    queryRepo.getMessageQueryOrder()                                    /* 정렬 조건. */
                )
            )
        } catch (e: Exception) {
            Either.Left(MessageFailure.QueryFailure())
        }


    override fun readConversation(thread: SmsThread): Either<Failure, UseCase.None> {
        if (thread.isAllRead()) return Either.Right(UseCase.None())
        if (thread.messageCount == 0L) return Either.Right(UseCase.None())

        return try {
            val numberOfUpdatedRows = resolver.update(
                queryRepo.getMessagesUriOfThread(thread.id),
                ContentValues().apply { put(Telephony.Sms.READ, true) },
                queryRepo.getUnreadMessagesQuerySelection().selection(),
                queryRepo.getUnreadMessagesQuerySelection().selectionArgs()
            )

            val success = (numberOfUpdatedRows != 0)

            if (success) Either.Right(UseCase.None())
            else Either.Left(MessageFailure.UpdateFailure())

        } catch (e: Exception) {
            Either.Left(MessageFailure.UpdateFailure())
        }
    }

}