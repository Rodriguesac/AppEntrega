package com.rodriguesacai.entregador.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class RideFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: data["tipo"] ?: data["event"] ?: ""
        val looksLikeRide = type.equals("NEW_RIDE", true) ||
            type.equals("NOVA_CORRIDA", true) ||
            type.equals("NOVA_ROTA", true) ||
            data.containsKey("rideId") || data.containsKey("rotaId") || data.containsKey("pedidoId")

        if (looksLikeRide) {
            NotificationHelper.urgentRideNotification(
                context = this,
                rideId = data["rideId"] ?: data["rotaId"] ?: data["pedidoId"] ?: data["id"] ?: "sem-id",
                value = data["value"] ?: data["valor"] ?: data["valorRota"] ?: data["valorEntrega"] ?: "R$ --",
                distance = data["distance"] ?: data["distancia"] ?: data["km"] ?: "-- km",
                pickup = data["pickup"] ?: data["coleta"] ?: data["loja"] ?: data["lojaNome"] ?: "Rodrigues Açaí e Cia",
                dropoff = data["dropoff"] ?: data["entrega"] ?: data["cliente"] ?: data["endereco"] ?: "Endereço do cliente liberado após aceite"
            )
        }
    }

    override fun onNewToken(token: String) {
        DriverSessionStore.saveFcmToken(this, token)
        DriverSessionStore.syncFcmTokenToFirestore(this, token)
    }
}
