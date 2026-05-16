package com.rodriguesacai.entregador.data

enum class DriverStep { INDISPONIVEL, DISPONIVEL, RESTRICAO, EM_OFERTA, INDO_COLETA, CHEGUEI_COLETA, PEDIDO_RETIRADO, INDO_ENTREGA, FINALIZADO }

data class RideOffer(
    val id: String = "", val pedidoId: String = "", val pedidoNumero: String = "", val entregadorId: String = "",
    val lojaNome: String = "Rodrigues Açaí e Cia.", val cliente: String = "", val bairro: String = "", val endereco: String = "",
    val enderecoCompleto: String = "", val itens: String = "", val valorEntrega: Double = 0.0, val totalPedido: Double = 0.0,
    val distanciaKm: Double = 0.0, val tempoMin: Int = 0, val formaPagamento: String = "", val trocoPara: Double = 0.0,
    val status: String = "enviada", val expiraEm: Long = 0L
)

data class DriverUiState(
    val isDark: Boolean = false, val isAvailable: Boolean = false, val activeTab: String = "home", val step: DriverStep = DriverStep.INDISPONIVEL,
    val pendingOffer: RideOffer? = null, val activeRide: RideOffer? = null, val earningsVisible: Boolean = true, val error: String? = null
)
