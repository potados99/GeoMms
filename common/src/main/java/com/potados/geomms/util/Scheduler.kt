package com.potados.geomms.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import timber.log.Timber

class Scheduler {
    private val handler = Handler(Looper.getMainLooper())

    private val tasks = LinkedHashMap<Long, Runnable>()

    /**
     * Add periodic task, and return its id.
     */
    fun doOnEvery(taskId: Long, period: Long, task: () -> Unit) {
        val runnable = object: Runnable {
            override fun run() {
                task()
                handler.postDelayed(this, period)
            }
        }

        tasks[taskId] = runnable // overwrite
    }

    /**
     * Stop periodic task and remove it from handler.
     */
    fun doNoMore(id: Long) {
        tasks[id]?.let {
            handler.removeCallbacks(it)
        } ?: Timber.i("scheduled task of id $id not found. ignore")
    }

    fun getTaskIds(): Set<Long> {
        return tasks.keys
    }

    fun getTasks(): Map<Long, Runnable> {
        return tasks
    }
}