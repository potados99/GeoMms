package com.potados.geomms.usecase

import com.potados.geomms.extension.elapsedTimeMillis
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.repository.SyncRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SyncMessages(
    private val syncRepo: SyncRepository,
    private val clearAll: ClearAll,
    private val processMessages: ProcessMessages,
    private val updateBadge: UpdateBadge
) : UseCase<Long>() {

    override fun run(params: Long): Result<*> =
        Result.of {
            Timber.i("Will sync messages from date $params.")

            // Connections relay on Recipient, which will be deleted after sync.
            // Disconnect all connections before sync to prevent connection having
            // no recipient.
            clearAll(Unit) {
                it.onError { Timber.w("Failed to clear all.") }
            }

            Timber.i("Clear all.")

            val elapsedMillis = elapsedTimeMillis {
                syncRepo.syncMessages(params)
            }

            val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis)

            Timber.i("Completed sync in $elapsedSeconds seconds.")

            processMessages(Unit) {
                it.onError { Timber.w("Failed to process messages.") }
            }

            updateBadge(Unit)
        }
}