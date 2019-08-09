package com.potados.geomms.usecase

import com.potados.geomms.repository.SyncRepository
import io.reactivex.Flowable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SyncMessages(
    private val syncRepo: SyncRepository,
    private val updateBadge: UpdateBadge
) : UseCase<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> =
        Flowable.just(System.currentTimeMillis())
            .doOnNext { syncRepo.syncMessages() }
            .map { startTime -> System.currentTimeMillis() - startTime }
            .map { elapsed -> TimeUnit.MILLISECONDS.toSeconds(elapsed) }
            .doOnNext { seconds -> Timber.v("Completed sync in $seconds seconds.") }
            .flatMap { updateBadge.buildObservable(Unit) }
}