package com.rodriguesacai.entregador.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository(private val context: Context) {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage
    private val prefs: SharedPreferences = context.getSharedPreferences("rod_entregador", Context.MODE_PRIVATE)

    var driverId: String
        get() = prefs.getString("driverId", "") ?: ""
        private set(value) = prefs.edit().putString("driverId", value).apply()

    val hasSession: Boolean get() = driverId.isNotBlank()

    suspend fun login(identifier: String, password: String): Result<String> = runCatching {
        ensureFirebaseSession()
        val clean = identifier.filter { it.isDigit() }
        if (clean.isBlank()) throw IllegalStateException("Informe CPF ou telefone.")
        if (password.isBlank()) throw IllegalStateException("Informe sua senha.")

        val candidates = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()
        suspend fun queryBy(field: String) {
            val snap = db.collection("entregadores").whereEqualTo(field, clean).limit(5).get().await()
            candidates.addAll(snap.documents)
        }
        queryBy("cpf")
        queryBy("cpfNormalizado")
        queryBy("telefone")
        queryBy("telefoneNormalizado")

        val doc = candidates.distinctBy { it.id }.firstOrNull { candidate ->
            val senha = candidate.getString("senhaApp") ?: candidate.getString("senha") ?: candidate.getString("senhaTemporaria")
            senha == password
        } ?: throw IllegalStateException("Entregador não encontrado ou senha incorreta.")

        val statusCadastro = doc.getString("statusCadastro") ?: doc.getString("status") ?: ""
        if (statusCadastro.equals("PENDENTE", true) || statusCadastro.equals("EM_ANALISE", true)) {
            throw IllegalStateException("Cadastro ainda está em análise.")
        }

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

    suspend fun submitRegistration(nome: String, cpf: String, telefone: String, placa: String, documentoUri: String?, selfieUri: String?) {
        ensureFirebaseSession()
        val cleanCpf = cpf.filter { it.isDigit() }
        val cleanPhone = telefone.filter { it.isDigit() }
        if (nome.isBlank()) throw IllegalStateException("Informe o nome completo.")
        if (cleanCpf.length != 11) throw IllegalStateException("Informe um CPF válido.")
        if (cleanPhone.length < 10) throw IllegalStateException("Informe um telefone válido.")

        val cadastroId = UUID.randomUUID().toString()
        val documentoUrl = uploadIfPresent("cadastrosEntregador/$cadastroId/documento", documentoUri)
        val selfieUrl = uploadIfPresent("cadastrosEntregador/$cadastroId/selfie", selfieUri)

        db.collection("cadastrosEntregador").document(cadastroId).set(
            mapOf(
                "nome" to nome.trim(),
                "cpfNormalizado" to cleanCpf,
                "telefoneNormalizado" to cleanPhone,
                "placa" to placa.uppercase().trim(),
                "documentoUrl" to documentoUrl,
                "selfieUrl" to selfieUrl,
                "status" to "PENDENTE",
                "origem" to "APP_NATIVO",
                "criadaEm" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    private suspend fun uploadIfPresent(path: String, uriString: String?): String {
        if (uriString.isNullOrBlank()) return ""
        val uri = Uri.parse(uriString)
        val ref = storage.reference.child("$path-${System.currentTimeMillis()}")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun createPassword(identifier: String, password: String) {
        ensureFirebaseSession()
        val clean = identifier.filter { it.isDigit() }
        if (clean.length != 11) throw IllegalStateException("Informe o CPF cadastrado.")
        if (password.length < 6) throw IllegalStateException("A senha precisa ter pelo menos 6 caracteres.")
        val snap = db.collection("entregadores")
            .whereEqualTo("cpfNormalizado", clean)
            .limit(1)
            .get()
            .await()
        val doc = snap.documents.firstOrNull() ?: throw IllegalStateException("Cadastro aprovado não encontrado para criar senha.")
        db.collection("entregadores").document(doc.id).set(
            mapOf(
                "senhaApp" to password,
                "senhaCriadaEm" to FieldValue.serverTimestamp(),
                "appNativo" to true
            ),
            SetOptions.merge()
        ).await()
    }

    fun logout() { prefs.edit().clear().apply() }

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
                else onData(snap?.documents?.map { it.toRide() } ?: emptyList())
            }
    }

    fun listenHistory(onData: (List<Ride>) -> Unit, onError: (Throwable) -> Unit): ListenerRegistration? {
        val id = driverId.ifBlank { return null }
        return db.collection("corridas")
            .whereEqualTo("entregadorUid", id)
            .orderBy("atualizadaEm", Query.Direction.DESCENDING)
            .limit(50)
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
                "motivoRecusa" to motivo.ifBlank { "Sem motivo informado" },
                "recusadaEm" to FieldValue.serverTimestamp(),
                "atualizadaEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        db.collection("entregadores").document(id).set(mapOf("corridaAtualId" to null, "pedidoAtualId" to null), SetOptions.merge()).await()
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
            db.collection("entregadores").document(driverId).set(mapOf("corridaAtualId" to null, "pedidoAtualId" to null), SetOptions.merge()).await()
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
                "pixChave" to chave.trim(),
                "pixTipo" to tipo.trim(),
                "banco" to banco.trim(),
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
                "tipo" to tipo.trim(),
                "novoValor" to novoValor.trim(),
                "observacao" to observacao.trim(),
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
                "motivo" to motivo.trim(),
                "detalhe" to detalhe.trim(),
                "status" to "ABERTA",
                "criadaEm" to FieldValue.serverTimestamp()
            )
        ).await()
        db.collection("corridas").document(rideId).set(
            mapOf(
                "status" to "OCORRENCIA",
                "ocorrenciaMotivo" to motivo.trim(),
                "atualizadaEm" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun updateLocation(lat: Double, lng: Double) {
        val id = driverId.ifBlank { return }
        val doc = db.collection("entregadores").document(id).get().await()
        val corridaAtual = doc.getString("corridaAtualId")
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
            mapOf("fcmToken" to token, "fcmAtualizadoEm" to FieldValue.serverTimestamp()),
            SetOptions.merge()
        ).await()
    }
}
