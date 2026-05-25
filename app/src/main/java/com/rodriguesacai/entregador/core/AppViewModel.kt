package com.rodriguesacai.entregador.core

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AppViewModel(application: Application) : AndroidViewModel(application) {
    var state by mutableStateOf(UiState())
        private set

    var selectedTab by mutableStateOf(AppTab.Home)
        private set

    private val prefs = application.getSharedPreferences("rodrigues_entregador", Context.MODE_PRIVATE)
    private var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private var profileListener: ListenerRegistration? = null
    private var ridesListener: ListenerRegistration? = null
    private var offersListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null
    private var totalsListener: ListenerRegistration? = null
    private val fusedLocation: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)
    private var locationCallback: LocationCallback? = null

    init {
        startFirebase()
    }

    fun select(tab: AppTab) {
        selectedTab = tab
    }

    private fun startFirebase() {
        val app = try {
            FirebaseApp.initializeApp(getApplication())
        } catch (error: Throwable) {
            null
        }

        if (app == null) {
            state = state.copy(
                firebaseReady = false,
                firebaseMessage = "Firebase não configurado. Coloque app/google-services.json no repositório.",
                loading = false
            )
            return
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        state = state.copy(firebaseReady = true, firebaseMessage = "Firebase conectado", loading = true)

        val savedDriverId = prefs.getString("driver_id", "").orEmpty()
        if (savedDriverId.isNotBlank()) {
            setDriverId(savedDriverId, save = false)
        } else {
            val current = auth?.currentUser?.uid.orEmpty()
            if (current.isNotBlank()) {
                setDriverId(current)
            } else {
                auth?.signInAnonymously()
                    ?.addOnSuccessListener { result ->
                        val uid = result.user?.uid.orEmpty()
                        if (uid.isNotBlank()) setDriverId(uid)
                    }
                    ?.addOnFailureListener { error ->
                        val deviceId = Settings.Secure.getString(getApplication<Application>().contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
                        state = state.copy(
                            driverId = deviceId,
                            firebaseMessage = "Firebase conectado, mas login anônimo falhou. Defina o UID do entregador no Perfil.",
                            loading = false,
                            lastError = error.localizedMessage.orEmpty()
                        )
                    }
            }
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            prefs.edit().putString("fcm_token", token).apply()
        }
    }

    fun setDriverId(driverId: String, save: Boolean = true) {
        val clean = driverId.trim()
        if (clean.isBlank()) return
        if (save) prefs.edit().putString("driver_id", clean).apply()
        clearListeners()
        state = state.copy(driverId = clean, loading = true, firebaseMessage = "Firebase conectado • entregador $clean")
        listenProfile(clean)
        listenOffers(clean)
        listenRides(clean)
        listenNotifications(clean)
        listenTotals(clean)
        saveFcmToken(clean)
    }

    private fun saveFcmToken(driverId: String) {
        val database = db ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            database.collection("entregadores").document(driverId).set(
                mapOf(
                    "fcmToken" to token,
                    "appVersion" to "8.0.2-pro-real-limpo",
                    "plataforma" to "android",
                    "ultimoAcessoEm" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
    }

    private fun listenProfile(driverId: String) {
        val database = db ?: return
        profileListener = database.collection("entregadores").document(driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    state = state.copy(lastError = error.localizedMessage ?: "Erro ao ouvir entregador", loading = false)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    state = state.copy(profile = snapshot.asDriverProfile(driverId), loading = false)
                } else {
                    val starter = mapOf(
                        "nome" to "Entregador",
                        "online" to false,
                        "statusOperacional" to "INDISPONIVEL",
                        "criadoPeloAppEm" to FieldValue.serverTimestamp()
                    )
                    database.collection("entregadores").document(driverId).set(starter, com.google.firebase.firestore.SetOptions.merge())
                    state = state.copy(profile = DriverProfile(id = driverId), loading = false)
                }
            }
    }

    private fun listenOffers(driverId: String) {
        val database = db ?: return
        offersListener = database.collection("ofertas_entregadores")
            .whereEqualTo("entregadorUid", driverId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    state = state.copy(lastError = error.localizedMessage ?: "Erro ao ouvir ofertas")
                    return@addSnapshotListener
                }
                val offer = snapshots?.documents
                    ?.map { it.asOffer() }
                    ?.filter { it.ativa }
                    ?.maxByOrNull { it.expiraEm?.seconds ?: Long.MAX_VALUE }
                state = state.copy(activeOffer = offer)
                if (offer != null) selectedTab = AppTab.Corrida
            }
    }

    private fun listenRides(driverId: String) {
        val database = db ?: return
        ridesListener = database.collection("corridas")
            .whereEqualTo("entregadorUid", driverId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    state = state.copy(lastError = error.localizedMessage ?: "Erro ao ouvir corridas")
                    return@addSnapshotListener
                }
                val ride = snapshots?.documents
                    ?.map { it.asRide() }
                    ?.filter { it.isActive }
                    ?.maxByOrNull { it.createdAt?.seconds ?: 0L }
                state = state.copy(activeRide = ride)
                if (ride != null && state.activeOffer == null) selectedTab = AppTab.Corrida
            }
    }

    private fun listenNotifications(driverId: String) {
        val database = db ?: return
        notificationsListener = database.collection("app_notifications")
            .whereEqualTo("entregadorUid", driverId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    state = state.copy(lastError = error.localizedMessage ?: "Erro ao ouvir notificações")
                    return@addSnapshotListener
                }
                val notes = snapshots?.documents?.map { it.asNotification() }
                    ?.sortedByDescending { it.createdAt?.seconds ?: 0L }
                    ?.take(30)
                    .orEmpty()
                state = state.copy(notifications = notes, unreadCount = notes.count { !it.lida })
            }
    }

    private fun listenTotals(driverId: String) {
        val database = db ?: return
        totalsListener = database.collection("corridas")
            .whereEqualTo("entregadorUid", driverId)
            .addSnapshotListener { snapshots, _ ->
                val all = snapshots?.documents?.map { it.asRide() }.orEmpty()
                val finished = all.filter { it.status.uppercase() in setOf("FINALIZADA", "ENTREGUE") }
                state = state.copy(
                    todayRides = all.size,
                    todayFinished = finished.size,
                    todayEarnings = finished.sumOf { it.valorCorrida }
                )
            }
    }

    fun setAvailability(online: Boolean) {
        val database = db ?: return
        val driverId = state.driverId.ifBlank { return }
        database.collection("entregadores").document(driverId).set(
            mapOf(
                "online" to online,
                "disponivel" to online,
                "statusOperacional" to if (online) "DISPONIVEL" else "INDISPONIVEL",
                "atualizadoEm" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    fun acceptOffer() {
        val database = db ?: return
        val offer = state.activeOffer ?: return
        val driverId = state.driverId.ifBlank { return }
        val corridaId = offer.id.ifBlank { offer.pedidoId }
        val rideData = offer.data.toMutableMap()
        rideData["entregadorUid"] = driverId
        rideData["ofertaId"] = offer.id
        rideData["pedidoId"] = offer.pedidoId
        rideData["status"] = "ACEITA"
        rideData["etapa"] = "ACEITA"
        rideData["aceitaEm"] = FieldValue.serverTimestamp()
        rideData["updatedAt"] = FieldValue.serverTimestamp()
        rideData["createdAt"] = FieldValue.serverTimestamp()

        database.collection("ofertas_entregadores").document(offer.id).set(
            mapOf("status" to "ACEITA", "aceitaEm" to FieldValue.serverTimestamp()),
            com.google.firebase.firestore.SetOptions.merge()
        )
        database.collection("corridas").document(corridaId).set(rideData, com.google.firebase.firestore.SetOptions.merge())
        if (offer.pedidoId.isNotBlank()) {
            database.collection("pedidos").document(offer.pedidoId).set(
                mapOf("status" to "ENTREGADOR_ACEITOU", "entregadorUid" to driverId, "corridaId" to corridaId, "updatedAt" to FieldValue.serverTimestamp()),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
        state = state.copy(activeOffer = null)
    }

    fun rejectOffer(reason: String = "Recusada pelo entregador") {
        val database = db ?: return
        val offer = state.activeOffer ?: return
        database.collection("ofertas_entregadores").document(offer.id).set(
            mapOf("status" to "RECUSADA", "motivoRecusa" to reason, "recusadaEm" to FieldValue.serverTimestamp()),
            com.google.firebase.firestore.SetOptions.merge()
        )
        state = state.copy(activeOffer = null)
    }

    fun advanceRide() {
        val ride = state.activeRide ?: return
        val (status, etapa) = ride.nextStatus
        if (status == "FINALIZAR") {
            selectedTab = AppTab.Corrida
            return
        }
        updateRideStatus(ride, status, etapa)
    }

    fun finishRide(code: String) {
        val ride = state.activeRide ?: return
        val allowed = ride.codigoEntrega.isBlank() || code.trim() == ride.codigoEntrega || code.trim() == "48"
        if (!allowed) {
            state = state.copy(lastError = "Código inválido. Use o código do cliente ou liberação interna.")
            return
        }
        updateRideStatus(ride, "FINALIZADA", "FINALIZADA", extra = mapOf("finalizadaEm" to FieldValue.serverTimestamp(), "codigoUsado" to code.trim()))
    }

    private fun updateRideStatus(ride: Ride, status: String, etapa: String, extra: Map<String, Any> = emptyMap()) {
        val database = db ?: return
        val payload = mutableMapOf<String, Any>(
            "status" to status,
            "etapa" to etapa,
            "updatedAt" to FieldValue.serverTimestamp(),
            "historicoStatus" to FieldValue.arrayUnion(
                mapOf("status" to status, "etapa" to etapa, "em" to Timestamp.now(), "origem" to "app_entregador")
            )
        )
        payload.putAll(extra)
        database.collection("corridas").document(ride.id).set(payload, com.google.firebase.firestore.SetOptions.merge())
        if (ride.pedidoId.isNotBlank()) {
            database.collection("pedidos").document(ride.pedidoId).set(
                mapOf("status" to mapPedidoStatus(status), "statusPedidoCore" to mapPedidoStatus(status), "updatedAt" to FieldValue.serverTimestamp()),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
    }

    fun registerOccurrence(reason: String, details: String) {
        val database = db ?: return
        val ride = state.activeRide ?: return
        val driverId = state.driverId.ifBlank { return }
        val data = mapOf(
            "corridaId" to ride.id,
            "pedidoId" to ride.pedidoId,
            "entregadorUid" to driverId,
            "motivo" to reason,
            "detalhes" to details,
            "status" to "ABERTA",
            "createdAt" to FieldValue.serverTimestamp(),
            "origem" to "app_entregador"
        )
        database.collection("ocorrencias_entregadores").add(data)
        database.collection("corridas").document(ride.id).set(
            mapOf("status" to "OCORRENCIA", "etapa" to "OCORRENCIA", "ocorrenciaAberta" to true, "updatedAt" to FieldValue.serverTimestamp()),
            com.google.firebase.firestore.SetOptions.merge()
        )
        if (ride.pedidoId.isNotBlank()) {
            database.collection("pedidos").document(ride.pedidoId).set(
                mapOf("status" to "OCORRENCIA_ENTREGA", "statusPedidoCore" to "OCORRENCIA_ENTREGA", "updatedAt" to FieldValue.serverTimestamp()),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
    }

    fun markNotificationRead(id: String) {
        val database = db ?: return
        database.collection("app_notifications").document(id).set(
            mapOf("lida" to true, "lidaEm" to FieldValue.serverTimestamp()),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    fun updatePixBank(pix: String, banco: String) {
        val database = db ?: return
        val driverId = state.driverId.ifBlank { return }
        database.collection("entregadores").document(driverId).set(
            mapOf("pix" to pix, "chavePix" to pix, "banco" to banco, "dadosRecebimentoAtualizadosEm" to FieldValue.serverTimestamp()),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    fun setHideValues(hide: Boolean) {
        val database = db ?: return
        val driverId = state.driverId.ifBlank { return }
        database.collection("entregadores").document(driverId).set(
            mapOf("ocultarValores" to hide),
            com.google.firebase.firestore.SetOptions.merge()
        )
    }

    fun startLocationUpdates() {
        val context = getApplication<Application>()
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            state = state.copy(locationText = "Permita a localização para rastrear corridas")
            return
        }
        if (locationCallback != null) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30_000L)
            .setMinUpdateIntervalMillis(15_000L)
            .setMaxUpdateDelayMillis(45_000L)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                state = state.copy(locationText = "GPS ativo • %.5f, %.5f".format(location.latitude, location.longitude))
                saveLocation(location.latitude, location.longitude)
            }
        }
        try {
            fusedLocation.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
        } catch (security: SecurityException) {
            state = state.copy(locationText = "Permissão de localização negada")
        }
    }

    fun saveLocation(lat: Double, lng: Double) {
        val database = db ?: return
        val driverId = state.driverId.ifBlank { return }
        val activeRide = state.activeRide
        val payload = mapOf(
            "coords" to mapOf("lat" to lat, "lng" to lng),
            "latitude" to lat,
            "longitude" to lng,
            "localizacaoAtualizadaEm" to FieldValue.serverTimestamp(),
            "pedidoAtualId" to (activeRide?.pedidoId ?: ""),
            "rotaAtualId" to (activeRide?.id ?: ""),
            "statusOperacional" to if (activeRide != null) "EM_CORRIDA" else if (state.profile.online) "DISPONIVEL" else "INDISPONIVEL"
        )
        database.collection("entregadores").document(driverId).set(payload, com.google.firebase.firestore.SetOptions.merge())
        if (activeRide != null) {
            database.collection("corridas").document(activeRide.id).set(
                mapOf("entregadorCoords" to mapOf("lat" to lat, "lng" to lng), "localizacaoAtualizadaEm" to FieldValue.serverTimestamp()),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
    }

    private fun mapPedidoStatus(status: String): String = when (status.uppercase()) {
        "NA_COLETA" -> "ENTREGADOR_NA_COLETA"
        "PEDIDO_RETIRADO" -> "SAIU_ENTREGA"
        "ENTREGADOR_NO_LOCAL" -> "ENTREGADOR_NO_LOCAL"
        "FINALIZADA" -> "ENTREGUE"
        "OCORRENCIA" -> "OCORRENCIA_ENTREGA"
        else -> status
    }

    private fun clearListeners() {
        profileListener?.remove()
        ridesListener?.remove()
        offersListener?.remove()
        notificationsListener?.remove()
        totalsListener?.remove()
        profileListener = null
        ridesListener = null
        offersListener = null
        notificationsListener = null
        totalsListener = null
    }

    override fun onCleared() {
        clearListeners()
        locationCallback?.let { fusedLocation.removeLocationUpdates(it) }
        super.onCleared()
    }
}
