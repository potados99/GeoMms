package com.potados.geomms.usecase

import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.manager.NotificationManager
import com.potados.geomms.repository.MessageRepository

class MarkFailed(
    private val messageRepo: MessageRepository,
    private val notificationMgr: NotificationManager
) : UseCase<MarkFailed.Params>() {

    data class Params(val id: Long, val resultCode: Int)

    override fun run(params: Params): Result<*> =
        Result.of {
            messageRepo.markFailed(params.id, params.resultCode)
            notificationMgr.notifyFailed(params.id)
        }
}