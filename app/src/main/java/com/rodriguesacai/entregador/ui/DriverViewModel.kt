package com.rodriguesacai.entregador.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.rodriguesacai.entregador.data.DriverStep
import com.rodriguesacai.entregador.data.DriverUiState
import com.rodriguesacai.entregador.data.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DriverViewModel(private val repo: FirebaseRepository = FirebaseRepository()) : ViewModel() {
    private val _state = MutableStateFlow(DriverUiState())
    val state: StateFlow<DriverUiState> = _state
    private var listener: ListenerRegistration? = null
    init { listenOffers() }

    private fun listenOffers() {
        listener?.remove()
        listener = repo.listenPendingOffers(onOffer = { offer ->
            val current = _state.value
            if (offer != null && current.activeRide == null) {
                _state.value = current.copy(pendingOffer = offer, step = DriverStep.EM_OFERTA, error = null)
                viewModelScope.launch { runCatching { repo.markVisualized(offer) } }
            }
            if (offer == null && current.activeRide == null && current.pendingOffer != null) {
                _state.value = current.copy(pendingOffer = null, step = if (current.isAvailable) DriverStep.DISPONIVEL else DriverStep.INDISPONIVEL)
            }
        }, onError = { _state.value = _state.value.copy(error = it.message) })
    }

    fun toggleTheme() { _state.value = _state.value.copy(isDark = !_state.value.isDark) }
    fun toggleEarnings() { _state.value = _state.value.copy(earningsVisible = !_state.value.earningsVisible) }
    fun setTab(tab: String) { _state.value = _state.value.copy(activeTab = tab) }

    fun setAvailable(value: Boolean) = viewModelScope.launch {
        runCatching { repo.setDriverAvailable(value) }.onSuccess {
            _state.value = _state.value.copy(isAvailable = value, step = if (value) DriverStep.DISPONIVEL else DriverStep.INDISPONIVEL, activeTab = "home", error = null)
        }.onFailure { _state.value = _state.value.copy(error = it.message) }
    }

    fun acceptOffer() {
        val offer = _state.value.pendingOffer ?: return
        viewModelScope.launch {
            runCatching { repo.acceptOffer(offer) }.onSuccess {
                _state.value = _state.value.copy(pendingOffer = null, activeRide = offer, step = DriverStep.INDO_COLETA, activeTab = "route", isAvailable = false, error = null)
            }.onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun rejectOffer(motivo: String = "Rejeitado pelo entregador") {
        val offer = _state.value.pendingOffer ?: return
        viewModelScope.launch {
            runCatching { repo.rejectOffer(offer, motivo) }.onSuccess {
                _state.value = _state.value.copy(pendingOffer = null, step = if (_state.value.isAvailable) DriverStep.DISPONIVEL else DriverStep.INDISPONIVEL, activeTab = "home", error = null)
            }.onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun nextStep() {
        val ride = _state.value.activeRide ?: return
        val next = when (_state.value.step) {
            DriverStep.INDO_COLETA -> DriverStep.CHEGUEI_COLETA
            DriverStep.CHEGUEI_COLETA -> DriverStep.PEDIDO_RETIRADO
            DriverStep.PEDIDO_RETIRADO -> DriverStep.INDO_ENTREGA
            DriverStep.INDO_ENTREGA -> DriverStep.FINALIZADO
            else -> _state.value.step
        }
        viewModelScope.launch {
            runCatching {
                when (next) {
                    DriverStep.CHEGUEI_COLETA -> repo.updateStep(ride, "cheguei_na_coleta")
                    DriverStep.PEDIDO_RETIRADO -> repo.updateStep(ride, "pedido_retirado")
                    DriverStep.INDO_ENTREGA -> repo.updateStep(ride, "indo_entrega")
                    DriverStep.FINALIZADO -> repo.finishRide(ride)
                    else -> Unit
                }
            }.onSuccess {
                _state.value = if (next == DriverStep.FINALIZADO) _state.value.copy(activeRide = null, step = DriverStep.DISPONIVEL, isAvailable = true, activeTab = "home", error = null) else _state.value.copy(step = next, activeTab = "route", error = null)
            }.onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }
    override fun onCleared() { listener?.remove(); super.onCleared() }
}
