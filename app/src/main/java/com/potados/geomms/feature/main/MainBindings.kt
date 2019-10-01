package com.potados.geomms.feature.main

import android.animation.ObjectAnimator
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.potados.geomms.R
import com.potados.geomms.common.extension.isVisible
import com.potados.geomms.repository.SyncRepository
import kotlinx.android.synthetic.main.main_syncing.view.*
import timber.log.Timber

@BindingAdapter("defaultSmsState")
fun setDefaultSmsState(layout: ConstraintLayout, isDefaultSms: Boolean) {
    // show when this is not a default sms app
    layout.isVisible = !isDefaultSms

    Timber.i("Default sms state updated")
}

@BindingAdapter("syncState")
fun setSyncState(layout: LinearLayout, state: SyncRepository.SyncProgress) {
    when (state) {
        is SyncRepository.SyncProgress.Idle -> {
            layout.isVisible = false
        }
        is SyncRepository.SyncProgress.Running -> {
            with(layout) {
                isVisible = true
                title.text = context.getString(R.string.main_syncing, state.progress, state.max)
                progress.max = state.max
                ObjectAnimator
                    .ofInt(progress, "progress", 0, 0)
                    .apply { setIntValues(progress.progress, state.progress) }
                    .start()
                progress.isIndeterminate = state.indeterminate
            }
        }
    }

    Timber.i("sync state updated")
}