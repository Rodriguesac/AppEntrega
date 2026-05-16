package com.rodriguesacai.entregador.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository(
    private val driverId: String = "diego",
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun listenOpenOffers(): Flow<RideOffer?> = callbackFlow {
        val registration = db.collection("ofertas_corrida")
            .whereEqualTo("entregadorId", driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val now = System.currentTimeMillis()
                val offer = snapshot?.documents
                    ?.map { it.toRideOffer() }
                    ?.filter {
                        val open = it.status in setOf("nova", "enviada", "visualizada")
                        val notExpired = it.expiraEm == 0L || it.expiraEm > now
                        open && notExpired
                    }
                    ?.maxByOrNull { it.expiraEm }

                trySend(offer)
            }

        awaitClose { registration.remove() }
    }

    suspend fun setDriverAvailable(available: Boolean) {
        val status = if (available) "disponivel" else "indisponivel"
        db.collection("entregadores").document(driverId).set(
            mapOf(
                "id" to driverId,
                "nome" to "Diego",
                "online" to available,
                "disponivel" to available,
                "status" to status,
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun markVisualized(offer: RideOffer) {
        if (offer.id.isBlank()) return
        db.collection("ofertas_corrida").document(offer.id).set(
            mapOf(
                "status" to "visualizada",
                "visualizadaEm" to FieldValue.serverTimestamp(),
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun acceptOffer(offer: RideOffer) {
        if (offer.id.isBlank()) return
        val batch = db.batch()
        val offerRef = db.collection("ofertas_corrida").document(offer.id)
        val rideRef = db.collection("corridas").document(offer.id)
        val driverRef = db.collection("entregadores").document(driverId)

        batch.set(
            offerRef,
            mapOf(
                "status" to "aceita",
                "etapaEntrega" to "indo_coleta",
                "aceitaEm" to FieldValue.serverTimestamp(),
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )

        batch.set(
            rideRef,
            mapOf(
                "id" to offer.id,
                "pedidoId" to offer.pedidoId,
                "ofertaId" to offer.id,
                "entregadorId" to driverId,
                "status" to "indo_coleta",
                "valorEntrega" to offer.valorEntrega,
                "distanciaKm" to offer.distanciaKm,
                "tempoEstimadoMin" to offer.tempoMin,
                "clienteNome" to offer.cliente,
                "clienteEndereco" to offer.enderecoCompleto,
                "clienteBairro" to offer.bairro,
                "formaPagamento" to offer.formaPagamento,
                "aceitaEm" to FieldValue.serverTimestamp(),
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )

        if (offer.pedidoId.isNotBlank()) {
            val orderRef = db.collection("pedidos").document(offer.pedidoId)
            batch.set(
                orderRef,
                mapOf(
                    "status" to "entregador_aceitou",
                    "entregadorId" to driverId,
                    "ofertaId" to offer.id,
                    "atualizadoEm" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }

        batch.set(
            driverRef,
            mapOf(
                "online" to true,
                "disponivel" to false,
                "status" to "em_corrida",
                "corridaAtualId" to offer.id,
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )

        batch.commit().await()
    }

    suspend fun rejectOffer(offer: RideOffer, reason: String = "") {
        if (offer.id.isBlank()) return
        db.collection("ofertas_corrida").document(offer.id).set(
            mapOf(
                "status" to "rejeitada",
                "motivoRejeicao" to reason,
                "rejeitadaEm" to FieldValue.serverTimestamp(),
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()

        if (offer.pedidoId.isNotBlank()) {
            db.collection("pedidos").document(offer.pedidoId).set(
                mapOf(
                    "status" to "aguardando_entregador",
                    "atualizadoEm" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }
    }

    suspend fun updateStep(offer: RideOffer, step: DriverStep) {
        if (offer.id.isBlank()) return
        val status = when (step) {
            DriverStep.INDO_COLETA -> "indo_coleta"
            DriverStep.CHEGUEI_COLETA -> "cheguei_na_coleta"
            DriverStep.PEDIDO_RETIRADO -> "pedido_retirado"
            DriverStep.INDO_ENTREGA -> "indo_entrega"
            DriverStep.FINALIZADO -> "finalizada"
            else -> "em_corrida"
        }

        val batch = db.batch()
        batch.set(
            db.collection("ofertas_corrida").document(offer.id),
            mapOf("etapaEntrega" to status, "atualizadoEm" to FieldValue.serverTimestamp()),
            com.google.firebase.firestore.SetOptions.merge()
        )
        batch.set(
            db.collection("corridas").document(offer.id),
            mapOf("status" to status, "atualizadoEm" to FieldValue.serverTimestamp()),
            com.google.firebase.firestore.SetOptions.merge()
        )
        if (offer.pedidoId.isNotBlank()) {
            batch.set(
                db.collection("pedidos").document(offer.pedidoId),
                mapOf("status" to status, "atualizadoEm" to FieldValue.serverTimestamp()),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
        batch.commit().await()
    }

    suspend fun finishRide(offer: RideOffer) {
        if (offer.id.isBlank()) return
        val batch = db.batch()
        batch.set(
            db.collection("ofertas_corrida").document(offer.id),
            mapOf(
                "status" to "finalizada",
                "etapaEntrega" to "finalizada",
                "finalizadaEm" to FieldValue.serverTimestamp(),
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
        batch.set(
            db.collection("corridas").document(offer.id),
            mapOf(
                "status" to "finalizada",
                "finalizadaEm" to FieldValue.serverTimestamp(),
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
        batch.set(
            db.collection("historico_entregador").document("${driverId}_${offer.id}"),
            mapOf(
                "entregadorId" to driverId,
                "corridaId" to offer.id,
                "pedidoId" to offer.pedidoId,
                "valorEntrega" to offer.valorEntrega,
                "cliente" to offer.cliente,
                "bairro" to offer.bairro,
                "status" to "finalizada",
                "criadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
        batch.set(
            db.collection("entregadores").document(driverId),
            mapOf(
                "online" to true,
                "disponivel" to true,
                "status" to "disponivel",
                "corridaAtualId" to "",
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
        if (offer.pedidoId.isNotBlank()) {
            batch.set(
                db.collection("pedidos").document(offer.pedidoId),
                mapOf(
                    "status" to "entregue",
                    "entregueEm" to FieldValue.serverTimestamp(),
                    "atualizadoEm" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
        batch.commit().await()
    }

    private fun DocumentSnapshot.toRideOffer(): RideOffer {
        return RideOffer(
            id = id,
            pedidoId = getString("pedidoId").orEmpty(),
            pedidoNumero = getString("pedidoNumero").orEmpty(),
            entregadorId = getString("entregadorId") ?: driverId,
            lojaNome = getString("lojaNome") ?: "Rodrigues Açaí e Cia.",
            cliente = getString("cliente") ?: getString("clienteNome") ?: "Cliente",
            bairro = getString("bairro").orEmpty(),
            endereco = getString("endereco").orEmpty(),
            enderecoCompleto = getString("enderecoCompleto").orEmpty(),
            itens = getString("itens") ?: getString("itensResumo") ?: "",
            valorEntrega = number("valorEntrega"),
            totalPedido = number("totalPedido"),
            distanciaKm = number("distanciaKm"),
            tempoMin = number("tempoMin").toInt(),
            formaPagamento = getString("formaPagamento").orEmpty(),
            trocoPara = number("trocoPara"),
            status = getString("status").orEmpty(),
            expiraEm = longMillis("expiraEm")
        )
    }

    private fun DocumentSnapshot.number(field: String): Double {
        val raw = get(field)
        return when (raw) {
            is Number -> raw.toDouble()
            is String -> raw.replace(",", ".").toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    private fun DocumentSnapshot.longMillis(field: String): Long {
        val raw = get(field)
        return when (raw) {
            is Number -> raw.toLong()
            is Timestamp -> raw.toDate().time
            is String -> raw.toLongOrNull() ?: 0L
            else -> 0L
        }
    }
}
