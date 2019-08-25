package com.potados.geomms.base

interface FailableHandler {
    /**
     * What to do when some failure occur
     */
    fun onFail(failure: Failable.Failure)

    /**
     * Add failables to manage.
     */
    fun addFailables(failables: List<Failable>)
}