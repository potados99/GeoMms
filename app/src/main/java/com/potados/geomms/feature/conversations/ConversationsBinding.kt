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

package com.potados.geomms.feature.conversations

import android.animation.ObjectAnimator
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.R
import com.potados.geomms.common.extension.isVisible
import com.potados.geomms.model.Conversation
import com.potados.geomms.model.SearchResult
import com.potados.geomms.repository.SyncRepository
import io.realm.RealmResults
import kotlinx.android.synthetic.main.main_syncing.view.*
import timber.log.Timber


@BindingAdapter("conversations")
fun setConversations(listView: RecyclerView, conversations: RealmResults<Conversation>?) {
    conversations ?: return

    (listView.adapter as? ConversationsAdapter)?.let {
        it.updateData(conversations)
        Timber.i("Conversations updated.")
    } ?: Timber.w("Adapter not set.")
}

@BindingAdapter("searchResults")
fun setSearchResults(listView: RecyclerView, results: List<SearchResult>?) {
    results ?: return

    (listView.adapter as? SearchAdapter)?.let {
        it.data = results
        Timber.i("Search results updated.")
    } ?: Timber.w("Adapter not set.")
}
