package com.potados.geomms.usecase

import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.repository.SyncRepository
import io.reactivex.Flowable

class SyncMessages(
    private val syncRepo: SyncRepository,
    private val updateBadge: UpdateBadge
) : UseCase<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> =
        Flowable.just(System.currentTimeMillis())
            .doOnNext { syncRepo.syncMessages() }
}