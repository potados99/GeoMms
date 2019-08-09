package com.potados.geomms.feature.message.domain.usecase

import com.potados.geomms.core.interactor.UseCase
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.feature.common.MessageService
import com.potados.geomms.feature.message.domain.SmsComposed

class SendSms(
    private val messageService: MessageService
) : UseCase<None, SmsComposed>() {

    override suspend fun buildObservable(params: SmsComposed): Flowable<*> =
        messageService.sendSms(params, true)
}