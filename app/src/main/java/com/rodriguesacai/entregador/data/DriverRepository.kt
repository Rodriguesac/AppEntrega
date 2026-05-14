package com.rodriguesacai.entregador.data

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DriverRepository {
    private const val PREFS = "driver_session"
    private const val KEY_ID = "driver_id"
    private const val KEY_NAME = "driver_name"
    private const val KEY_PHONE = "driver_phone"
    private const val KEY_PHOTO = "driver_photo"

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun currentSession(context: Context): DriverProfile? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_ID, null)?.takeIf { it.isNotBlank() } ?: return null
        return DriverProfile(
            id = id,
            name = prefs.getString(KEY_NAME, null).orEmpty().ifBlank { "Entregador" },
            phone = prefs.getString(KEY_PHONE, null).orEmpty(),
            photoUrl = prefs.getString(KEY_PHOTO, null).orEmpty(),
            verified = true
        )
    }

    fun logout(context: Context, onDone: () -> Unit = {}) {
        val session = currentSession(context)
        if (session != null) {
            db.collection("drivers").document(session.id).set(
                mapOf(
                    "online" to false,
                    "status" to "offline",
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        onDone()
    }

    fun login(
        context: Context,
        documentOrPhone: String,
        onSuccess: (DriverProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val raw = documentOrPhone.trim()
        val normalized = raw.onlyDigits()
        if (normalized.length < 4) {
            onError("Informe o CPF ou telefone cadastrado do entregador.")
            return
        }

        // 1) tenta documento direto: drivers/{cpfOuTelefone}
        db.collection("drivers").document(normalized).get()
            .addOnSuccessListener { directDoc ->
                if (directDoc.exists()) {
                    val profile = directDoc.toProfile(normalized)
                    if (profile == null) onError("Cadastro do entregador incompleto no Firebase.") else saveSession(context, profile, onSuccess)
                } else {
                    queryByFields(context, normalized, onSuccess, onError)
                }
            }
            .addOnFailureListener { queryByFields(context, normalized, onSuccess, onError) }
    }

    private fun queryByFields(
        context: Context,
        normalized: String,
        onSuccess: (DriverProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("drivers").whereEqualTo("cpf", normalized).limit(1).get()
            .addOnSuccessListener { cpfSnap ->
                val cpfDoc = cpfSnap.documents.firstOrNull()
                if (cpfDoc != null) {
                    val profile = cpfDoc.toProfile(cpfDoc.id)
                    if (profile == null) onError("Cadastro do entregador incompleto no Firebase.") else saveSession(context, profile, onSuccess)
                } else {
                    db.collection("drivers").whereEqualTo("phone", normalized).limit(1).get()
                        .addOnSuccessListener { phoneSnap ->
                            val phoneDoc = phoneSnap.documents.firstOrNull()
                            val profile = phoneDoc?.toProfile(phoneDoc.id)
                            if (profile == null) {
                                onError("Entregador não encontrado. Cadastre/aprove esse entregador no painel gestor.")
                            } else {
                                saveSession(context, profile, onSuccess)
                            }
                        }
                        .addOnFailureListener { onError(it.message ?: "Falha ao consultar telefone no Firebase.") }
                }
            }
            .addOnFailureListener { onError(it.message ?: "Falha ao consultar CPF no Firebase.") }
    }

    private fun saveSession(context: Context, profile: DriverProfile, onSuccess: (DriverProfile) -> Unit) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ID, profile.id)
            .putString(KEY_NAME, profile.name)
            .putString(KEY_PHONE, profile.phone)
            .putString(KEY_PHOTO, profile.photoUrl)
            .apply()

        db.collection("drivers").document(profile.id).set(
            mapOf(
                "lastLoginAt" to Timestamp.now(),
                "platform" to "android_native",
                "appVersion" to "3.0.0-nativo"
            ),
            SetOptions.merge()
        )
        saveMessagingToken(context)
        onSuccess(profile)
    }

    fun setOnline(context: Context, online: Boolean) {
        val profile = currentSession(context) ?: return
        val payload = linkedMapOf<String, Any?>(
            "id" to profile.id,
            "name" to profile.name,
            "phone" to profile.phone,
            "online" to online,
            "status" to if (online) "available" else "offline",
            "updatedAt" to Timestamp.now(),
            "platform" to "android_native",
            "appVersion" to "3.0.0-nativo"
        )
        db.collection("drivers").document(profile.id).set(payload, SetOptions.merge())
        if (online) saveMessagingToken(context)
    }

    fun saveMessagingToken(context: Context) {
        val profile = currentSession(context) ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            db.collection("drivers").document(profile.id).set(
                mapOf(
                    "fcmToken" to token,
                    "tokenUpdatedAt" to Timestamp.now(),
                    "platform" to "android_native"
                ),
                SetOptions.merge()
            )
        }
    }

    fun listenPendingRide(
        context: Context,
        onRide: (DriverRide?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val profile = currentSession(context) ?: return null
        return db.collection("rides")
            .whereEqualTo("status", "pending")
            .limit(30)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir novas corridas.")
                    return@addSnapshotListener
                }
                val ride = snap?.documents.orEmpty()
                    .firstNotNullOfOrNull { doc ->
                        val rejected = (doc.get("rejectedDriverIds") as? List<*>)?.map { it.toString() }.orEmpty()
                        val expired = (doc.get("expiredDriverIds") as? List<*>)?.map { it.toString() }.orEmpty()
                        val candidate = doc.toDriverRide()
                        val canReceive = candidate != null &&
                            (candidate.assignedDriverId.isBlank() || candidate.assignedDriverId == profile.id || candidate.targetDriverId == profile.id || candidate.broadcast) &&
                            !rejected.contains(profile.id) &&
                            !expired.contains(profile.id)
                        if (canReceive) candidate else null
                    }
                onRide(ride)
            }
    }

    fun listenMyActiveRide(
        context: Context,
        onRide: (DriverRide?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val profile = currentSession(context) ?: return null
        return db.collection("rides")
            .whereEqualTo("driverId", profile.id)
            .limit(20)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir corrida ativa.")
                    return@addSnapshotListener
                }
                val active = snap?.documents.orEmpty()
                    .mapNotNull { it.toDriverRide() }
                    .firstOrNull { it.status in listOf("accepted", "pickup", "delivering") }
                onRide(active)
            }
    }

    fun listenMyHistory(
        context: Context,
        onHistory: (List<DriverHistory>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val profile = currentSession(context) ?: return null
        return db.collection("driverHistory")
            .whereEqualTo("driverId", profile.id)
            .limit(40)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir histórico.")
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty().map { doc ->
                    DriverHistory(
                        id = doc.id,
                        rideId = doc.getString("rideId").orEmpty(),
                        action = doc.getString("action") ?: "registro",
                        value = doc.getString("value") ?: formatCurrency(doc.getDouble("valueNumber") ?: 0.0),
                        createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                        createdLabel = doc.getTimestamp("createdAt")?.toDate()?.formatHour().orEmpty().ifBlank { "agora" }
                    )
                }.sortedByDescending { it.createdAtMillis }
                onHistory(list)
            }
    }

    fun listenDailyStats(
        context: Context,
        onStats: (DriverStats) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val profile = currentSession(context) ?: return null
        return db.collection("driverHistory")
            .whereEqualTo("driverId", profile.id)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onError(err.message ?: "Erro ao ouvir ganhos.")
                    return@addSnapshotListener
                }
                val finished = snap?.documents.orEmpty().filter { it.getString("action") == "finished" }
                val total = finished.sumOf { doc -> valueNumberFromDoc(doc) }
                onStats(DriverStats(totalToday = total, finishedCount = finished.size, score = 100))
            }
    }

    fun acceptRide(context: Context, rideId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login antes de aceitar corrida.")
            return
        }
        db.collection("rides").document(rideId).get()
            .addOnSuccessListener { doc ->
                val ride = doc.toDriverRide()
                val update = mapOf(
                    "status" to "accepted",
                    "driverId" to profile.id,
                    "driverName" to profile.name,
                    "assignedDriverId" to profile.id,
                    "acceptedAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                )
                db.collection("rides").document(rideId).set(update, SetOptions.merge())
                    .addOnSuccessListener {
                        addHistory(profile, rideId, "accepted", ride?.value.orEmpty(), ride?.valueNumber ?: 0.0)
                        onDone()
                    }
                    .addOnFailureListener { onError(it.message ?: "Falha ao aceitar corrida.") }
            }
            .addOnFailureListener { onError(it.message ?: "Falha ao abrir corrida.") }
    }

    fun rejectRide(context: Context, rideId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login antes de rejeitar corrida.")
            return
        }
        db.collection("rides").document(rideId).get()
            .addOnSuccessListener { doc ->
                val ride = doc.toDriverRide()
                db.collection("rides").document(rideId).set(
                    mapOf(
                        "lastRejectedBy" to profile.id,
                        "rejectedDriverIds" to FieldValue.arrayUnion(profile.id),
                        "updatedAt" to Timestamp.now()
                    ),
                    SetOptions.merge()
                ).addOnSuccessListener {
                    db.collection("rides").document(rideId).collection("rejections").document(profile.id).set(
                        mapOf("driverId" to profile.id, "driverName" to profile.name, "reason" to "rejected_by_driver", "createdAt" to Timestamp.now()),
                        SetOptions.merge()
                    )
                    addHistory(profile, rideId, "rejected", ride?.value.orEmpty(), ride?.valueNumber ?: 0.0)
                    onDone()
                }.addOnFailureListener { onError(it.message ?: "Falha ao rejeitar corrida.") }
            }
            .addOnFailureListener { onError(it.message ?: "Falha ao abrir corrida.") }
    }

    fun expireRide(context: Context, rideId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context) ?: return
        db.collection("rides").document(rideId).set(
            mapOf(
                "lastExpiredFor" to profile.id,
                "expiredDriverIds" to FieldValue.arrayUnion(profile.id),
                "updatedAt" to Timestamp.now()
            ),
            SetOptions.merge()
        ).addOnSuccessListener {
            addHistory(profile, rideId, "expired", "", 0.0)
            onDone()
        }.addOnFailureListener { onError(it.message ?: "Falha ao expirar oferta.") }
    }

    fun updateRideStatus(context: Context, rideId: String, status: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val profile = currentSession(context)
        if (profile == null) {
            onError("Faça login para atualizar a corrida.")
            return
        }
        db.collection("rides").document(rideId).get()
            .addOnSuccessListener { doc ->
                val ride = doc.toDriverRide()
                val fields = mutableMapOf<String, Any>("status" to status, "updatedAt" to Timestamp.now())
                when (status) {
                    "pickup" -> fields["pickupStartedAt"] = Timestamp.now()
                    "delivering" -> fields["deliveryStartedAt"] = Timestamp.now()
                    "finished" -> fields["finishedAt"] = Timestamp.now()
                }
                db.collection("rides").document(rideId).set(fields, SetOptions.merge())
                    .addOnSuccessListener {
                        addHistory(profile, rideId, status, ride?.value.orEmpty(), ride?.valueNumber ?: 0.0)
                        if (status == "finished") setOnline(context, true)
                        onDone()
                    }
                    .addOnFailureListener { onError(it.message ?: "Falha ao atualizar corrida.") }
            }
            .addOnFailureListener { onError(it.message ?: "Falha ao abrir corrida.") }
    }

    private fun addHistory(profile: DriverProfile, rideId: String, action: String, value: String, valueNumber: Double) {
        db.collection("driverHistory").add(
            mapOf(
                "driverId" to profile.id,
                "driverName" to profile.name,
                "rideId" to rideId,
                "action" to action,
                "value" to value,
                "valueNumber" to valueNumber,
                "createdAt" to Timestamp.now(),
                "platform" to "android_native"
            )
        )
    }

    private fun DocumentSnapshot.toProfile(fallbackId: String): DriverProfile? {
        val name = getString("name") ?: getString("driverName") ?: getString("nome") ?: "Entregador"
        val blocked = getBoolean("blocked") ?: false
        val approved = getBoolean("approved") ?: true
        if (blocked || !approved) return null
        return DriverProfile(
            id = getString("id") ?: fallbackId,
            name = name,
            phone = getString("phone") ?: getString("telefone") ?: "",
            photoUrl = getString("photoUrl") ?: getString("avatar") ?: "",
            verified = getBoolean("verified") ?: true
        )
    }

    private fun valueNumberFromDoc(doc: DocumentSnapshot): Double {
        return doc.getDouble("valueNumber")
            ?: doc.getString("value")?.toMoneyDouble()
            ?: 0.0
    }

    private fun String.onlyDigits(): String = filter { it.isDigit() }

    private fun String.toMoneyDouble(): Double? {
        return replace("R$", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
            .toDoubleOrNull()
    }

    fun formatCurrency(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)

    private fun Date.formatHour(): String = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(this)
}

data class DriverProfile(
    val id: String,
    val name: String,
    val phone: String = "",
    val photoUrl: String = "",
    val verified: Boolean = true
)

data class DriverStats(
    val totalToday: Double = 0.0,
    val finishedCount: Int = 0,
    val score: Int = 100
)

data class DriverHistory(
    val id: String,
    val rideId: String,
    val action: String,
    val value: String,
    val createdAtMillis: Long,
    val createdLabel: String
)

data class DriverRide(
    val id: String,
    val status: String,
    val value: String,
    val valueNumber: Double,
    val distance: String,
    val duration: String,
    val pickup: String,
    val dropoff: String,
    val assignedDriverId: String,
    val targetDriverId: String,
    val broadcast: Boolean,
    val customerName: String,
    val orderCode: String,
    val stops: Int
)

private fun DocumentSnapshot.toDriverRide(): DriverRide? {
    val number = getDouble("valueNumber")
        ?: getString("value")?.replace("R$", "")?.replace(".", "")?.replace(",", ".")?.trim()?.toDoubleOrNull()
        ?: 0.0
    return DriverRide(
        id = id,
        status = getString("status") ?: "pending",
        value = getString("value") ?: DriverRepository.formatCurrency(number),
        valueNumber = number,
        distance = getString("distance") ?: "-- km",
        duration = getString("duration") ?: getString("estimatedTime") ?: "-- min",
        pickup = getString("pickup") ?: getString("pickupAddress") ?: "Rodrigues Açaí e Cia",
        dropoff = getString("dropoff") ?: getString("dropoffAddress") ?: "Endereço do cliente liberado após aceite",
        assignedDriverId = getString("assignedDriverId") ?: "",
        targetDriverId = getString("targetDriverId") ?: "",
        broadcast = getBoolean("broadcast") ?: false,
        customerName = getString("customerName") ?: getString("clientName") ?: "Cliente",
        orderCode = getString("orderCode") ?: getString("orderId") ?: id.takeLast(6).uppercase(),
        stops = (getLong("stops") ?: getLong("paradas") ?: 2L).toInt()
    )
}
