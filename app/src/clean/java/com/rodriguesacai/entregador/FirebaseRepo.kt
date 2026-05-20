package com.rodriguesacai.entregador

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import java.util.Locale

class FirebaseRepo(private val context: Context) {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        try { FirebaseApp.initializeApp(context) } catch (_: Exception) { }
    }

    fun closeListeners() {
        listeners.forEach { try { it.remove() } catch (_: Exception) {} }
        listeners.clear()
    }

    fun ensureAuth(onReady: () -> Unit, onError: (String) -> Unit) {
        try {
            FirebaseApp.initializeApp(context)
            onReady()
        } catch (e: Exception) {
            onError("Firebase não inicializou: ${e.message ?: "sem detalhe"}")
        }
    }

    fun login(identifier: String, password: String, onResult: (DriverProfile?, String?) -> Unit) {
        val clean = identifier.trim()
        if (clean.isEmpty()) {
            onResult(null, "Informe CPF, telefone ou e-mail.")
            return
        }
        ensureAuth({ findDriverByIdentifier(clean, password, onResult) }, { err -> onResult(null, err) })
    }

    private fun findDriverByUid(uid: String, onResult: (DriverProfile?, String?) -> Unit) {
        if (uid.isBlank()) { onResult(null, "UID do Firebase não encontrado."); return }
        val collections = listOf("entregadores", "motoboys", "drivers")
        tryNextDriverQuery(collections, 0, "uid", uid, "", onResult)
    }

    private fun findDriverByIdentifier(identifier: String, password: String, onResult: (DriverProfile?, String?) -> Unit) {
        val normalized = identifier.filter { it.isDigit() }
        val value = if (normalized.length >= 8) normalized else identifier.trim()
        val fields = listOf("cpf", "telefone", "celular", "phone", "whatsapp", "email")
        tryDriverField(0, fields, value, password, onResult)
    }

    private fun tryDriverField(index: Int, fields: List<String>, value: String, password: String, onResult: (DriverProfile?, String?) -> Unit) {
        if (index >= fields.size) {
            onResult(null, "Nenhum entregador encontrado com esses dados.")
            return
        }
        val collections = listOf("entregadores", "motoboys", "drivers")
        tryNextDriverQuery(collections, 0, fields[index], value, password) { profile, error ->
            if (profile != null) onResult(profile, null) else tryDriverField(index + 1, fields, value, password, onResult)
        }
    }

    private fun tryNextDriverQuery(collections: List<String>, idx: Int, field: String, value: String, password: String, onResult: (DriverProfile?, String?) -> Unit) {
        if (idx >= collections.size) {
            onResult(null, "Cadastro não encontrado.")
            return
        }
        val col = collections[idx]
        db.collection(col).whereEqualTo(field, value).limit(1).get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull()
                if (doc == null) {
                    tryNextDriverQuery(collections, idx + 1, field, value, password, onResult)
                    return@addOnSuccessListener
                }
                val profile = doc.driverFrom(col)
                val stored = doc.str("senha", "senhaApp", "password", "senhaEntregador")
                val statusLower = profile.status.lowercase(Locale.ROOT)
                if (statusLower.contains("pendente") || statusLower.contains("analise") || statusLower.contains("análise")) {
                    onResult(profile.copy(approved = false), null)
                    return@addOnSuccessListener
                }
                if (statusLower.contains("bloque") || statusLower.contains("reprov")) {
                    onResult(null, "Cadastro bloqueado ou reprovado. Fale com a loja.")
                    return@addOnSuccessListener
                }
                if (stored.isNotBlank() && stored != password) {
                    onResult(null, "Senha incorreta.")
                    return@addOnSuccessListener
                }
                onResult(profile.copy(approved = profile.approved || statusLower.isBlank()), null)
            }
            .addOnFailureListener { error ->
                tryNextDriverQuery(collections, idx + 1, field, value, password, onResult)
            }
    }

    fun createSignup(name: String, phone: String, cpf: String, onDone: (Boolean, String) -> Unit) {
        ensureAuth({
            val data = hashMapOf<String, Any>(
                "nome" to name.trim(),
                "telefone" to phone.trim(),
                "cpf" to cpf.filter { it.isDigit() },
                "status" to "PENDENTE",
                "origem" to "APP_ENTREGADOR",
                "criadoEm" to FieldValue.serverTimestamp()
            )
            db.collection("entregadores_cadastros").add(data)
                .addOnSuccessListener { onDone(true, "Cadastro enviado para análise.") }
                .addOnFailureListener { onDone(false, "Não foi possível enviar cadastro: ${it.message ?: "erro"}") }
        }, { onDone(false, it) })
    }

    fun savePassword(profile: DriverProfile, password: String, onDone: (Boolean, String) -> Unit) {
        if (password.length < 4) { onDone(false, "Crie uma senha com pelo menos 4 dígitos."); return }
        db.collection(profile.collection).document(profile.id)
            .update(mapOf("senhaApp" to password, "senhaCriada" to true, "precisaCriarSenha" to false, "atualizadoEm" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onDone(true, "Senha criada com sucesso.") }
            .addOnFailureListener { onDone(false, "Falha ao salvar senha: ${it.message ?: "erro"}") }
    }

    fun listenProfile(profile: DriverProfile, onUpdate: (DriverProfile) -> Unit, onError: (String) -> Unit) {
        val reg = db.collection(profile.collection).document(profile.id).addSnapshotListener { doc, error ->
            if (error != null) { onError(error.message ?: "Erro ao ler perfil."); return@addSnapshotListener }
            if (doc != null && doc.exists()) onUpdate(doc.driverFrom(profile.collection))
        }
        listeners.add(reg)
    }

    fun setOnline(profile: DriverProfile, online: Boolean, onDone: (Boolean, String) -> Unit) {
        val status = if (online) "DISPONIVEL" else "INDISPONIVEL"
        db.collection(profile.collection).document(profile.id)
            .update(mapOf("online" to online, "disponivel" to online, "statusOperacional" to status, "localizacaoAtualizadaEm" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onDone(true, if (online) "Você está disponível." else "Você ficou indisponível.") }
            .addOnFailureListener { onDone(false, "Não foi possível mudar status: ${it.message ?: "erro"}") }
    }

    fun listenRides(profile: DriverProfile, onUpdate: (List<RideItem>) -> Unit, onError: (String) -> Unit) {
        val all = linkedMapOf<String, RideItem>()
        listOf("corridas", "pedidos", "rotas", "orders").forEach { col ->
            val reg = db.collection(col).limit(100).addSnapshotListener { snap, error ->
                if (error != null) { onError("$col: ${error.message ?: "erro"}"); return@addSnapshotListener }
                if (snap != null) {
                    snap.documents.map { it.rideFrom(col) }.forEach { ride ->
                        if (rideIsRelevant(ride, profile)) all["${ride.collection}/${ride.id}"] = ride else all.remove("${ride.collection}/${ride.id}")
                    }
                    onUpdate(all.values.sortedWith(compareByDescending<RideItem> { it.createdAt }.thenBy { it.orderNumber }))
                }
            }
            listeners.add(reg)
        }
    }

    private fun rideIsRelevant(ride: RideItem, profile: DriverProfile): Boolean {
        val status = ride.status.uppercase(Locale.ROOT)
        val driverKeys = listOf(profile.id, profile.uid).filter { it.isNotBlank() }
        val assigned = ride.assignedDriverId.isNotBlank() && driverKeys.any { it == ride.assignedDriverId }
        val available = ride.assignedDriverId.isBlank() && listOf("PENDENTE", "DISPONIVEL", "DISPONÍVEL", "OFERTA", "NOVA", "ABERTA", "AGUARDANDO_ENTREGADOR").any { status.contains(it) }
        val finishedForMe = assigned && listOf("FINAL", "ENTREGUE", "RECUS", "EXPIR", "OCORR").any { status.contains(it) }
        val activeForMe = assigned && !finishedForMe
        return available || activeForMe || finishedForMe
    }

    fun listenBanners(onUpdate: (List<BannerItem>) -> Unit, onError: (String) -> Unit) {
        val all = linkedMapOf<String, BannerItem>()
        listOf("carrosselApp", "appBanners", "banners").forEach { col ->
            val reg = db.collection(col).limit(20).addSnapshotListener { snap, error ->
                if (error != null) { onError("$col: ${error.message ?: "erro"}"); return@addSnapshotListener }
                snap?.documents?.map { it.bannerFrom() }?.forEach { banner ->
                    if (banner.active) all["$col/${banner.id}"] = banner else all.remove("$col/${banner.id}")
                }
                onUpdate(all.values.toList())
            }
            listeners.add(reg)
        }
    }

    fun listenNotices(profile: DriverProfile, onUpdate: (List<NoticeItem>) -> Unit, onError: (String) -> Unit) {
        val all = linkedMapOf<String, NoticeItem>()
        listOf("notificacoes", "avisos").forEach { col ->
            val reg = db.collection(col).limit(80).addSnapshotListener { snap, error ->
                if (error != null) { onError("$col: ${error.message ?: "erro"}"); return@addSnapshotListener }
                snap?.documents?.forEach { doc ->
                    val target = doc.str("entregadorId", "entregadorUid", "driverId", "motoboyId")
                    val public = doc.bool("todos", "publico", "geral", "all") || target.isBlank()
                    val mine = target == profile.id || target == profile.uid
                    val key = "$col/${doc.id}"
                    if (public || mine) all[key] = doc.noticeFrom() else all.remove(key)
                }
                onUpdate(all.values.sortedByDescending { it.createdAt })
            }
            listeners.add(reg)
        }
    }

    fun listenUpdateInfo(onUpdate: (AppUpdateInfo?) -> Unit) {
        val reg = db.collection("configuracoes").document("appEntregador").addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                onUpdate(AppUpdateInfo(
                    latestVersion = doc.str("latestVersion", "versaoAtual", "versionName"),
                    minVersion = doc.str("minVersion", "versaoMinima"),
                    message = doc.str("message", "mensagem", "descricao"),
                    url = doc.str("url", "apkUrl", "link"),
                    mandatory = doc.bool("mandatory", "obrigatoria", "forcarAtualizacao")
                ))
            } else onUpdate(null)
        }
        listeners.add(reg)
    }

    private fun rideDoc(ride: RideItem): DocumentReference = db.collection(ride.collection).document(ride.id)

    fun acceptRide(profile: DriverProfile, ride: RideItem, onDone: (Boolean, String) -> Unit) {
        val data = mapOf<String, Any>(
            "status" to "ACEITA",
            "entregadorId" to profile.id,
            "entregadorUid" to profile.uid,
            "entregadorNome" to profile.name,
            "aceitaEm" to FieldValue.serverTimestamp(),
            "atualizadoEm" to FieldValue.serverTimestamp()
        )
        rideDoc(ride).update(data)
            .addOnSuccessListener { onDone(true, "Corrida aceita.") }
            .addOnFailureListener { onDone(false, "Falha ao aceitar: ${it.message ?: "erro"}") }
    }

    fun rejectRide(profile: DriverProfile, ride: RideItem, reason: String, onDone: (Boolean, String) -> Unit) {
        rideDoc(ride).update(mapOf(
            "status" to "RECUSADA",
            "recusadaPor" to profile.id,
            "motivoRecusa" to reason,
            "recusadaEm" to FieldValue.serverTimestamp(),
            "atualizadoEm" to FieldValue.serverTimestamp()
        )).addOnSuccessListener { onDone(true, "Corrida recusada.") }
            .addOnFailureListener { onDone(false, "Falha ao recusar: ${it.message ?: "erro"}") }
    }

    fun advanceRide(ride: RideItem, onDone: (Boolean, String) -> Unit) {
        val newStatus = nextStatus(ride.status)
        rideDoc(ride).update(mapOf("status" to newStatus, "statusPedidoCore" to newStatus, "atualizadoEm" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onDone(true, "Status atualizado: ${humanStatus(newStatus)}") }
            .addOnFailureListener { onDone(false, "Falha ao atualizar: ${it.message ?: "erro"}") }
    }

    fun finishRide(ride: RideItem, onDone: (Boolean, String) -> Unit) {
        rideDoc(ride).update(mapOf("status" to "FINALIZADA", "statusPedidoCore" to "FINALIZADA", "finalizadaEm" to FieldValue.serverTimestamp(), "atualizadoEm" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onDone(true, "Entrega finalizada.") }
            .addOnFailureListener { onDone(false, "Falha ao finalizar: ${it.message ?: "erro"}") }
    }

    fun registerOccurrence(profile: DriverProfile, ride: RideItem, reason: String, onDone: (Boolean, String) -> Unit) {
        val data = mapOf<String, Any>(
            "entregadorId" to profile.id,
            "entregadorNome" to profile.name,
            "motivo" to reason,
            "statusAnterior" to ride.status,
            "criadoEm" to FieldValue.serverTimestamp()
        )
        val ref = rideDoc(ride)
        ref.collection("ocorrencias").add(data)
            .addOnSuccessListener {
                ref.update(mapOf("status" to "OCORRENCIA", "ocorrenciaAberta" to true, "atualizadoEm" to FieldValue.serverTimestamp()))
                onDone(true, "Ocorrência registrada.")
            }
            .addOnFailureListener { onDone(false, "Falha na ocorrência: ${it.message ?: "erro"}") }
    }

    fun requestProfileChange(profile: DriverProfile, type: String, value: String, onDone: (Boolean, String) -> Unit) {
        val data = mapOf<String, Any>(
            "entregadorId" to profile.id,
            "entregadorColecao" to profile.collection,
            "tipo" to type,
            "novoValor" to value,
            "status" to "PENDENTE",
            "criadoEm" to FieldValue.serverTimestamp()
        )
        db.collection("solicitacoes_entregador").add(data)
            .addOnSuccessListener { onDone(true, "Solicitação enviada para aprovação.") }
            .addOnFailureListener { onDone(false, "Falha ao enviar solicitação: ${it.message ?: "erro"}") }
    }

    fun savePayout(profile: DriverProfile, pix: String, bank: String, type: String, onDone: (Boolean, String) -> Unit) {
        db.collection(profile.collection).document(profile.id)
            .update(mapOf("pix" to pix, "chavePix" to pix, "banco" to bank, "tipoRepasse" to type, "dadosRecebimentoStatus" to "PENDENTE", "atualizadoEm" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onDone(true, "Dados de recebimento salvos. Status pendente de conferência.") }
            .addOnFailureListener { onDone(false, "Falha ao salvar recebimento: ${it.message ?: "erro"}") }
    }
}
