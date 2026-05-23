package com.rodriguesacai.entregador.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rodriguesacai.entregador.data.DriverRepository

class RideFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = (data["type"] ?: data["event"] ?: data["acao"] ?: data["action"] ?: "").lowercase()
        val rideId = data["rideId"] ?: data["rotaId"] ?: data["pedidoId"] ?: data["id"]

        val looksLikeRide = rideId != null || type in setOf("new_ride", "newride", "nova_corrida", "nova_rota", "pedido_novo", "new_order")
        if (!looksLikeRide) {
            NotificationHelper.appNoticeNotification(
                context = this,
                noticeId = data["noticeId"] ?: data["id"] ?: System.currentTimeMillis().toString(),
                title = data["title"] ?: data["titulo"] ?: message.notification?.title ?: "Aviso da operação",
                message = data["message"] ?: data["mensagem"] ?: message.notification?.body ?: "Abra o app para ver detalhes.",
                category = data["category"] ?: data["categoria"] ?: "Sistema"
            )
            return
        }

        NotificationHelper.urgentRideNotification(
            context = this,
            rideId = rideId ?: "sem-id",
            value = data["value"] ?: data["valor"] ?: data["valorRota"] ?: "",
            distance = data["distance"] ?: data["distancia"] ?: data["distanciaKm"] ?: "",
            duration = data["duration"] ?: data["tempo"] ?: data["tempoMin"] ?: "",
            pickup = data["pickup"] ?: data["pickupAddress"] ?: data["lojaEndereco"] ?: data["nomeLoja"] ?: "",
            dropoff = data["dropoff"] ?: data["dropoffAddress"] ?: data["enderecoEntrega"] ?: data["enderecoCompleto"] ?: "",
            paymentMethod = data["formaPagamento"] ?: data["paymentMethod"] ?: data["metodoPagamento"] ?: "",
            paymentStatus = data["statusPagamento"] ?: data["paymentStatus"] ?: "",
            amountToCollect = data["valorReceberCliente"] ?: data["amountToCollect"] ?: data["totalPedido"] ?: data["valorPedido"] ?: "",
            changeFor = data["trocoPara"] ?: data["changeFor"] ?: "",
            requiresMachine = data["precisaMaquininha"] ?: data["requiresMachine"] ?: ""
        )
    }

    override fun onNewToken(token: String) {
        DriverRepository.saveMessagingToken(this)
    }
}
