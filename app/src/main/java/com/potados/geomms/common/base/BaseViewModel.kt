package com.potados.geomms.common.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.Disposables

open class BaseViewModel : ViewModel() {
    protected val disposable = Disposables.empty()

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}