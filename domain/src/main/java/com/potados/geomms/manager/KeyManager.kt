package com.potados.geomms.manager

/**
 * Helper class for generating incrementing ids for messages
 */
abstract class KeyManager : Manager() {

    /**
     * Should be called when a new sync is being started
     */
    abstract fun reset(channel: Int)

    /**
     * Returns a valid ID
     */
    abstract fun newId(channel: Int): Long

    /**
     * Returns random ID
     */
    abstract fun randomId(max: Long): Long
}