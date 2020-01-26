/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.common.base

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.base.Failable
import com.potados.geomms.common.extension.setVisible
import io.realm.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

abstract class BaseRealmAdapter<T: RealmModel>
    : RealmRecyclerViewAdapter<T, BaseViewHolder>(null, true), Failable, KoinComponent
{

    protected val context: Context by inject()

    private val failure = MutableLiveData<Failable.Failure>()

    final override fun setFailure(failure: Failable.Failure) {
        this.failure.postValue(failure)
        Timber.w("Failure is set: ${failure.message}")
    }

    final override fun getFailure(): MutableLiveData<Failable.Failure> {
        return failure
    }

    override fun fail(@StringRes message: Int, vararg formatArgs: Any?, show: Boolean) {
        setFailure(Failable.Failure(context.getString(message, *formatArgs), show))
    }

    var emptyView: View? = null
        set(value) {
            if (field === value) return

            field = value
            value?.setVisible(data?.isLoaded == true && data?.isEmpty() == true)
        }

    /**
     * Opposite to emptyView.
     * Shown when data exists.
     * Useful when we want to hide something when data does not exist.
     */
    var companionView: View? = null
        set(value) {
            if (field === value) return

            field = value
            value?.setVisible(!(data?.isLoaded == true && data?.isEmpty() == true))
        }

    private val emptyListener: (OrderedRealmCollection<T>) -> Unit = { data ->
        emptyView?.setVisible(data.isLoaded && data.isEmpty())
        companionView?.setVisible(!(data.isLoaded && data.isEmpty()))
    }

    override fun updateData(data: OrderedRealmCollection<T>?) {
        if (getData() === data) return

        removeListener(getData())
        addListener(data)

        data?.run(emptyListener)

        super.updateData(data)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        addListener(data)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        removeListener(data)
    }

    protected open fun addListener(data: OrderedRealmCollection<T>?) {
        when (data) {
            is RealmResults<T> -> data.addChangeListener(emptyListener)
            is RealmList<T> -> data.addChangeListener(emptyListener)
        }
    }

    protected open fun removeListener(data: OrderedRealmCollection<T>?) {
        when (data) {
            is RealmResults<T> -> data.removeChangeListener(emptyListener)
            is RealmList<T> -> data.removeChangeListener(emptyListener)
        }
    }
}