package com.potados.geomms.util

import android.os.Handler
import android.os.Looper
import timber.log.Timber

/**
 * Manage tasks by id.
 */
class Scheduler {
    private val handler = Handler(Looper.getMainLooper())

    private val tasks = LinkedHashMap<Long, MutableList<Runnable>>()

    /**
     * Add periodic task, and return its id.
     */
    fun doOnEvery(taskId: Long, period: Long, task: () -> Unit) {
        val runnable = object: Runnable {
            override fun run() {
                task()
                Timber.v("Periodic task $taskId ran.")
                handler.postDelayed(this, period)
            }
        }

        with(runnable) {
            addRunnable(taskId, this)
            handler.post(this)
        }

        Timber.i("task $taskId scheduled.")
    }

    /**
     * Add task to be done at time.
     */
    fun doAtTime(taskId: Long, time: Long, task: () -> Unit) {
        val runnable = Runnable {
            task()
            Timber.v("On time task $taskId ran.")
        }

        with(runnable) {
            addRunnable(taskId, this)
            handler.postDelayed(this, time - System.currentTimeMillis())
        }

        Timber.i("task $taskId scheduled.")
    }

    /**
     * Stop periodic task and remove it from handler.
     */
    fun cancel(taskId: Long) {
        tasks[taskId]?.let {
            it.forEach(handler::removeCallbacks)
            Timber.i("task $taskId canceled.")

        } ?: Timber.i("scheduled task of taskId $taskId not found. ignore")
    }

    private fun addRunnable(taskId: Long, runnable: Runnable) {
        if (tasks[taskId] == null) {
            tasks[taskId] = mutableListOf()
        }

        tasks[taskId]?.add(runnable)
    }

    fun cancelAll() {
        tasks.keys.forEach { cancel(it) }
    }
}