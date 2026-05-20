package com.rodriguesacai.entregador.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.math.roundToInt

data class Driver(
    val id: String = "",
    val nome: String = "",
    val telefone: String = "",
    val email: String = "",
    val cpf: String = "",
    val fotoUrl: String = "",
    val statusOperacional: String = "INDISPONIVEL",
    val verificado: Boolean = false,
    val online: Boolean = false,
    val pixChave: String = "",
    val pixTipo: String = "",
    val banco: String = "",
    val agencia: String = "",
    val conta: String = "",
    val tipoConta: String = "",
    val saldoHoje: Double? = null,
    val saldoSemana: Double? = null,
    val saldoMes: Double? = null,
    val saldoDisponivel: Double? = null,
    val saldoPendente: Double? = null,
    val totalAReceber: Double? = null,
    val corridasHoje: Int? = null,
    val ocultarValores: Boolean = false,
    val restricaoMotivo: String = ""
)

data class Ride(
    val id: String = "",
    val pedidoId: String = "",
    val numeroPedido: String = "",
    val entregadorUid: String = "",
    val status: String = "OFERTA_RECEBIDA",
    val lojaNome: String = "",
    val lojaEndereco: String = "",
    val lojaLat: Double? = null,
    val lojaLng: Double? = null,
    val clienteNome: String = "",
    val clienteBairro: String = "",
    val clienteEnderecoCompleto: String = "",
    val clienteLat: Double? = null,
    val clienteLng: Double? = null,
    val valorCorrida: Double? = null,
    val distanciaKm: Double? = null,
    val tempoEstimadoMin: Int? = null,
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

fun DocumentSnapshot.toDriver(): Driver {
    fun str(vararg keys: String): String = keys.firstNotNullOfOrNull { getString(it) }.orEmpty()
    return Driver(
        id = id,
        nome = str("nome", "name"),
        telefone = str("telefone", "phone", "telefoneNormalizado"),
        email = str("email", "e-mail"),
        cpf = str("cpf", "cpfNormalizado"),
        fotoUrl = str("fotoUrl", "urlPerfil", "photoUrl"),
        statusOperacional = str("statusOperacional", "status").ifBlank { "INDISPONIVEL" },
        verificado = getBoolean("verificado") ?: getBoolean("aprovado") ?: false,
        online = getBoolean("online") ?: false,
        pixChave = str("pixChave", "chavePix"),
        pixTipo = str("pixTipo", "tipoPix"),
        banco = str("banco"),
        agencia = str("agencia"),
        conta = str("conta"),
        tipoConta = str("tipoConta", "tipo_conta"),
        saldoHoje = optionalDouble("saldoHoje", "ganhosHoje", "hoje"),
        saldoSemana = optionalDouble("saldoSemana", "ganhosSemana", "semana"),
        saldoMes = optionalDouble("saldoMes", "ganhosMes", "mes"),
        saldoDisponivel = optionalDouble("saldoDisponivel", "carteiraSaldoDisponivel"),
        saldoPendente = optionalDouble("saldoPendente", "carteiraSaldoPendente"),
        totalAReceber = optionalDouble("totalAReceber", "totalReceber"),
        corridasHoje = optionalDouble("corridasHoje", "entregasHoje")?.roundToInt(),
        ocultarValores = getBoolean("ocultarValores") ?: false,
        restricaoMotivo = str("restricaoMotivo", "motivoRestricao")
    )
}

fun DocumentSnapshot.toRide(): Ride {
    fun str(vararg keys: String): String = keys.firstNotNullOfOrNull { getString(it) }.orEmpty()
    val numero = str("numeroPedido", "numeroCurto", "pedidoNumero")
    return Ride(
        id = id,
        pedidoId = str("pedidoId", "orderId"),
        numeroPedido = numero.ifBlank { id.takeLast(6).uppercase() },
        entregadorUid = str("entregadorUid", "driverId"),
        status = str("status", "statusCorrida").ifBlank { "OFERTA_RECEBIDA" },
        lojaNome = str("lojaNome", "storeName"),
        lojaEndereco = str("lojaEndereco", "enderecoLoja"),
        lojaLat = optionalDouble("lojaLat", "storeLat", "origemLat"),
        lojaLng = optionalDouble("lojaLng", "storeLng", "origemLng"),
        clienteNome = str("clienteNome", "customerName"),
        clienteBairro = str("clienteBairro", "bairro"),
        clienteEnderecoCompleto = str("clienteEnderecoCompleto", "enderecoCompleto", "deliveryAddress"),
        clienteLat = optionalDouble("clienteLat", "deliveryLat", "destinoLat"),
        clienteLng = optionalDouble("clienteLng", "deliveryLng", "destinoLng"),
        valorCorrida = optionalDouble("valorCorrida", "valorEntrega", "taxaEntrega", "valor"),
        distanciaKm = optionalDouble("distanciaKm", "distancia", "km"),
        tempoEstimadoMin = optionalDouble("tempoEstimadoMin", "tempoMin", "etaMin")?.roundToInt(),
        criadaEm = getTimestamp("criadaEm") ?: getTimestamp("createdAt"),
        atualizadaEm = getTimestamp("atualizadaEm") ?: getTimestamp("updatedAt")
    )
}

fun DocumentSnapshot.toDriverNotification(): DriverNotification {
    fun str(vararg keys: String): String = keys.firstNotNullOfOrNull { getString(it) }.orEmpty()
    return DriverNotification(
        id = id,
        titulo = str("titulo", "title"),
        mensagem = str("mensagem", "body"),
        tipo = str("tipo", "type").ifBlank { "INFO" },
        lida = getBoolean("lida") ?: false,
        criadaEm = getTimestamp("criadaEm") ?: getTimestamp("createdAt")
    )
}

private fun DocumentSnapshot.optionalDouble(vararg keys: String): Double? {
    for (key in keys) {
        if (!contains(key)) continue
        val value = get(key)
        when (value) {
            is Double -> return value
            is Float -> return value.toDouble()
            is Long -> return value.toDouble()
            is Int -> return value.toDouble()
            is String -> value.replace("R$", "").replace(".", "").replace(",", ".").trim().toDoubleOrNull()?.let { return it }
        }
    }
    return null
}
