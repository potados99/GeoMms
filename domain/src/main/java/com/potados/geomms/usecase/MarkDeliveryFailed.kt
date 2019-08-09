package com.potados.geomms.usecase

import com.potados.geomms.repository.MessageRepository
import io.reactivex.Flowable

class MarkDeliveryFailed(
    private val messageRepo: MessageRepository
) : UseCase<MarkDeliveryFailed.Params>() {

    data class Params(val id: Long, val resultCode: Int)

    override fun buildObservable(params: Params): Flowable<*> =
        Flowable.just(Unit)
            .doOnNext { messageRepo.markDeliveryFailed(params.id, params.resultCode) }
}