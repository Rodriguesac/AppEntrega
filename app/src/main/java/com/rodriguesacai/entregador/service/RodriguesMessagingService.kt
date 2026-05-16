package com.rodriguesacai.entregador.service
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
class RodriguesMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        NotificationHelper.createChannels(this)
        val title = message.notification?.title ?: message.data["title"] ?: "Nova corrida"
        val body = message.notification?.body ?: message.data["body"] ?: "Toque para aceitar."
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2001, NotificationHelper.urgentRideNotification(this, title, body))
    }
    override fun onNewToken(token: String) { super.onNewToken(token) }
}
