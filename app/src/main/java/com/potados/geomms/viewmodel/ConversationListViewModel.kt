package com.potados.geomms.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.data.MessageRepository
import com.potados.geomms.data.ShortMessage
import com.potados.geomms.data.SmsThread
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * ConversationListFragment를 보조할 뷰모델입니다.
 * 대화방 정보를 가지고 있습니다.
 *
 * 이를 직접 설정하지는 않고, updateConversations() 메소드를 통해 re-query를 유발합니다.
 */
class ConversationListViewModel : ViewModel(), KoinComponent {
    /**
     * 메시지를 가져오려면 필요합니다.
     */
    private val messageRepo: MessageRepository by inject()

    /**
     * 위의 repo에서 가져온 실제 데이터입니다. UI와 연결하기 위 잠시 여기에 보관하고 LiveData로 외부에 공개합니다.
     */
    private val conversations: MutableList<SmsThread> = mutableListOf()

    /**
     * 위의 대화 리스트를 LiveData로 포장합니다.
     */
    private val liveDataOfConversations = MutableLiveData<List<SmsThread>>()

    init {
        updateConversations()
    }

    /**
     * 현재 있는 대화 목록을 넘겨줍니다. Observe 가능.
     */
    fun getConversations(): LiveData<List<SmsThread>> = liveDataOfConversations

    /**
     * 있던 것을 버리고 다시 가져옵니다.
     * 조금 느릴 것 같음.
     * TODO: 더 나은 방법 찾기
     */
    fun updateConversations() {
        conversations.clear()
        conversations.addAll(messageRepo.getSmsThreads())

        liveDataOfConversations.value = conversations
    }
}