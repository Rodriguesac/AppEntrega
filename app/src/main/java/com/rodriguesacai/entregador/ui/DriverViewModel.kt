package com.rodriguesacai.entregador.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.data.DriverNotification
import com.rodriguesacai.entregador.data.FirebaseRepository
import com.rodriguesacai.entregador.data.Ride
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DriverUiState(
    val loading: Boolean = false,
    val logged: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val driver: Driver? = null,
    val activeRides: List<Ride> = emptyList(),
    val history: List<Ride> = emptyList(),
    val notifications: List<DriverNotification> = emptyList()
)

class DriverViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FirebaseRepository(app)
    private val _state = MutableStateFlow(DriverUiState(logged = repo.hasSession))
    val state: StateFlow<DriverUiState> = _state.asStateFlow()

    private var driverReg: ListenerRegistration? = null
    private var activeReg: ListenerRegistration? = null
    private var historyReg: ListenerRegistration? = null
    private var notifReg: ListenerRegistration? = null

    init {
        if (repo.hasSession) startListeners()
    }

    fun login(identifier: String, password: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null, message = null)
        val result = repo.login(identifier, password)
        result.onSuccess {
            _state.value = _state.value.copy(loading = false, logged = true)
            startListeners()
            syncFcmToken()
        }.onFailure {
            _state.value = _state.value.copy(loading = false, error = it.message ?: "Falha no login")
        }
    }

    fun submitRegistration(nome: String, cpf: String, telefone: String, placa: String, documentoUri: String?, selfieUri: String?) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null, message = null)
        runCatching { repo.submitRegistration(nome, cpf, telefone, placa, documentoUri, selfieUri) }
            .onSuccess { _state.value = _state.value.copy(loading = false, message = "Cadastro enviado para análise.") }
            .onFailure { setError(it) }
    }

    fun createPassword(identifier: String, password: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null, message = null)
        runCatching { repo.createPassword(identifier, password) }
            .onSuccess { _state.value = _state.value.copy(loading = false, message = "Senha criada. Faça login.") }
            .onFailure { setError(it) }
    }

    fun logout() {
        repo.logout()
        clearListeners()
        _state.value = DriverUiState(logged = false)
    }

    private fun startListeners() {
        clearListeners()
        driverReg = repo.listenDriver(
            onData = { _state.value = _state.value.copy(driver = it, error = null) },
            onError = { _state.value = _state.value.copy(error = it.message) }
        )
        activeReg = repo.listenActiveRides(
            onData = { _state.value = _state.value.copy(activeRides = it, error = null) },
            onError = { _state.value = _state.value.copy(error = it.message) }
        )
        historyReg = repo.listenHistory(
            onData = { _state.value = _state.value.copy(history = it, error = null) },
            onError = { _state.value = _state.value.copy(error = it.message) }
        )
        notifReg = repo.listenNotifications(
            onData = { _state.value = _state.value.copy(notifications = it, error = null) },
            onError = { _state.value = _state.value.copy(error = it.message) }
        )
    }

    private fun clearListeners() {
        driverReg?.remove(); activeReg?.remove(); historyReg?.remove(); notifReg?.remove()
    }

    fun setOnline(online: Boolean) = viewModelScope.launch { runCatching { repo.setOnline(online) }.onFailure { setError(it) } }
    fun acceptRide(rideId: String) = viewModelScope.launch { runCatching { repo.acceptRide(rideId) }.onFailure { setError(it) } }
    fun rejectRide(rideId: String, motivo: String) = viewModelScope.launch { runCatching { repo.rejectRide(rideId, motivo) }.onFailure { setError(it) } }
    fun advanceRide(ride: Ride) = viewModelScope.launch { runCatching { repo.advanceRide(ride) }.onFailure { setError(it) } }
    fun savePix(chave: String, tipo: String, banco: String) = viewModelScope.launch { runCatching { repo.savePixBank(chave, tipo, banco) }.onFailure { setError(it) } }
    fun toggleValues(hidden: Boolean) = viewModelScope.launch { runCatching { repo.toggleValues(hidden) }.onFailure { setError(it) } }
    fun requestChange(tipo: String, novoValor: String, obs: String) = viewModelScope.launch { runCatching { repo.requestChange(tipo, novoValor, obs) }.onFailure { setError(it) } }
    fun createOccurrence(rideId: String, motivo: String, detalhe: String) = viewModelScope.launch { runCatching { repo.createOccurrence(rideId, motivo, detalhe) }.onFailure { setError(it) } }

    private fun syncFcmToken() = viewModelScope.launch {
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            repo.saveFcmToken(token)
        }
    }

    private fun setError(t: Throwable) {
        _state.value = _state.value.copy(loading = false, error = t.message ?: "Erro inesperado")
    }

    override fun onCleared() {
        clearListeners()
        super.onCleared()
    }
}
