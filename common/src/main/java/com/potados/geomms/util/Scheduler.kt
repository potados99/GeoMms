/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
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

        addRunnable(taskId, runnable)
        handler.postDelayed(runnable, 1000) // Start first job after 1 sec.

        Timber.i("Task $taskId scheduled.")
    }

    /**
     * Add task to be done at time.
     */
    fun doAtTime(taskId: Long, time: Long, task: () -> Any?) {
        // Auto remove after invoked.
        val runnable = object: Runnable {
            override fun run() {
                task()
                Timber.v("On time task $taskId ran.")
                tasks[taskId]?.remove(this)
            }
        }

        addRunnable(taskId, runnable)
        handler.postDelayed(runnable, time - System.currentTimeMillis())

        Timber.i("Task $taskId scheduled.")
    }

    /**
     * Repeat a [task] for [repeat] times, with [interval].
     * Each repeat will make a runnable.
     * They will NOT be added to [tasks].
     * This action is not cancelable.
     *
     * If you want to repeat a task forever, use [doOnEvery].
     */
    fun doFor(taskId: Long, repeat: Long, interval: Long, task: () -> Any?) {
        val runnable = Runnable {
            task()
            Timber.v("Repeating $taskId.")
        }
        
        for (i: Long in (0 until repeat)) {
            handler.postDelayed(runnable, interval * i)
        }
    }

    /**
     * Stop and remove evey runnable related to taskId.
     */
    fun cancel(taskId: Long) {
        tasks[taskId]?.let {
            it.forEach(handler::removeCallbacks)
            Timber.i("Task $taskId canceled.")

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