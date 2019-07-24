package com.potados.geomms.feature.usecase

import android.app.PendingIntent
import android.content.*
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import com.potados.geomms.core.exception.Failure
import com.potados.geomms.core.functional.Either
import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.feature.data.entity.SmsEntity
import com.potados.geomms.feature.failure.MessageFailure

class SendSms(
    private val context: Context,
    private val smsManager: SmsManager
) : UseCase<UseCase.None, SmsEntity>() {

    override suspend fun run(params: SmsEntity): Either<Failure, None> =
        try {
            val sentPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(SMS_SENT), 0)
            val deliveredPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(SMS_DELIVERED), 0)

            smsManager.sendTextMessage(
                params.address,
                null,
                params.body,
                sentPendingIntent,
                deliveredPendingIntent
            )

            context.contentResolver.insert(
                Telephony.Sms.Sent.CONTENT_URI,

                ContentValues().apply {
                    put(Telephony.Sms.ADDRESS, params.address)
                    put(Telephony.Sms.BODY, params.body)
                    /* 나머지 column은 자동으로 채워짐! */
                }
            )

            Log.i("SendSms:run", "try to send message.")

            Either.Right(None())
        } catch (e: Exception) {
            e.printStackTrace()
            Either.Left(MessageFailure.SendFailure())
        }

    companion object {
        const val SMS_SENT = "com.potados.SMS_SENT"
        const val SMS_DELIVERED = "com.potados.SMS_DELIVERED"
    }
}