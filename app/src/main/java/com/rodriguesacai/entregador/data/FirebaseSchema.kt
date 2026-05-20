package com.rodriguesacai.entregador.data

object Collections {
    const val DRIVERS = "entregadores"
    const val RIDES = "corridas"
    const val ORDERS = "pedidos"
    const val NOTIFICATIONS = "notificacoes"
    const val OCCURRENCES = "ocorrencias"
    const val CHANGE_REQUESTS = "solicitacoesAlteracao"
    const val REGISTRATION_REQUESTS = "cadastrosEntregador"
    const val CONFIG = "configuracoes"
}

object RideStatus {
    const val OFFER = "OFERTA_RECEBIDA"
    const val ACCEPTED = "ACEITA"
    const val GOING_PICKUP = "INDO_COLETA"
    const val AT_PICKUP = "CHEGUEI_COLETA"
    const val PICKED_UP = "PEDIDO_RETIRADO"
    const val GOING_DELIVERY = "INDO_ENTREGA"
    const val AT_CUSTOMER = "ENTREGADOR_NO_LOCAL"
    const val FINISHED = "FINALIZADA"
    const val REFUSED = "RECUSADA"
    const val EXPIRED = "EXPIRADA"
    const val OCCURRENCE = "OCORRENCIA"
}

object DriverStatus {
    const val AVAILABLE = "DISPONIVEL"
    const val UNAVAILABLE = "INDISPONIVEL"
    const val RESTRICTED = "RESTRICAO"
}
