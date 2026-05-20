package com.rodriguesacai.entregador

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class RodriguesMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val title = message.notification?.title ?: data["title"] ?: data["titulo"] ?: "Nova corrida disponível"
        val body = message.notification?.body ?: data["body"] ?: data["mensagem"] ?: "Abra para analisar a oferta."
        val type = data["type"] ?: data["tipo"] ?: ""
        val rideId = data["rideId"] ?: data["corridaId"] ?: data["pedidoId"]
        if (type.contains("corrida", true) || type.contains("ride", true) || rideId != null) {
            NotificationCenter.showUrgentRide(this, title, body, rideId)
        }
    }
}
