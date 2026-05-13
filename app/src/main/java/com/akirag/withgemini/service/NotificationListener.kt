package com.akirag.withgemini.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Jab naya notification aaye
        val intent = Intent("AKIRA_NOTIF_POSTED")
        sendBroadcast(intent)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Jab notification clear ho jaye
        val intent = Intent("AKIRA_NOTIF_CLEARED")
        sendBroadcast(intent)
    }
}
