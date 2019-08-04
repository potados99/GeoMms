package com.potados.geomms.feature.common

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.app.SmsDeliveredReceiver
import com.potados.geomms.app.SmsSentReceiver
import com.potados.geomms.feature.message.domain.SmsComposed

class MessageServiceImpl(
    private val context: Context,
    private val smsManager: SmsManager
) : MessageService {

    override fun sendSms(sms: SmsComposed, save: Boolean): Result<None> =
        try {
            val sentPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(SmsSentReceiver.SMS_SENT), 0)
            val deliveredPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(SmsDeliveredReceiver.SMS_DELIVERED), 0)

            smsManager.sendTextMessage(
                sms.address,
                null,
                sms.body,
                sentPendingIntent,
                deliveredPendingIntent
            )

            if (save) {
                /**
                 * save 옵션이 참이면 DB에 저장합니다.
                 */
                context.contentResolver.insert(
                    Telephony.Sms.Sent.CONTENT_URI,

                    ContentValues().apply {
                        put(Telephony.Sms.ADDRESS, sms.address)
                        put(Telephony.Sms.BODY, sms.body)
                    }
                )
            }

            Log.i("MessageServiceImpl:sendSms", "try to send message: {address: ${sms.address}, body: ${sms.body}}")

            Result.Success(None())
        } catch (e: Exception) {
            Log.i("MessageServiceImpl:sendSms", e.message)

            Result.Error(e)
        }
}