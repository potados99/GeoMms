package com.potados.geomms.feature.common

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.potados.geomms.feature.location.LocationSupportProtocol
import com.potados.geomms.core.util.Notify

/**
 * SMS를 수신했을 때에 작동합니다.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        if (intent == null) return

        Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach {
            processMessage(context, it)
        }
    }

    /**
     * 도착한 메시지를 처리합니다.
     */
    private fun processMessage(context: Context, message: SmsMessage) {

        /**
         * 메시지가 LocationSupport 메시지인지 판단하여 처리합니다.
         */
        if (LocationSupportProtocol.isLocationSupportMessage(message.messageBody)) {
            onLocationSupportMessageArrived(context, message)
        }
        else {
            onPlaneSmsArrived(context, message)
        }

        /**
         * 그리고 메시지가 도착하였다고 알립니다.
         */
        context.sendBroadcast(Intent().apply {
            action = SMS_DELIVER_ACTION
            putExtra(Telephony.Sms.ADDRESS, message.originatingAddress)
            putExtra(Telephony.Sms.BODY, message.messageBody)
            putExtra(Telephony.Sms.DATE, message.timestampMillis)
        })
    }

    /**
     * LocationSupport 메시지가 도착했을 때에 실행됩니다.
     * 사용자에게 알림을 띄웁니다.
     */
    private fun onLocationSupportMessageArrived(context: Context, message: SmsMessage) {
        // TODO: 푸시알림 설치하기
        // Notify(context).short("Open Geo MMS!")
    }

    /**
     * 그냥 SMS가 도착했을 때에 실행됩니다.
     * 데이터베이스에 추가해줍니다.
     */
    private fun onPlaneSmsArrived(context: Context, message: SmsMessage) {

        /**
         * Telephony.SmsEntity.ADDRESS와 Telephony.SmsEntity.BODY만 채워서
         * content://sms/inbox에 넣어줍니다.
         */
        context.contentResolver.insert(
            Telephony.Sms.Inbox.CONTENT_URI,

            ContentValues().apply {
                put(Telephony.Sms.ADDRESS, message.originatingAddress)
                put(Telephony.Sms.BODY, message.messageBody)
                /* 나머지 column은 자동으로 채워짐! */
            }
        )
    }

    companion object {
        const val SMS_DELIVER_ACTION = "com.potados.geomms.SMS_DELIVER"
    }
}