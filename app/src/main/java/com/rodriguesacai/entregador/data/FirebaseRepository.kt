package com.rodriguesacai.entregador.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseRepository(context: Context) {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val prefs: SharedPreferences = context.getSharedPreferences("rod_entregador", Context.MODE_PRIVATE)

    var driverId: String
        get() = prefs.getString("driverId", "") ?: ""
        private set(value) = prefs.edit().putString("driverId", value).apply()

    val hasSession: Boolean get() = driverId.isNotBlank()

    suspend fun login(identifier: String, password: String): Result<String> = runCatching {
        ensureFirebaseSession()
        val clean = identifier.filter { it.isDigit() }
        val candidates = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()

        suspend fun queryBy(field: String) {
            val snap = db.collection("entregadores").whereEqualTo(field, clean).limit(3).get().await()
            candidates.addAll(snap.documents)
        }

        queryBy("cpf")
        queryBy("cpfNormalizado")
        queryBy("telefone")
        queryBy("telefoneNormalizado")

        val doc = candidates.distinctBy { it.id }.firstOrNull { candidate ->
            val senha = candidate.getString("senhaApp") ?: candidate.getString("senha") ?: candidate.getString("senhaTemporaria")
            senha.isNullOrBlank() || senha == password
        } ?: throw IllegalStateException("Entregador não encontrado ou senha incorreta.")

        driverId = doc.id
        db.collection("entregadores").document(doc.id).set(
            mapOf(
                "ultimoLoginEm" to FieldValue.serverTimestamp(),
                "appNativo" to true,
                "fcmPreparado" to true
            ),
            SetOptions.merge()
        ).await()
        doc.id
    }

    suspend fun enterDemo(): String {
        ensureFirebaseSession()
        driverId = "entregador_demo"
        db.collection("entregadores").document(driverId).set(
            mapOf(
                "nome" to "Diego",
                "cpfNormalizado" to "00000000000",
                "telefoneNormalizado" to "67999999999",
                "statusOperacional" to "INDISPONIVEL",
                "online" to false,
                "verificado" to true,
                "appNativo" to true,
                "saldoHoje" to 0.0,
                "saldoSemana" to 0.0,
                "saldoMes" to 0.0,
                "corridasHoje" to 0,
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        return driverId
    }

    suspend fun submitRegistration(nome: String, cpf: String, telefone: String, placa: String) {
        ensureFirebaseSession()
        val cleanCpf = cpf.filter { it.isDigit() }
        val cleanPhone = telefone.filter { it.isDigit() }
        db.collection("cadastrosEntregador").add(
            mapOf(
                "nome" to nome,
                "cpfNormalizado" to cleanCpf,
                "telefoneNormalizado" to cleanPhone,
                "placa" to placa.uppercase(),
                "status" to "PENDENTE",
                "origem" to "APP_NATIVO",
                "criadaEm" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun createPassword(identifier: String, password: String) {
        ensureFirebaseSession()
        val clean = identifier.filter { it.isDigit() }
        val snap = db.collection("entregadores")
            .whereEqualTo("cpfNormalizado", clean)
            .limit(1)
            .get()
            .await()
        val doc = snap.documents.firstOrNull() ?: throw IllegalStateException("Cadastro não encontrado para criar senha.")
        db.collection("entregadores").document(doc.id).set(
            mapOf(
                "senhaApp" to password,
                "senhaCriadaEm" to FieldValue.serverTimestamp(),
                "appNativo" to true
            ),
            SetOptions.merge()
        ).await()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    suspend fun ensureFirebaseSession() {
        if (auth.currentUser == null) auth.signInAnonymously().await()
    }

    fun listenDriver(onData: (Driver?) -> Unit, onError: (Throwable) -> Unit): ListenerRegistration? {
        val id = driverId.ifBlank { return null }
        return db.collection("entregadores").document(id)
            .addSnapshotListener { snap, error ->
                if (error != null) onError(error)
                else onData(snap?.takeIf { it.exists() }?.toDriver())
            }
    }

    fun listenActiveRides(onData: (List<Ride>) -> Unit, onError: (Throwable) -> Unit): ListenerRegistration? {
        val id = driverId.ifBlank { return null }
        return db.collection("corridas")
            .whereEqualTo("entregadorUid", id)
            .whereIn("status", listOf("OFERTA_RECEBIDA", "ACEITA", "INDO_COLETA", "CHEGUEI_COLETA", "PEDIDO_RETIRADO", "INDO_ENTREGA", "ENTREGADOR_NO_LOCAL", "OCORRENCIA"))
            .addSnapshotListener { snap, error ->
                if (error != null) onError(error)
                else onData(snap?.documents?.map { it.toRide() }?.sortedBy { it.status } ?: emptyList())
            }
    }

    fun listenHistory(onData: (List<Ride>) -> Unit, onError: (Throwable) -> Unit): ListenerRegistration? {
        val id = driverId.ifBlank { return null }
        return db.collection("corridas")
            .whereEqualTo("entregadorUid", id)
            .orderBy("atualizadaEm", Query.Direction.DESCENDING)
            .limit(40)
            .addSnapshotListener { snap, error ->
                if (error != null) onError(error)
                else onData(snap?.documents?.map { it.toRide() } ?: emptyList())
            }
    }

    fun listenNotifications(onData: (List<DriverNotification>) -> Unit, onError: (Throwable) -> Unit): ListenerRegistration? {
        val id = driverId.ifBlank { return null }
        return db.collection("notificacoes")
            .whereEqualTo("entregadorUid", id)
            .orderBy("criadaEm", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snap, error ->
                if (error != null) onError(error)
                else onData(snap?.documents?.map { it.toDriverNotification() } ?: emptyList())
            }
    }

    suspend fun setOnline(online: Boolean) {
        val id = driverId.ifBlank { return }
        val status = if (online) "DISPONIVEL" else "INDISPONIVEL"
        db.collection("entregadores").document(id).set(
            mapOf(
                "online" to online,
                "statusOperacional" to status,
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun acceptRide(rideId: String) = updateRide(rideId, "ACEITA", "aceitaEm")

    suspend fun rejectRide(rideId: String, motivo: String) {
        val id = driverId.ifBlank { return }
        db.collection("corridas").document(rideId).set(
            mapOf(
                "status" to "RECUSADA",
                "motivoRecusa" to motivo,
                "recusadaEm" to FieldValue.serverTimestamp(),
                "atualizadaEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        db.collection("entregadores").document(id).set(mapOf("corridaAtualId" to null), SetOptions.merge()).await()
    }

    suspend fun advanceRide(ride: Ride) {
        val next = when (ride.status) {
            "ACEITA" -> "INDO_COLETA"
            "INDO_COLETA" -> "CHEGUEI_COLETA"
            "CHEGUEI_COLETA" -> "PEDIDO_RETIRADO"
            "PEDIDO_RETIRADO" -> "INDO_ENTREGA"
            "INDO_ENTREGA" -> "ENTREGADOR_NO_LOCAL"
            "ENTREGADOR_NO_LOCAL" -> "FINALIZADA"
            else -> ride.status
        }
        val field = when (next) {
            "INDO_COLETA" -> "indoColetaEm"
            "CHEGUEI_COLETA" -> "chegouColetaEm"
            "PEDIDO_RETIRADO" -> "retiradoEm"
            "INDO_ENTREGA" -> "indoEntregaEm"
            "ENTREGADOR_NO_LOCAL" -> "chegouClienteEm"
            "FINALIZADA" -> "finalizadaEm"
            else -> "atualizadaEm"
        }
        updateRide(ride.id, next, field)
        if (next == "FINALIZADA") {
            db.collection("entregadores").document(driverId).set(mapOf("corridaAtualId" to null), SetOptions.merge()).await()
        }
    }

    private suspend fun updateRide(rideId: String, status: String, timestampField: String) {
        val id = driverId.ifBlank { return }
        db.collection("corridas").document(rideId).set(
            mapOf(
                "status" to status,
                timestampField to FieldValue.serverTimestamp(),
                "atualizadaEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        if (status != "FINALIZADA" && status != "RECUSADA") {
            db.collection("entregadores").document(id).set(
                mapOf("corridaAtualId" to rideId, "pedidoAtualId" to rideId),
                SetOptions.merge()
            ).await()
        }
    }

    suspend fun savePixBank(chave: String, tipo: String, banco: String) {
        val id = driverId.ifBlank { return }
        db.collection("entregadores").document(id).set(
            mapOf(
                "pixChave" to chave,
                "pixTipo" to tipo,
                "banco" to banco,
                "dadosRecebimentoAtualizadosEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun toggleValues(hidden: Boolean) {
        val id = driverId.ifBlank { return }
        db.collection("entregadores").document(id).set(mapOf("ocultarValores" to hidden), SetOptions.merge()).await()
    }

    suspend fun requestChange(tipo: String, novoValor: String, observacao: String) {
        val id = driverId.ifBlank { return }
        db.collection("solicitacoesAlteracao").add(
            mapOf(
                "entregadorUid" to id,
                "tipo" to tipo,
                "novoValor" to novoValor,
                "observacao" to observacao,
                "status" to "PENDENTE",
                "criadaEm" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun createOccurrence(rideId: String, motivo: String, detalhe: String) {
        val id = driverId.ifBlank { return }
        db.collection("ocorrencias").add(
            mapOf(
                "entregadorUid" to id,
                "corridaId" to rideId,
                "motivo" to motivo,
                "detalhe" to detalhe,
                "status" to "ABERTA",
                "criadaEm" to FieldValue.serverTimestamp()
            )
        ).await()
        db.collection("corridas").document(rideId).set(
            mapOf(
                "status" to "OCORRENCIA",
                "ocorrenciaMotivo" to motivo,
                "atualizadaEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun updateLocation(lat: Double, lng: Double) {
        val id = driverId.ifBlank { return }
        val corridaAtual = db.collection("entregadores").document(id).get().await().getString("corridaAtualId")
        db.collection("entregadores").document(id).set(
            mapOf(
                "coords" to mapOf("lat" to lat, "lng" to lng),
                "lat" to lat,
                "lng" to lng,
                "localizacaoAtualizadaEm" to Timestamp.now(),
                "rastreamentoAtivo" to true
            ),
            SetOptions.merge()
        ).await()
        if (!corridaAtual.isNullOrBlank()) {
            db.collection("corridas").document(corridaAtual).set(
                mapOf(
                    "entregadorCoords" to mapOf("lat" to lat, "lng" to lng),
                    "localizacaoEntregadorAtualizadaEm" to Timestamp.now()
                ),
                SetOptions.merge()
            ).await()
        }
    }

    suspend fun saveFcmToken(token: String) {
        ensureFirebaseSession()
        val id = driverId.ifBlank { return }
        db.collection("entregadores").document(id).set(
            mapOf(
                "fcmToken" to token,
                "fcmAtualizadoEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }
}
