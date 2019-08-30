package com.potados.geomms.usecase

import com.potados.geomms.extension.elapsedTimeMillis
import com.potados.geomms.functional.Result
import com.potados.geomms.interactor.UseCase
import com.potados.geomms.repository.SyncRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SyncContacts(
    private val syncRepo: SyncRepository
) : UseCase<Unit>() {

    override fun run(params: Unit): Result<*> =
        Result.of {
            val elapsedMillis = elapsedTimeMillis(syncRepo::syncContacts)
            val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis)

            Timber.i("Completed sync in $elapsedSeconds seconds.")
        }
}