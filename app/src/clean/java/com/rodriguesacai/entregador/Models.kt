package com.rodriguesacai.entregador

import com.google.firebase.firestore.DocumentSnapshot
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DriverProfile(
    val id: String,
    val collection: String,
    val uid: String = "",
    val name: String = "Entregador",
    val cpf: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val status: String = "",
    val online: Boolean = false,
    val pix: String = "",
    val bank: String = "",
    val payoutType: String = "",
    val needsPasswordSetup: Boolean = false,
    val approved: Boolean = false
)

data class RideItem(
    val id: String,
    val collection: String,
    val orderNumber: String,
    val status: String,
    val value: Double,
    val distance: String,
    val duration: String,
    val pickup: String,
    val dropoff: String,
    val customerName: String,
    val neighborhood: String,
    val payment: String,
    val assignedDriverId: String,
    val createdAt: Long
)

data class BannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val action: String,
    val active: Boolean
)

data class NoticeItem(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val createdAt: Long,
    val read: Boolean = false
)

data class AppUpdateInfo(
    val latestVersion: String = "",
    val minVersion: String = "",
    val message: String = "",
    val url: String = "",
    val mandatory: Boolean = false
)

fun DocumentSnapshot.str(vararg names: String): String {
    for (name in names) {
        val value = get(name) ?: continue
        val text = value.toString().trim()
        if (text.isNotEmpty() && text != "null") return text
    }
    return ""
}

fun DocumentSnapshot.bool(vararg names: String): Boolean {
    for (name in names) {
        val value = get(name) ?: continue
        if (value is Boolean) return value
        val text = value.toString().lowercase(Locale.ROOT)
        if (text in listOf("true", "sim", "1", "online", "ativo", "aprovado")) return true
        if (text in listOf("false", "nao", "não", "0", "offline", "inativo")) return false
    }
    return false
}

fun DocumentSnapshot.double(vararg names: String): Double {
    for (name in names) {
        val value = get(name) ?: continue
        when (value) {
            is Number -> return value.toDouble()
            is String -> value.replace("R$", "").replace(".", "").replace(",", ".").trim().toDoubleOrNull()?.let { return it }
        }
    }
    return 0.0
}

fun DocumentSnapshot.longTime(vararg names: String): Long {
    for (name in names) {
        val value = get(name) ?: continue
        when (value) {
            is Number -> return value.toLong()
            is Date -> return value.time
            is com.google.firebase.Timestamp -> return value.toDate().time
            is String -> value.toLongOrNull()?.let { return it }
        }
    }
    return 0L
}

fun DocumentSnapshot.driverFrom(collection: String): DriverProfile {
    val rawStatus = str("status", "statusCadastro", "situacao", "statusOperacional")
    val lower = rawStatus.lowercase(Locale.ROOT)
    val approved = lower in listOf("aprovado", "ativo", "liberado", "online", "offline", "disponivel", "disponível") || bool("aprovado", "ativo", "liberado")
    val needsPassword = bool("precisaCriarSenha", "needsPasswordSetup") || (!bool("senhaCriada") && str("senha", "senhaApp", "password").isEmpty())
    return DriverProfile(
        id = id,
        collection = collection,
        uid = str("uid", "authUid", "userUid", "firebaseUid"),
        name = str("nome", "name", "nomeCompleto", "displayName").ifBlank { "Entregador" },
        cpf = str("cpf", "documento"),
        phone = str("telefone", "phone", "celular", "whatsapp"),
        email = str("email"),
        photoUrl = str("fotoUrl", "photoUrl", "avatar", "imagem", "urlFoto"),
        status = rawStatus,
        online = bool("online", "disponivel", "disponível"),
        pix = str("pix", "chavePix", "pixKey"),
        bank = str("banco", "bank"),
        payoutType = str("tipoRepasse", "repasse", "payoutType"),
        needsPasswordSetup = needsPassword,
        approved = approved
    )
}

fun DocumentSnapshot.rideFrom(collection: String): RideItem {
    val code = str("numeroPedido", "pedidoNumero", "codigo", "orderNumber", "shortId").ifBlank { id.takeLast(6).uppercase(Locale.ROOT) }
    val pickupAddress = str("enderecoColeta", "coletaEndereco", "lojaEndereco", "origem", "pickupAddress", "loja")
    val deliveryAddress = str("enderecoEntrega", "entregaEndereco", "clienteEndereco", "destino", "dropoffAddress", "endereco")
    return RideItem(
        id = id,
        collection = collection,
        orderNumber = code,
        status = str("status", "statusPedido", "statusPedidoCore", "situacao").ifBlank { "PENDENTE" },
        value = double("valorEntrega", "taxaEntrega", "valor", "value", "frete", "totalMotoboy", "repasseMotoboy"),
        distance = str("distancia", "distance", "km", "distanciaKm"),
        duration = str("tempo", "duration", "tempoEstimado", "eta"),
        pickup = pickupAddress,
        dropoff = deliveryAddress,
        customerName = str("clienteNome", "nomeCliente", "customerName", "cliente"),
        neighborhood = str("bairro", "bairroEntrega", "neighborhood"),
        payment = str("pagamento", "formaPagamento", "payment"),
        assignedDriverId = str("entregadorId", "entregadorUid", "motoboyId", "driverId", "courierId"),
        createdAt = longTime("criadoEm", "createdAt", "data", "dataHora", "timestamp", "updatedAt")
    )
}

fun DocumentSnapshot.bannerFrom(): BannerItem = BannerItem(
    id = id,
    title = str("titulo", "title", "headline").ifBlank { "Aviso do gestor" },
    subtitle = str("descricao", "subtitle", "texto", "body"),
    action = str("acao", "action", "link", "destino"),
    active = !bool("inativo", "disabled") && (bool("ativo", "active") || str("status").lowercase(Locale.ROOT) != "inativo")
)

fun DocumentSnapshot.noticeFrom(): NoticeItem = NoticeItem(
    id = id,
    title = str("titulo", "title", "assunto").ifBlank { "Notificação" },
    body = str("mensagem", "body", "texto", "descricao"),
    type = str("tipo", "type", "categoria"),
    createdAt = longTime("criadoEm", "createdAt", "data", "timestamp"),
    read = bool("lida", "read")
)

fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)

fun humanStatus(status: String): String {
    val s = status.uppercase(Locale.ROOT)
    return when {
        s.contains("FINAL") || s.contains("ENTREGUE") -> "Finalizada"
        s.contains("RECUS") -> "Recusada"
        s.contains("EXPIR") -> "Expirada"
        s.contains("OCORR") -> "Ocorrência"
        s.contains("NO_LOCAL") || s.contains("CHEGOU_CLIENTE") -> "No cliente"
        s.contains("ENTREGA") || s.contains("ROTA") || s.contains("CAMINHO_CLIENTE") -> "Em rota"
        s.contains("RETIR") -> "Pedido retirado"
        s.contains("COLETA") -> "Na coleta"
        s.contains("ACEIT") -> "Aceita"
        else -> "Disponível"
    }
}

fun nextActionLabel(status: String): String {
    val s = status.uppercase(Locale.ROOT)
    return when {
        s.contains("ACEIT") -> "Indo para coleta"
        s.contains("INDO_COLETA") -> "Cheguei na coleta"
        s.contains("CHEGUEI_COLETA") || s.contains("NA_COLETA") -> "Pedido retirado"
        s.contains("RETIR") -> "Indo para entrega"
        s.contains("ENTREGA") || s.contains("ROTA") -> "Cheguei no cliente"
        s.contains("NO_LOCAL") || s.contains("CHEGOU_CLIENTE") -> "Finalizar entrega"
        else -> "Avançar etapa"
    }
}

fun nextStatus(status: String): String {
    val s = status.uppercase(Locale.ROOT)
    return when {
        s.contains("ACEIT") -> "INDO_COLETA"
        s.contains("INDO_COLETA") -> "CHEGUEI_COLETA"
        s.contains("CHEGUEI_COLETA") || s.contains("NA_COLETA") -> "RETIRADO"
        s.contains("RETIR") -> "INDO_ENTREGA"
        s.contains("ENTREGA") || s.contains("ROTA") -> "ENTREGADOR_NO_LOCAL"
        s.contains("NO_LOCAL") || s.contains("CHEGOU_CLIENTE") -> "FINALIZADA"
        else -> "ACEITA"
    }
}

fun shortDate(time: Long): String {
    if (time <= 0L) return "Sem data"
    val now = System.currentTimeMillis()
    val day = 24 * 60 * 60 * 1000L
    val hour = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date(time))
    return when {
        now - time < day && Date(now).date == Date(time).date -> "Hoje • $hour"
        now - time < 2 * day -> "Ontem • $hour"
        else -> SimpleDateFormat("dd/MM • HH:mm", Locale("pt", "BR")).format(Date(time))
    }
}
