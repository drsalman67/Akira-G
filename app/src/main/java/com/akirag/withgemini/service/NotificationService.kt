package com.akirag.withgemini.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Jab koi naya message aaye
        sendBroadcast(Intent("com.akirag.ACTION_NOTIFICATION_POSTED"))
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Jab message clear ho jaye
        sendBroadcast(Intent("com.akirag.ACTION_NOTIFICATION_CLEARED"))
    }
}
