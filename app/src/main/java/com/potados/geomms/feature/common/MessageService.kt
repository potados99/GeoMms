package com.potados.geomms.feature.common

import com.potados.geomms.common.functional.Result
import com.potados.geomms.common.interactor.UseCase.None
import com.potados.geomms.feature.message.domain.SmsComposed

interface MessageService {

    fun sendSms(sms: SmsComposed, save: Boolean = true): Result<None>
}