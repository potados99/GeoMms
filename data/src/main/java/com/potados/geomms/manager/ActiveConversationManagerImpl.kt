package com.potados.geomms.manager

class ActiveConversationManagerImpl: ActiveConversationManager() {

    private var threadId: Long? = null

    override fun setActiveConversation(threadId: Long?) {
        this.threadId = threadId
    }

    override fun getActiveConversation(): Long? {
        return threadId
    }

}