package com.rodriguesacai.entregador.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val driverId = "diego"
    private val openStatuses = setOf("nova", "enviada", "visualizada")

    fun listenPendingOffers(onOffer: (RideOffer?) -> Unit, onError: (Throwable) -> Unit): ListenerRegistration {
        return db.collection("ofertas_corrida").whereEqualTo("entregadorId", driverId).addSnapshotListener { snap, err ->
            if (err != null) { onError(err); return@addSnapshotListener }
            val offer = snap?.documents?.mapNotNull { it.toRideOffer() }?.filter { it.status in openStatuses }?.maxByOrNull { it.expiraEm }
            onOffer(offer)
        }
    }

    suspend fun setDriverAvailable(available: Boolean) {
        db.collection("entregadores").document(driverId).set(mapOf(
            "id" to driverId, "nome" to "Diego", "status" to if (available) "online" else "offline",
            "online" to available, "disponivel" to available, "gpsAtivo" to true, "notificacaoAtiva" to true,
            "permissaoUrgenteAtiva" to true, "atualizadoEm" to Timestamp.now()
        ), SetOptions.merge()).await()
    }

    suspend fun markVisualized(offer: RideOffer) {
        if (offer.id.isBlank()) return
        db.collection("ofertas_corrida").document(offer.id).set(mapOf("status" to "visualizada", "visualizadaEm" to Timestamp.now(), "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
    }

    suspend fun acceptOffer(offer: RideOffer) {
        db.collection("ofertas_corrida").document(offer.id).set(mapOf("status" to "aceita", "aceitaEm" to Timestamp.now(), "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
        if (offer.pedidoId.isNotBlank()) db.collection("pedidos").document(offer.pedidoId).set(mapOf("status" to "entregador_aceitou", "entregadorId" to driverId, "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
        db.collection("entregadores").document(driverId).set(mapOf("status" to "indo_coleta", "disponivel" to false, "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
        db.collection("corridas").document(offer.id).set(mapOf("ofertaId" to offer.id, "pedidoId" to offer.pedidoId, "entregadorId" to driverId, "status" to "indo_coleta", "valorEntrega" to offer.valorEntrega, "distanciaKm" to offer.distanciaKm, "tempoMin" to offer.tempoMin, "criadoEm" to Timestamp.now(), "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
    }

    suspend fun rejectOffer(offer: RideOffer, motivo: String) {
        db.collection("ofertas_corrida").document(offer.id).set(mapOf("status" to "rejeitada", "motivoRejeicao" to motivo, "rejeitadaEm" to Timestamp.now(), "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
        if (offer.pedidoId.isNotBlank()) db.collection("pedidos").document(offer.pedidoId).set(mapOf("status" to "aguardando_entregador", "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
    }

    suspend fun updateStep(offer: RideOffer, status: String) {
        db.collection("ofertas_corrida").document(offer.id).set(mapOf("etapaEntrega" to status, "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
        db.collection("corridas").document(offer.id).set(mapOf("status" to status, "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
        if (offer.pedidoId.isNotBlank()) db.collection("pedidos").document(offer.pedidoId).set(mapOf("status" to status, "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
    }

    suspend fun finishRide(offer: RideOffer) {
        db.collection("ofertas_corrida").document(offer.id).set(mapOf("status" to "finalizada", "finalizadaEm" to Timestamp.now(), "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
        if (offer.pedidoId.isNotBlank()) db.collection("pedidos").document(offer.pedidoId).set(mapOf("status" to "entregue", "entregueEm" to Timestamp.now(), "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
        db.collection("corridas").document(offer.id).set(mapOf("status" to "finalizada", "finalizadaEm" to Timestamp.now(), "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
        db.collection("historico_entregador").add(mapOf("entregadorId" to driverId, "ofertaId" to offer.id, "pedidoId" to offer.pedidoId, "pedidoNumero" to offer.pedidoNumero, "valorEntrega" to offer.valorEntrega, "bairro" to offer.bairro, "status" to "entregue", "criadoEm" to Timestamp.now())).await()
        db.collection("financeiro_entregadores").add(mapOf("entregadorId" to driverId, "ofertaId" to offer.id, "pedidoId" to offer.pedidoId, "valorEntrega" to offer.valorEntrega, "tipo" to "corrida_finalizada", "criadoEm" to Timestamp.now())).await()
        db.collection("entregadores").document(driverId).set(mapOf("status" to "online", "disponivel" to true, "atualizadoEm" to Timestamp.now()), SetOptions.merge()).await()
    }

    private fun DocumentSnapshot.toRideOffer(): RideOffer = RideOffer(
        id = id,
        pedidoId = getString("pedidoId") ?: "",
        pedidoNumero = getString("pedidoNumero") ?: getString("numero") ?: "",
        entregadorId = getString("entregadorId") ?: "",
        lojaNome = getString("lojaNome") ?: "Rodrigues Açaí e Cia.",
        cliente = getString("cliente") ?: getString("clienteNome") ?: "",
        bairro = getString("bairro") ?: "",
        endereco = getString("endereco") ?: "",
        enderecoCompleto = getString("enderecoCompleto") ?: getString("endereco") ?: "",
        itens = getString("itens") ?: getString("itensResumo") ?: "",
        valorEntrega = getNumberSafe("valorEntrega", "valores.corrida", "taxaEntrega"),
        totalPedido = getNumberSafe("totalPedido", "valores.pedido", "total"),
        distanciaKm = getNumberSafe("distanciaKm", "rota.distanciaKm"),
        tempoMin = getNumberSafe("tempoMin", "rota.tempoMin").toInt(),
        formaPagamento = getString("formaPagamento") ?: getString("pagamento.tipo") ?: "",
        trocoPara = getNumberSafe("trocoPara", "pagamento.trocoPara"),
        status = getString("status") ?: "enviada",
        expiraEm = (get("expiraEm") as? Number)?.toLong() ?: 0L
    )

    private fun DocumentSnapshot.getNumberSafe(vararg keys: String): Double {
        keys.forEach { key ->
            val direct = get(key)
            if (direct is Number) return direct.toDouble()
            if (key.contains(".")) {
                val parts = key.split(".")
                val root = get(parts[0]) as? Map<*, *>
                val value = root?.get(parts[1])
                if (value is Number) return value.toDouble()
            }
        }
        return 0.0
    }
}
