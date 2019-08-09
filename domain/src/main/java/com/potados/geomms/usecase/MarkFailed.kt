package com.potados.geomms.usecase

import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.core.functional.Result
import com.potados.geomms.manager.NotificationManager
import com.potados.geomms.repository.MessageRepository
import io.reactivex.Flowable

class MarkFailed(
    private val messageRepo: MessageRepository,
    private val notificationMgr: NotificationManager
) : UseCase<MarkFailed.Params>() {

    data class Params(val id: Long, val resultCode: Int)

    override fun buildObservable(params: Params): Flowable<*> =
        Flowable.just(Unit)
            .doOnNext { messageRepo.markFailed(params.id, params.resultCode) }
            .doOnNext { notificationMgr.notifyFailed(params.id) }
}