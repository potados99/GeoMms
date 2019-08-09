package com.potados.geomms.usecase

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase
import io.reactivex.Flowable
import timber.log.Timber

class UpdateBadge : UseCase<Unit>() {
    override fun buildObservable(params: Unit): Flowable<*> =
        Flowable.just(params)
            .doOnNext { Timber.d("UpdateBadge:buildObservable") }
}