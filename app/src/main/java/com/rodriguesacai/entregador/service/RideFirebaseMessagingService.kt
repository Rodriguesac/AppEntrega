package com.rodriguesacai.entregador.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rodriguesacai.entregador.data.DriverRepository

class RideFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: data["event"] ?: return

        if (type == "NEW_RIDE" || type == "new_ride") {
            NotificationHelper.urgentRideNotification(
                context = this,
                rideId = data["rideId"] ?: data["id"] ?: "sem-id",
                value = data["value"] ?: "R$ --",
                distance = data["distance"] ?: "-- km",
                duration = data["duration"] ?: "-- min",
                pickup = data["pickup"] ?: data["pickupAddress"] ?: "Rodrigues Açaí e Cia",
                dropoff = data["dropoff"] ?: data["dropoffAddress"] ?: "Endereço liberado após aceite"
            )
        }
    }

    override fun onNewToken(token: String) {
        DriverRepository.saveMessagingToken(this)
    }
}
