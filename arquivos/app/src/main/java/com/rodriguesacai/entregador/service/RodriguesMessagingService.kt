package com.rodriguesacai.entregador.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class RodriguesMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.data["title"]
            ?: message.notification?.title
            ?: "Nova corrida"
        val body = message.data["body"]
            ?: message.notification?.body
            ?: "Você recebeu uma nova oferta de entrega."
        NotificationHelper.showUrgent(this, title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
