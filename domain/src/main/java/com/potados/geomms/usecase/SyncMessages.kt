package com.potados.geomms.usecase

import com.potados.geomms.extension.elapsedTimeMillis
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.repository.SyncRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SyncMessages(
    private val syncRepo: SyncRepository,
    private val updateBadge: UpdateBadge
) : UseCase<Unit>() {

    override fun run(params: Unit): Result<*> =
        Result.of {
            val elapsedMillis = elapsedTimeMillis(syncRepo::syncMessages)
            val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis)

            Timber.i("Completed sync in $elapsedSeconds seconds.")

            updateBadge(Unit)
        }
}