package com.potados.geomms.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.repository.MessageRepository
import io.reactivex.Flowable

class MarkSent(
    private val messageRepo: MessageRepository
) : UseCase<Long>() {

    override fun buildObservable(params: Long): Flowable<*> =
        Flowable.just(Unit)
            .doOnNext{ messageRepo.markSent(params)}
}