/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.potados.geomms.manager

import com.potados.geomms.base.FailableComponent

/**
 * Keeps track of the conversation that the user is currently viewing. This is useful when we
 * receive a message, because it allows us to immediately mark the message read and not display
 * a notification
 */
abstract class ActiveConversationManager : FailableComponent() {
    abstract fun setActiveConversation(threadId: Long?)

    abstract fun getActiveConversation(): Long?
}