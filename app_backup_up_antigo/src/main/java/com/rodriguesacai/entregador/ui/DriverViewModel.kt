package com.rodriguesacai.entregador.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodriguesacai.entregador.data.DriverStep
import com.rodriguesacai.entregador.data.DriverUiState
import com.rodriguesacai.entregador.data.FirebaseRepository
import com.rodriguesacai.entregador.data.RideOffer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DriverViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.listenOpenOffers().collect { offer ->
                val current = _uiState.value
                if (offer != null && current.activeRide == null) {
                    _uiState.value = current.copy(
                        pendingOffer = offer,
                        step = DriverStep.EM_OFERTA,
                        activeTab = "home"
                    )
                    runCatching { repository.markVisualized(offer) }
                }
            }
        }
    }

    fun toggleTheme() {
        _uiState.value = _uiState.value.copy(isDark = !_uiState.value.isDark)
    }

    fun setTab(tab: String) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun toggleEarningsVisibility() {
        _uiState.value = _uiState.value.copy(earningsVisible = !_uiState.value.earningsVisible)
    }

    fun setAvailable(available: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching {
                repository.setDriverAvailable(available)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    isAvailable = available,
                    step = if (available) DriverStep.DISPONIVEL else DriverStep.INDISPONIVEL
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(loading = false, error = it.message)
            }
        }
    }

    fun acceptOffer() {
        val offer = _uiState.value.pendingOffer ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching {
                repository.acceptOffer(offer)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    pendingOffer = null,
                    activeRide = offer,
                    step = DriverStep.INDO_COLETA,
                    activeTab = "route",
                    isAvailable = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(loading = false, error = it.message)
            }
        }
    }

    fun rejectOffer() {
        val offer = _uiState.value.pendingOffer ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching {
                repository.rejectOffer(offer)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    pendingOffer = null,
                    step = if (_uiState.value.isAvailable) DriverStep.DISPONIVEL else DriverStep.INDISPONIVEL
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(loading = false, error = it.message)
            }
        }
    }

    fun nextStep() {
        val offer = _uiState.value.activeRide ?: return
        val next = when (_uiState.value.step) {
            DriverStep.INDO_COLETA -> DriverStep.CHEGUEI_COLETA
            DriverStep.CHEGUEI_COLETA -> DriverStep.PEDIDO_RETIRADO
            DriverStep.PEDIDO_RETIRADO -> DriverStep.INDO_ENTREGA
            DriverStep.INDO_ENTREGA -> DriverStep.FINALIZADO
            else -> _uiState.value.step
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching {
                if (next == DriverStep.FINALIZADO) {
                    repository.finishRide(offer)
                } else {
                    repository.updateStep(offer, next)
                }
            }.onSuccess {
                if (next == DriverStep.FINALIZADO) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        activeRide = null,
                        step = DriverStep.DISPONIVEL,
                        isAvailable = true,
                        activeTab = "home",
                        ganhosHoje = _uiState.value.ganhosHoje + offer.valorEntrega,
                        corridasHoje = _uiState.value.corridasHoje + 1
                    )
                } else {
                    _uiState.value = _uiState.value.copy(loading = false, step = next)
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(loading = false, error = it.message)
            }
        }
    }
}
