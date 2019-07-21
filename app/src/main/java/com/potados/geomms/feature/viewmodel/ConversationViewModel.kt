package com.potados.geomms.feature.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.potados.geomms.feature.data.repository.ContactRepository
import com.potados.geomms.feature.data.repository.MessageRepository
import com.potados.geomms.feature.data.entity.ShortMessage
import com.potados.geomms.feature.data.entity.SmsThread
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * ConversationActivity를 보조할 뷰모델입니다.
 */
class ConversationViewModel : ViewModel(), KoinComponent {

    /**
     * 메시지 가져올 때에 사용.
     */
    private val messageRepo: MessageRepository by inject()

    /**
     * 연락처 가져올 때에 사용.
     */
    private val contactRepo: ContactRepository by inject()



    /***********************************************************************
     * 여기 smsThread는 이 뷰모델을 대표하는 데이터입니다.
     * smsThread가 바뀌면 다른 모든 데이터도 바뀝니다.
     * 하지만 실제로 변화가 자동으로 전달되지는 않습니다.
     * 별도의 observer를 갖추지 않는(비 LiveData)의 경우
     * smsThread 변화에 따라 사용자가 스스로 업데이트하는 코드를 작성해야 합니다.
     ***********************************************************************/

    private val smsThread = MutableLiveData<SmsThread>()

    fun getSmsThread(): LiveData<SmsThread> {
        return smsThread
    }
    fun setSmsThread(thread: SmsThread) {
        smsThread.value = thread

        /**
         * smsThread 종속적인 것들 같이 업데이트
         */
        updateMessages()
    }



    /***********************************************************************
     * 여기 있는 친구들은 smsThread가 바뀔 때를 포함해 자주 업데이트되어야 합니다.
     * 따라서 LiveData로 설계할 필요가 있고, smsThread와 별도로 observer를 두어야 합니다.
     * 물론 이들도 smsThread가 바뀔 때에는 바뀌어야 하기 때문에 setSmsThread에는
     * 이들을 업데이트하는 코드가 들어갑니다.
     ***********************************************************************/

    /**
     * smsThread가 가지는 메시지들입니다.
     */
    private val messages = mutableListOf<ShortMessage>()
    private val liveMessages = MutableLiveData<List<ShortMessage>>()

    fun getMessages(): LiveData<List<ShortMessage>> {
        /**
         * setSmsThread()를 통해 updateMessage()가 호출되기 전까지는 value가 null입니다.
         */
        return liveMessages
    }
    fun addMessage(message: ShortMessage) {

    }
    fun updateMessages() {
        messages.clear()
        smsThread.value?.let {
            messages.addAll(messageRepo.getMessagesFromSmsThread(it))
        }

        liveMessages.value = messages
    }



    /***********************************************************************
     * 여기 있는 친구들은 smsThread가 바뀔 때에만 같이 바뀝니다.
     * 다른 경우에는 바뀔 일이 없습니다.
     * 따라서 LiveData로 설계할 필요가 없고, 사용자는 getSmsThread를 observer한 후에
     * smsThread에 변화가 일어날 때에 이들을 호출해주면 됩니다.
     ***********************************************************************/

    /**
     * 상대방에 대한 정보를 담은 문자열입니다.
     */
    fun getRecipients(): String = smsThread.value?.getRecipientString(contactRepo) ?: ""



    /***********************************************************************
     * 이것들은 뷰의 상태를 잠시 저장해놓기 위함입니다.
     * Observe는 필요하지 않습니다.
     ***********************************************************************/

    /**
     * 리사이클러뷰가 최하단에 도달했는지 여부.
     * 초기값은 참.
     */
    var recyclerViewReachedItsEnd: Boolean = true

}