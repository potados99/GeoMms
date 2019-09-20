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

package com.potados.geomms.receiver

import com.android.mms.transaction.PushReceiver

/**
 * Manifest registered.
 * Receive both explicit intent and
 * implicit intent with action of [android.provider.Telephony.WAP_PUSH_DELIVER].
 *
 * Handle MMS and call [MmsReceivedReceiver].
 *
 * It moves first when the action(see above) arrives.
 * The PushReceiver receives the intent, download, save MMS,
 * and calls [MmsReceivedReceiver] by action [com.klinker.android.messaging.MMS_RECEIVED].
 *
 * @see [MmsReceivedReceiver]
 */
class MmsReceiver : PushReceiver()