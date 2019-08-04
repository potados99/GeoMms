package com.potados.geomms.feature.common

import com.potados.geomms.core.functional.Result
import com.potados.geomms.core.interactor.UseCase.None
import com.potados.geomms.feature.message.domain.SmsComposed

interface MessageService {

    fun sendSms(sms: SmsComposed, save: Boolean = true): Result<None>
}