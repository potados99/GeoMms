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