package com.rodriguesacai.entregador.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.theme.AppColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
fun moneyOrEmpty(value: Double?, hidden: Boolean = false): String = when {
    hidden -> "••••"
    value == null -> "Sem dado"
    else -> money(value)
}
fun safeMoney(value: Double?): String = value?.let { money(it) } ?: "A calcular"
fun Double.format1(): String = String.format(Locale("pt", "BR"), "%.1f", this)
fun safeDistance(value: Double?): String = value?.takeIf { it > 0.0 }?.let { "${it.format1()} km" } ?: "Rota pendente"
fun safeEta(value: Int?): String = value?.takeIf { it > 0 }?.let { "${it} min" } ?: "Tempo pendente"

fun Ride.deliveryAddressVisible(): Boolean = status in listOf(
    "PEDIDO_RETIRADO",
    "INDO_ENTREGA",
    "ENTREGADOR_NO_LOCAL",
    "OCORRENCIA",
    "FINALIZADA"
)

fun Ride.hasPickupLocation(): Boolean = lojaLat != null && lojaLng != null
fun Ride.hasDeliveryLocation(): Boolean = clienteLat != null && clienteLng != null
fun Ride.pickupVisibleAddress(): String = lojaEndereco.ifBlank { "Endereço da coleta pendente" }
fun Ride.deliveryVisibleTitle(): String = if (deliveryAddressVisible()) clienteNome.ifBlank { "Cliente não informado" } else clienteBairro.ifBlank { "Bairro pendente" }
fun Ride.safeDeliveryAddress(): String = if (deliveryAddressVisible()) {
    clienteEnderecoCompleto.ifBlank { "Endereço do cliente pendente" }
} else {
    "Endereço completo liberado após retirar o pedido"
}

fun humanStatus(status: String): String = when (status) {
    "OFERTA_RECEBIDA" -> "Oferta recebida"
    "DISPONIVEL" -> "Disponível"
    "ACEITA" -> "Aceita"
    "INDO_COLETA" -> "Indo para coleta"
    "CHEGUEI_COLETA" -> "Na coleta"
    "PEDIDO_RETIRADO" -> "Pedido retirado"
    "INDO_ENTREGA" -> "Em rota"
    "ENTREGADOR_NO_LOCAL" -> "No local"
    "FINALIZADA" -> "Finalizada"
    "RECUSADA" -> "Recusada"
    "EXPIRADA" -> "Expirada"
    "OCORRENCIA" -> "Ocorrência"
    "RESTRICAO" -> "Restrição"
    "INDISPONIVEL" -> "Indisponível"
    else -> status.lowercase().replace("_", " ").replaceFirstChar { it.titlecase() }
}

fun nextActionText(status: String): String = when (status) {
    "ACEITA" -> "Iniciar ida à coleta"
    "INDO_COLETA" -> "Cheguei na coleta"
    "CHEGUEI_COLETA" -> "Pedido retirado"
    "PEDIDO_RETIRADO" -> "Ir para entrega"
    "INDO_ENTREGA" -> "Cheguei no cliente"
    "ENTREGADOR_NO_LOCAL" -> "Finalizar entrega"
    else -> "Atualizar corrida"
}

fun statusColor(status: String): Color = when (status) {
    "DISPONIVEL", "FINALIZADA" -> AppColors.Green
    "RECUSADA", "EXPIRADA", "RESTRICAO" -> AppColors.Red
    "OCORRENCIA" -> AppColors.Yellow
    "OFERTA_RECEBIDA" -> AppColors.Red
    "INDISPONIVEL" -> AppColors.Ink
    else -> AppColors.DarkGreen
}

fun shortDate(timestamp: com.google.firebase.Timestamp?): String {
    val date = timestamp?.toDate() ?: return "Sem horário"
    return SimpleDateFormat("dd/MM • HH:mm", Locale("pt", "BR")).format(date)
}

fun openNavigation(context: Context, ride: Ride): Boolean {
    val goingToDelivery = ride.deliveryAddressVisible()
    val lat = if (goingToDelivery) ride.clienteLat else ride.lojaLat
    val lng = if (goingToDelivery) ride.clienteLng else ride.lojaLng
    if (lat == null || lng == null) return false
    val google = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$lat,$lng&mode=d")).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(google) }.onFailure { context.startActivity(fallback) }
    return true
}
