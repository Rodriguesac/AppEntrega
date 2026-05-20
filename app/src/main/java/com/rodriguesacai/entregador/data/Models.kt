package com.rodriguesacai.entregador.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.math.roundToInt

data class Driver(
    val id: String = "",
    val nome: String = "Entregador",
    val telefone: String = "",
    val cpf: String = "",
    val fotoUrl: String = "",
    val statusOperacional: String = "INDISPONIVEL",
    val verificado: Boolean = false,
    val online: Boolean = false,
    val pixChave: String = "",
    val pixTipo: String = "",
    val banco: String = "",
    val saldoHoje: Double = 0.0,
    val saldoSemana: Double = 0.0,
    val saldoMes: Double = 0.0,
    val corridasHoje: Int = 0,
    val ocultarValores: Boolean = false
)

data class Ride(
    val id: String = "",
    val pedidoId: String = "",
    val numeroPedido: String = "",
    val entregadorUid: String = "",
    val status: String = "OFERTA_RECEBIDA",
    val lojaNome: String = "Rodrigues Açaí e Cia.",
    val lojaEndereco: String = "Endereço da loja",
    val lojaLat: Double = -20.4697,
    val lojaLng: Double = -54.6201,
    val clienteNome: String = "Cliente",
    val clienteBairro: String = "Bairro",
    val clienteEnderecoCompleto: String = "Endereço liberado após coleta",
    val clienteLat: Double = -20.4697,
    val clienteLng: Double = -54.6201,
    val valorCorrida: Double = 0.0,
    val distanciaKm: Double = 0.0,
    val tempoEstimadoMin: Int = 0,
    val criadaEm: Timestamp? = null,
    val atualizadaEm: Timestamp? = null
)

data class DriverNotification(
    val id: String = "",
    val titulo: String = "",
    val mensagem: String = "",
    val tipo: String = "INFO",
    val lida: Boolean = false,
    val criadaEm: Timestamp? = null
)

data class AppConfig(
    val emManutencao: Boolean = false,
    val mensagemManutencao: String = "Sistema em manutenção.",
    val versaoMinima: Int = 1,
    val mensagemAtualizacao: String = "Existe uma atualização disponível."
)

fun DocumentSnapshot.toDriver(): Driver {
    fun str(vararg keys: String): String = keys.firstNotNullOfOrNull { getString(it) }.orEmpty()
    return Driver(
        id = id,
        nome = str("nome", "name" ).ifBlank { "Entregador" },
        telefone = str("telefone", "phone"),
        cpf = str("cpf"),
        fotoUrl = str("fotoUrl", "urlPerfil", "photoUrl"),
        statusOperacional = str("statusOperacional", "status").ifBlank { "INDISPONIVEL" },
        verificado = getBoolean("verificado") ?: getBoolean("aprovado") ?: false,
        online = getBoolean("online") ?: false,
        pixChave = str("pixChave", "chavePix"),
        pixTipo = str("pixTipo", "tipoPix"),
        banco = str("banco"),
        saldoHoje = anyDouble("saldoHoje", "ganhosHoje", "hoje"),
        saldoSemana = anyDouble("saldoSemana", "ganhosSemana", "semana"),
        saldoMes = anyDouble("saldoMes", "ganhosMes", "mes"),
        corridasHoje = anyDouble("corridasHoje", "entregasHoje").roundToInt(),
        ocultarValores = getBoolean("ocultarValores") ?: false
    )
}

fun DocumentSnapshot.toRide(): Ride {
    fun str(vararg keys: String): String = keys.firstNotNullOfOrNull { getString(it) }.orEmpty()
    return Ride(
        id = id,
        pedidoId = str("pedidoId", "orderId"),
        numeroPedido = str("numeroPedido", "numeroCurto", "pedidoNumero").ifBlank { id.takeLast(6).uppercase() },
        entregadorUid = str("entregadorUid", "driverId"),
        status = str("status", "statusCorrida").ifBlank { "OFERTA_RECEBIDA" },
        lojaNome = str("lojaNome", "storeName").ifBlank { "Rodrigues Açaí e Cia." },
        lojaEndereco = str("lojaEndereco", "enderecoLoja").ifBlank { "Rodrigues Açaí e Cia." },
        lojaLat = anyDouble("lojaLat", "storeLat", "origemLat"),
        lojaLng = anyDouble("lojaLng", "storeLng", "origemLng"),
        clienteNome = str("clienteNome", "customerName").ifBlank { "Cliente" },
        clienteBairro = str("clienteBairro", "bairro").ifBlank { "Bairro" },
        clienteEnderecoCompleto = str("clienteEnderecoCompleto", "enderecoCompleto", "deliveryAddress").ifBlank { "Endereço liberado na etapa correta" },
        clienteLat = anyDouble("clienteLat", "deliveryLat", "destinoLat"),
        clienteLng = anyDouble("clienteLng", "deliveryLng", "destinoLng"),
        valorCorrida = anyDouble("valorCorrida", "valorEntrega", "taxaEntrega", "valor"),
        distanciaKm = anyDouble("distanciaKm", "distancia", "km"),
        tempoEstimadoMin = anyDouble("tempoEstimadoMin", "tempoMin", "etaMin").roundToInt(),
        criadaEm = getTimestamp("criadaEm") ?: getTimestamp("createdAt"),
        atualizadaEm = getTimestamp("atualizadaEm") ?: getTimestamp("updatedAt")
    )
}

fun DocumentSnapshot.toDriverNotification(): DriverNotification {
    fun str(vararg keys: String): String = keys.firstNotNullOfOrNull { getString(it) }.orEmpty()
    return DriverNotification(
        id = id,
        titulo = str("titulo", "title").ifBlank { "Notificação" },
        mensagem = str("mensagem", "body"),
        tipo = str("tipo", "type").ifBlank { "INFO" },
        lida = getBoolean("lida") ?: false,
        criadaEm = getTimestamp("criadaEm") ?: getTimestamp("createdAt")
    )
}

private fun DocumentSnapshot.anyDouble(vararg keys: String): Double {
    for (key in keys) {
        val value = get(key)
        when (value) {
            is Double -> return value
            is Float -> return value.toDouble()
            is Long -> return value.toDouble()
            is Int -> return value.toDouble()
            is String -> value.replace(",", ".").toDoubleOrNull()?.let { return it }
        }
    }
    return 0.0
}
