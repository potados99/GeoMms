package com.potados.geomms.feature.conversations

import android.animation.ObjectAnimator
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.potados.geomms.common.extension.isVisible
import com.potados.geomms.model.Conversation
import com.potados.geomms.repository.SyncRepository
import io.realm.RealmResults
import kotlinx.android.synthetic.main.main_syncing.view.*
import timber.log.Timber


@BindingAdapter("conversations")
fun setConversations(listView: RecyclerView, conversations: RealmResults<Conversation>) {
    (listView.adapter as? ConversationsAdapter)?.let {
        it.updateData(conversations)
        Timber.i("conversations updated.")
    } ?: Timber.w("adapter not set.")
}

@BindingAdapter("syncState")
fun setSyncState(syncLayout: LinearLayout, state: SyncRepository.SyncProgress) {
    when (state) {
        is SyncRepository.SyncProgress.Idle -> {
            syncLayout.isVisible = false
        }
        is SyncRepository.SyncProgress.Running -> {
            syncLayout.isVisible = true
            syncLayout.progress.max = state.max
            ObjectAnimator
                .ofInt(syncLayout.progress, "progress", 0, 0)
                .apply { setIntValues(syncLayout.progress.progress, state.progress) }
                .start()
            syncLayout.progress.isIndeterminate = state.indeterminate
        }
    }
}

@BindingAdapter("defaultSmsState")
fun setDefaultSmsState(layout: ConstraintLayout, isDefaultSms: Boolean) {
    // show when this is not a default sms app
    layout.isVisible = !isDefaultSms
}