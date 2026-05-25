package com.rodriguesacai.entregador.core

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val br = Locale("pt", "BR")
private val money = NumberFormat.getCurrencyInstance(br)

enum class AppTab { Home, Mapa, Corrida, Ganhos, Perfil, Notificacoes }

data class DriverProfile(
    val id: String = "",
    val nome: String = "Entregador",
    val fotoUrl: String = "",
    val telefone: String = "",
    val cpf: String = "",
    val online: Boolean = false,
    val restricao: String = "",
    val bateriaRestrita: Boolean = false,
    val pix: String = "",
    val banco: String = "",
    val ocultarValores: Boolean = false
)

data class Ride(
    val id: String = "",
    val pedidoId: String = "",
    val ofertaId: String = "",
    val status: String = "",
    val etapa: String = "",
    val lojaNome: String = "Rodrigues Açaí e Cia.",
    val lojaEndereco: String = "Endereço da loja não informado",
    val clienteNome: String = "Cliente",
    val clienteBairro: String = "Bairro não informado",
    val clienteEndereco: String = "Endereço liberado após a etapa correta",
    val valorCorrida: Double = 0.0,
    val valorPedido: Double = 0.0,
    val valorReceberCliente: Double = 0.0,
    val distanciaKm: Double = 0.0,
    val tempoMin: Int = 0,
    val pagamentoStatus: String = "Pagamento não informado",
    val pagamentoForma: String = "Não informado",
    val precisaTroco: Boolean = false,
    val trocoPara: Double = 0.0,
    val precisaMaquininha: Boolean = false,
    val observacoes: String = "Sem observações.",
    val codigoEntrega: String = "",
    val prioridade: String = "Normal",
    val createdAt: Timestamp? = null
) {
    val isActive: Boolean
        get() = status.uppercase() !in setOf("", "FINALIZADA", "ENTREGUE", "CANCELADA", "RECUSADA", "EXPIRADA")

    val currentStage: Int
        get() = when (etapa.uppercase().ifBlank { status.uppercase() }) {
            "ACEITA", "ACEITO", "INDO_COLETA", "A_CAMINHO_COLETA" -> 0
            "NA_COLETA", "CHEGOU_COLETA", "CHEGUEI_COLETA" -> 1
            "PEDIDO_RETIRADO", "COLETADO", "RETIRADO" -> 2
            "ENTREGADOR_NO_LOCAL", "CHEGOU_CLIENTE", "CHEGUEI_CLIENTE" -> 3
            "FINALIZAR", "AGUARDANDO_FINALIZACAO" -> 4
            else -> 0
        }

    val statusHumano: String
        get() = when (etapa.uppercase().ifBlank { status.uppercase() }) {
            "ACEITA", "ACEITO", "INDO_COLETA", "A_CAMINHO_COLETA" -> "Em rota para coleta"
            "NA_COLETA", "CHEGOU_COLETA", "CHEGUEI_COLETA" -> "Na coleta"
            "PEDIDO_RETIRADO", "COLETADO", "RETIRADO" -> "Indo para entrega"
            "ENTREGADOR_NO_LOCAL", "CHEGOU_CLIENTE", "CHEGUEI_CLIENTE" -> "Chegou no cliente"
            "OCORRENCIA" -> "Ocorrência aberta"
            else -> if (status.isBlank()) "Sem corrida" else status.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase(br) }
        }

    val nextAction: String
        get() = when (currentStage) {
            0 -> "Cheguei na coleta"
            1 -> "Pedido retirado"
            2 -> "Cheguei no cliente"
            3 -> "Finalizar entrega"
            else -> "Conferir corrida"
        }

    val nextStatus: Pair<String, String>
        get() = when (currentStage) {
            0 -> "NA_COLETA" to "NA_COLETA"
            1 -> "PEDIDO_RETIRADO" to "PEDIDO_RETIRADO"
            2 -> "ENTREGADOR_NO_LOCAL" to "ENTREGADOR_NO_LOCAL"
            3 -> "FINALIZAR" to "FINALIZAR"
            else -> "ATUALIZADA" to "ATUALIZADA"
        }
}

data class Offer(
    val id: String = "",
    val pedidoId: String = "",
    val entregadorUid: String = "",
    val status: String = "",
    val lojaNome: String = "Rodrigues Açaí e Cia.",
    val lojaEndereco: String = "Endereço da loja não informado",
    val clienteBairro: String = "Bairro não informado",
    val valorCorrida: Double = 0.0,
    val distanciaKm: Double = 0.0,
    val tempoMin: Int = 0,
    val pagamentoForma: String = "Não informado",
    val valorReceberCliente: Double = 0.0,
    val prioridade: String = "Normal",
    val expiraEm: Timestamp? = null,
    val data: Map<String, Any?> = emptyMap()
) {
    val ativa: Boolean
        get() = status.uppercase() in setOf("OFERTA_ENTREGADOR", "ATIVA", "PENDENTE", "TOCANDO")
}

data class DriverNotification(
    val id: String = "",
    val titulo: String = "Aviso",
    val corpo: String = "",
    val lida: Boolean = false,
    val tipo: String = "NORMAL",
    val createdAt: Timestamp? = null
)

data class UiState(
    val firebaseReady: Boolean = false,
    val firebaseMessage: String = "Conectando ao Firebase...",
    val driverId: String = "",
    val profile: DriverProfile = DriverProfile(),
    val activeRide: Ride? = null,
    val activeOffer: Offer? = null,
    val notifications: List<DriverNotification> = emptyList(),
    val loading: Boolean = true,
    val lastError: String = "",
    val unreadCount: Int = 0,
    val todayRides: Int = 0,
    val todayFinished: Int = 0,
    val todayEarnings: Double = 0.0,
    val locationText: String = "Localização aguardando permissão"
)

fun Double.moneyBr(): String = money.format(this)
fun Double.kmBr(): String = if (this <= 0.0) "--" else String.format(br, "%.1f km", this)
fun Int.minBr(): String = if (this <= 0) "--" else "$this min"

fun Timestamp?.shortDate(): String {
    if (this == null) return "Agora"
    return SimpleDateFormat("dd/MM • HH:mm", br).format(this.toDate())
}

fun DocumentSnapshot.asRide(): Ride {
    val data = data ?: emptyMap()
    return Ride(
        id = id,
        pedidoId = data.str("pedidoId", "pedido_id", "orderId", default = id),
        ofertaId = data.str("ofertaId", "oferta_id", default = ""),
        status = data.str("status", "statusCorrida", default = ""),
        etapa = data.str("etapa", "etapaAtual", "statusOperacional", default = ""),
        lojaNome = data.str("lojaNome", "nomeLoja", "restauranteNome", default = "Rodrigues Açaí e Cia."),
        lojaEndereco = data.str("lojaEndereco", "enderecoLoja", "coletaEndereco", default = "Endereço da loja não informado"),
        clienteNome = data.str("clienteNome", "nomeCliente", default = "Cliente"),
        clienteBairro = data.str("clienteBairro", "bairro", "bairroEntrega", default = "Bairro não informado"),
        clienteEndereco = data.str("clienteEndereco", "enderecoCliente", "enderecoEntrega", default = "Endereço não informado"),
        valorCorrida = data.num("valorCorrida", "taxaEntrega", "valorEntrega"),
        valorPedido = data.num("valorPedido", "valor_total", "totalPedido"),
        valorReceberCliente = data.num("valorReceberCliente", "valorAReceber", "receberCliente"),
        distanciaKm = data.num("distanciaKm", "distancia", "km"),
        tempoMin = data.int("tempoMin", "tempoEstimado", "minutos"),
        pagamentoStatus = data.str("pagamentoStatus", "pagamento.status", "statusPagamento", default = "Pagamento não informado"),
        pagamentoForma = data.str("pagamentoForma", "formaPagamento", "pagamento.forma", default = "Não informado"),
        precisaTroco = data.bool("precisaTroco", "troco"),
        trocoPara = data.num("trocoPara", "valorTrocoPara"),
        precisaMaquininha = data.bool("precisaMaquininha", "maquininha"),
        observacoes = data.str("observacoes", "obs", "observacao", default = "Sem observações."),
        codigoEntrega = data.str("codigoEntrega", "codigo_confirmacao", "codigo", default = ""),
        prioridade = data.str("prioridade", default = "Normal"),
        createdAt = getTimestamp("createdAt") ?: getTimestamp("criadoEm") ?: getTimestamp("dataCriacao")
    )
}

fun DocumentSnapshot.asOffer(): Offer {
    val data = data ?: emptyMap()
    return Offer(
        id = id,
        pedidoId = data.str("pedidoId", "pedido_id", "orderId", default = ""),
        entregadorUid = data.str("entregadorUid", "driverId", "motoboyUid", default = ""),
        status = data.str("status", default = ""),
        lojaNome = data.str("lojaNome", "nomeLoja", default = "Rodrigues Açaí e Cia."),
        lojaEndereco = data.str("lojaEndereco", "coletaEndereco", default = "Endereço da loja não informado"),
        clienteBairro = data.str("clienteBairro", "bairro", "bairroEntrega", default = "Bairro não informado"),
        valorCorrida = data.num("valorCorrida", "taxaEntrega", "valorEntrega"),
        distanciaKm = data.num("distanciaKm", "distancia", "km"),
        tempoMin = data.int("tempoMin", "tempoEstimado", "minutos"),
        pagamentoForma = data.str("pagamentoForma", "formaPagamento", "pagamento.forma", default = "Não informado"),
        valorReceberCliente = data.num("valorReceberCliente", "valorAReceber", "receberCliente"),
        prioridade = data.str("prioridade", default = "Normal"),
        expiraEm = getTimestamp("expiraEm") ?: getTimestamp("expiresAt"),
        data = data
    )
}

fun DocumentSnapshot.asDriverProfile(driverId: String): DriverProfile {
    val data = data ?: emptyMap()
    return DriverProfile(
        id = driverId,
        nome = data.str("nome", "name", default = "Entregador"),
        fotoUrl = data.str("fotoUrl", "photoUrl", "foto", default = ""),
        telefone = data.str("telefone", "phone", default = ""),
        cpf = data.str("cpf", default = ""),
        online = data.bool("online", "disponivel"),
        restricao = data.str("restricao", "motivoRestricao", "statusRestricao", default = ""),
        bateriaRestrita = data.bool("bateriaRestrita", "restricaoBateria"),
        pix = data.str("pix", "chavePix", default = ""),
        banco = data.str("banco", "bank", default = ""),
        ocultarValores = data.bool("ocultarValores", "hideValues")
    )
}

fun DocumentSnapshot.asNotification(): DriverNotification {
    val data = data ?: emptyMap()
    return DriverNotification(
        id = id,
        titulo = data.str("titulo", "title", default = "Aviso"),
        corpo = data.str("corpo", "body", "mensagem", default = ""),
        lida = data.bool("lida", "read"),
        tipo = data.str("tipo", "type", default = "NORMAL"),
        createdAt = getTimestamp("createdAt") ?: getTimestamp("criadoEm")
    )
}

private fun Map<String, Any?>.str(vararg keys: String, default: String = ""): String {
    for (key in keys) {
        val direct = this[key]
        if (direct is String && direct.isNotBlank()) return direct
        if (key.contains('.')) {
            val nested = nested(key)
            if (nested is String && nested.isNotBlank()) return nested
        }
    }
    return default
}

private fun Map<String, Any?>.num(vararg keys: String): Double {
    for (key in keys) {
        val value = if (key.contains('.')) nested(key) else this[key]
        when (value) {
            is Number -> return value.toDouble()
            is String -> value.replace("R$", "").replace(".", "").replace(',', '.').trim().toDoubleOrNull()?.let { return it }
        }
    }
    return 0.0
}

private fun Map<String, Any?>.int(vararg keys: String): Int = num(*keys).toInt()

private fun Map<String, Any?>.bool(vararg keys: String): Boolean {
    for (key in keys) {
        val value = if (key.contains('.')) nested(key) else this[key]
        when (value) {
            is Boolean -> return value
            is Number -> return value.toInt() != 0
            is String -> return value.equals("true", true) || value.equals("sim", true) || value == "1"
        }
    }
    return false
}

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>.nested(path: String): Any? {
    var current: Any? = this
    path.split('.').forEach { part ->
        current = (current as? Map<String, Any?>)?.get(part)
    }
    return current
}
