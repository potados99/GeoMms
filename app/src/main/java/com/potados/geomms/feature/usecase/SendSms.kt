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
import com.potados.geomms.feature.data.repository.MessageRepository
import com.potados.geomms.feature.failure.MessageFailure

class SendSms(
    private val messageRepository: MessageRepository
) : UseCase<UseCase.None, SmsEntity>() {

    override suspend fun run(params: SmsEntity): Either<Failure, None> =
        messageRepository.sendSms(params, true)
}