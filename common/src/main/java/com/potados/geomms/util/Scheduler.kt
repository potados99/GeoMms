/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
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

        with(runnable) {
            addRunnable(taskId, this)
            handler.postDelayed(this, 1000) // Start first job after 1 sec.
        }

        Timber.i("Task $taskId scheduled.")
    }

    /**
     * Add task to be done at time.
     */
    fun doAtTime(taskId: Long, time: Long, task: () -> Any?) {
        val runnable = Runnable {
            task()
            Timber.v("On time task $taskId ran.")
        }

        with(runnable) {
            addRunnable(taskId, this)
            handler.postDelayed(this, time - System.currentTimeMillis())
        }

        Timber.i("Task $taskId scheduled.")
    }

    /**
     * Stop periodic task and remove it from handler.
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