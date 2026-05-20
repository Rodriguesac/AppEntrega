package com.rodriguesacai.entregador.data

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseRepo(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("rod_entregador", Context.MODE_PRIVATE)

    fun savedDriverUid(): String? = prefs.getString("driverUid", null)

    fun saveDriverUid(uid: String) {
        prefs.edit().putString("driverUid", uid).apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    suspend fun ensureFirebaseAuth() = withContext(Dispatchers.IO) {
        if (auth.currentUser == null) auth.signInAnonymously().await()
        auth.currentUser
    }

    suspend fun login(chave: String, senha: String): Result<String> = runCatching {
        ensureFirebaseAuth()
        val normalized = cleanNumber(chave).ifBlank { chave.trim() }
        if (normalized.equals("demo", true) || normalized.equals("teste", true)) {
            createDemoDriverIfNeeded()
            saveDriverUid("entregador_demo")
            return@runCatching "entregador_demo"
        }

        val snapshot = db.collection("entregadores").get().await()
        val match = snapshot.documents.firstOrNull { doc ->
            val data = doc.data ?: emptyMap()
            val cpf = cleanNumber(data.str("cpf") ?: "")
            val tel = cleanNumber(data.str("telefone", "celular", "phone") ?: "")
            val uid = doc.id
            val loginOk = normalized == uid || normalized == cpf || normalized == tel
            val senhaFirestore = data.str("senhaApp", "senha", "password", "pin")
            val senhaOk = senhaFirestore.isNullOrBlank() || senhaFirestore == senha
            loginOk && senhaOk
        } ?: error("Entregador não encontrado ou senha inválida.")

        saveDriverUid(match.id)
        updateFcmToken(match.id)
        match.id
    }

    suspend fun registerDriver(nome: String, cpf: String, telefone: String, placa: String): Result<Unit> = runCatching {
        ensureFirebaseAuth()
        val id = "cadastro_" + System.currentTimeMillis()
        db.collection("cadastros_entregadores").document(id).set(
            mapOf(
                "nome" to nome,
                "cpf" to cleanNumber(cpf),
                "telefone" to cleanNumber(telefone),
                "placa" to placa.uppercase(),
                "status" to "PENDENTE_ANALISE",
                "origem" to "APP_NATIVO",
                "criadaEm" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun createPassword(driverUid: String, senha: String): Result<Unit> = runCatching {
        ensureFirebaseAuth()
        db.collection("entregadores").document(driverUid).update(
            mapOf(
                "senhaApp" to senha,
                "senhaCriadaEm" to FieldValue.serverTimestamp(),
                "appAtualizadoEm" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    fun listenDriver(driverUid: String, onChange: (DriverProfile?) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection("entregadores").document(driverUid).addSnapshotListener { snap, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            onChange(if (snap?.exists() == true) snap.toDriver() else null)
        }
    }

    fun listenRides(driverUid: String, onChange: (List<Ride>) -> Unit, onError: (Exception) -> Unit): List<ListenerRegistration> {
        val state = mutableMapOf<String, List<Ride>>()
        fun emit() = onChange(state.values.flatten().sortedWith(compareByDescending<Ride> { it.isOffer }.thenByDescending { it.criadaEmMs }))
        fun belongs(ride: Ride): Boolean {
            val ids = listOf(
                ride.entregadorUid,
                ride.raw.str("entregadorUid", "entregadorId", "motoboyId", "uidEntregador", "driverId", "entregadorAtualOferta", "motoboyAtualOferta") ?: ""
            ).filter { it.isNotBlank() }
            val offerForAll = ride.isOffer && ids.isEmpty()
            return ids.contains(driverUid) || offerForAll
        }
        fun activeOrRecent(ride: Ride): Boolean = ride.isActive || ride.isFinished

        val collections = listOf("corridas", "rotas_entrega", "pedidos")
        return collections.map { collectionName ->
            db.collection(collectionName).addSnapshotListener { snap, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val rides = snap?.documents.orEmpty()
                    .map { it.toRide(collectionName) }
                    .filter { belongs(it) && activeOrRecent(it) }
                    .take(50)
                state[collectionName] = rides
                emit()
            }
        }
    }

    fun listenNotifications(driverUid: String, onChange: (List<AppNotification>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return db.collection("notificacoes").addSnapshotListener { snap, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            val items = snap?.documents.orEmpty().filter { doc ->
                val data = doc.data ?: emptyMap()
                val uid = data.str("entregadorUid", "driverUid", "uid")
                uid.isNullOrBlank() || uid == driverUid || data.bool("geral", "todos") == true
            }.map { it.toAppNotification() }.sortedByDescending { it.criadaEmMs }.take(30)
            onChange(items)
        }
    }

    suspend fun setAvailability(driverUid: String, available: Boolean): Result<Unit> = runCatching {
        ensureFirebaseAuth()
        db.collection("entregadores").document(driverUid).set(
            mapOf(
                "online" to available,
                "statusOperacional" to if (available) "DISPONIVEL" else "INDISPONIVEL",
                "appOnlineEm" to FieldValue.serverTimestamp(),
                "app" to "NATIVO_ANDROID"
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
        updateFcmToken(driverUid)
    }

    suspend fun acceptRide(ride: Ride, driverUid: String): Result<Unit> = runCatching {
        ensureFirebaseAuth()
        db.collection(ride.collection).document(ride.id).set(
            mapOf(
                "status" to "ACEITA",
                "statusEntregador" to "ACEITA",
                "entregadorUid" to driverUid,
                "aceitaEm" to FieldValue.serverTimestamp(),
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun rejectRide(ride: Ride, driverUid: String, motivo: String): Result<Unit> = runCatching {
        ensureFirebaseAuth()
        db.collection(ride.collection).document(ride.id).set(
            mapOf(
                "status" to "RECUSADA",
                "statusEntregador" to "RECUSADA",
                "recusadaPor" to driverUid,
                "motivoRecusa" to motivo,
                "recusadaEm" to FieldValue.serverTimestamp(),
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun advanceRide(ride: Ride): Result<Unit> = runCatching {
        ensureFirebaseAuth()
        val next = nextStatus(ride.status)
        val payload = mutableMapOf<String, Any>(
            "status" to next,
            "statusEntregador" to next,
            "atualizadoEm" to FieldValue.serverTimestamp()
        )
        when (next) {
            "CHEGUEI_COLETA" -> payload["chegouColetaEm"] = FieldValue.serverTimestamp()
            "PEDIDO_RETIRADO" -> payload["retiradoEm"] = FieldValue.serverTimestamp()
            "ENTREGADOR_NO_LOCAL" -> payload["chegouClienteEm"] = FieldValue.serverTimestamp()
            "FINALIZADA" -> payload["finalizadaEm"] = FieldValue.serverTimestamp()
        }
        db.collection(ride.collection).document(ride.id).set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    suspend fun saveBank(driverUid: String, pix: String, banco: String, tipoRepasse: String): Result<Unit> = runCatching {
        ensureFirebaseAuth()
        db.collection("entregadores").document(driverUid).set(
            mapOf(
                "pix" to pix,
                "banco" to banco,
                "tipoRepasse" to tipoRepasse,
                "recebimentoStatus" to "PENDENTE_CONFERENCIA",
                "recebimentoAtualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun requestChange(driverUid: String, tipo: String, novoValor: String, observacao: String): Result<Unit> = runCatching {
        ensureFirebaseAuth()
        db.collection("solicitacoesAlteracao").add(
            mapOf(
                "entregadorUid" to driverUid,
                "tipo" to tipo,
                "novoValor" to novoValor,
                "observacao" to observacao,
                "status" to "PENDENTE",
                "origem" to "APP_NATIVO",
                "criadaEm" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun createOccurrence(ride: Ride, driverUid: String, motivo: String, obs: String): Result<Unit> = runCatching {
        ensureFirebaseAuth()
        val payload = mapOf(
            "corridaId" to ride.id,
            "pedidoId" to ride.pedidoId,
            "entregadorUid" to driverUid,
            "motivo" to motivo,
            "observacao" to obs,
            "status" to "ABERTA",
            "origem" to "APP_NATIVO",
            "criadaEm" to FieldValue.serverTimestamp()
        )
        db.collection("ocorrencias").add(payload).await()
        db.collection(ride.collection).document(ride.id).set(
            mapOf(
                "status" to "OCORRENCIA",
                "statusEntregador" to "OCORRENCIA",
                "ocorrenciaAberta" to true,
                "motivoOcorrencia" to motivo,
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun updateFcmToken(driverUid: String) {
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            db.collection("entregadores").document(driverUid).set(
                mapOf(
                    "fcmToken" to token,
                    "fcmTokenAtualizadoEm" to FieldValue.serverTimestamp(),
                    "platform" to "ANDROID_NATIVE"
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }
    }

    private suspend fun createDemoDriverIfNeeded() {
        val ref = db.collection("entregadores").document("entregador_demo")
        val exists = ref.get().await().exists()
        if (!exists) {
            ref.set(
                mapOf(
                    "nome" to "Entregador Rodrigues",
                    "cpf" to "00000000000",
                    "telefone" to "67999999999",
                    "statusOperacional" to "INDISPONIVEL",
                    "online" to false,
                    "verificado" to true,
                    "pix" to "",
                    "banco" to "",
                    "tipoRepasse" to "Semanal",
                    "ganhosHoje" to 0,
                    "ganhosSemana" to 0,
                    "ganhosMes" to 0,
                    "proximoRepasse" to 0,
                    "app" to "NATIVO_ANDROID",
                    "criadoEm" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    private fun nextStatus(status: String): String = when (status.uppercase()) {
        "ACEITA", "ACEITO" -> "INDO_COLETA"
        "INDO_COLETA", "A_CAMINHO_COLETA" -> "CHEGUEI_COLETA"
        "CHEGUEI_COLETA", "NA_COLETA" -> "PEDIDO_RETIRADO"
        "PEDIDO_RETIRADO", "RETIRADO" -> "INDO_ENTREGA"
        "INDO_ENTREGA", "EM_ROTA", "SAIU_ENTREGA", "A_CAMINHO_CLIENTE" -> "ENTREGADOR_NO_LOCAL"
        "ENTREGADOR_NO_LOCAL" -> "FINALIZADA"
        else -> "INDO_COLETA"
    }
}
