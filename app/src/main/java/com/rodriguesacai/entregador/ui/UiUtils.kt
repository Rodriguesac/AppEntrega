package com.rodriguesacai.entregador.ui

import androidx.compose.ui.graphics.Color
import com.rodriguesacai.entregador.ui.theme.AppColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
fun Double.format1(): String = String.format(Locale("pt", "BR"), "%.1f", this)

fun humanStatus(status: String): String = when (status) {
    "OFERTA_RECEBIDA" -> "Oferta recebida"
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
    else -> status.lowercase().replaceFirstChar { it.titlecase() }
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
    "FINALIZADA" -> AppColors.Green
    "RECUSADA", "EXPIRADA" -> AppColors.Red
    "OCORRENCIA" -> AppColors.Yellow
    "OFERTA_RECEBIDA" -> AppColors.Purple
    else -> AppColors.DarkGreen
}

fun shortDate(timestamp: com.google.firebase.Timestamp?): String {
    val date = timestamp?.toDate() ?: return "Agora"
    return SimpleDateFormat("dd/MM • HH:mm", Locale("pt", "BR")).format(date)
}
