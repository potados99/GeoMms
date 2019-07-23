package com.potados.geomms.feature.usecases

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Telephony
import android.telephony.SmsManager
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

            Either.Right(None())
        } catch (e: Exception) {
            Either.Left(MessageFailure.SendFailure())
        }

    companion object {
        const val SMS_SENT = "com.potados.SMS_SENT"
        const val SMS_DELIVERED = "com.potados.SMS_DELIVERED"
    }
}