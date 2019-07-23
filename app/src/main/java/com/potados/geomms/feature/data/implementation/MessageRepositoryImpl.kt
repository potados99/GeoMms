package com.potados.geomms.feature.data.implementation

import android.content.ContentResolver
import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.feature.data.entity.ShortMessage
import com.potados.geomms.feature.data.entity.SmsThread
import com.potados.geomms.feature.data.repository.MessageRepository
import com.potados.geomms.feature.data.repository.QueryInfoRepository
import com.potados.geomms.core.util.QueryHelper
import com.potados.geomms.feature.failure.MessageFailure
import kotlin.Exception

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
                    queryRepo.getConversationsQuerySelection().getSelection(),          /* 선택 조건문. */
                    queryRepo.getConversationsQuerySelection().getSelectionArgs(),      /* 선택 조건문에 쓰일 값들. */
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
                    queryRepo.getConversationsQuerySelection(id).getSelection(),        /* 특정 id인 대화방만 가져옴. */
                    queryRepo.getConversationsQuerySelection(id).getSelectionArgs(),    /* 그 특정 id가 이 배열에 들어있을 것임. */
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
                    queryRepo.getMessagesQuerySelection().getSelection(),               /* 메시지 선택 조건문. */
                    queryRepo.getMessagesQuerySelection().getSelectionArgs(),           /* 조건문에 쓰일 값들. */
                    queryRepo.getMessageQueryOrder()                                    /* 정렬 조건. */
                )
            )
        } catch (e: Exception) {
            Either.Left(MessageFailure.QueryFailure())
        }


}