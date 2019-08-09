/**
 * UseCase.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.usecase

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Abstract class for Use Case (Interactor in terms of Clean Architecture).
 * Any use case in this application should implement this.
 */
abstract class UseCase<in Params> : Disposable {

    private val disposables: CompositeDisposable = CompositeDisposable()

    abstract fun buildObservable(params: Params): Flowable<*>

    operator fun invoke(params: Params, onComplete: () -> Unit = {}) {
        disposables += buildObservable(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(onComplete)
            .subscribe({}, Timber::w)
    }

    override fun dispose() {
        disposables.dispose()
    }

    override fun isDisposed(): Boolean {
        return disposables.isDisposed
    }

}