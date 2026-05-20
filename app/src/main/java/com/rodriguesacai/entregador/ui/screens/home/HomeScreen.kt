package com.rodriguesacai.entregador.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.DriverUiState
import com.rodriguesacai.entregador.ui.components.ActiveRideCard
import com.rodriguesacai.entregador.ui.components.AppBottomBar
import com.rodriguesacai.entregador.ui.components.EarningsCompact
import com.rodriguesacai.entregador.ui.components.EmptyCard
import com.rodriguesacai.entregador.ui.components.Header
import com.rodriguesacai.entregador.ui.components.QuickGrid
import com.rodriguesacai.entregador.ui.components.StatusSwitch
import com.rodriguesacai.entregador.ui.components.UrgentCard
import com.rodriguesacai.entregador.ui.navigation.AppRoute
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun HomeScreen(
    state: DriverUiState,
    onOnline: (Boolean) -> Unit,
    onPermissions: () -> Unit,
    onUrgent: (Ride) -> Unit,
    onRide: (Ride) -> Unit,
    onNav: (AppRoute) -> Unit,
    onLogout: () -> Unit,
    onToggleValues: (Boolean) -> Unit
) {
    val offer = state.activeRides.firstOrNull { it.status == "OFERTA_RECEBIDA" }
    val active = state.activeRides.firstOrNull { it.status != "OFERTA_RECEBIDA" }

    Scaffold(
        containerColor = AppColors.Bg,
        bottomBar = { AppBottomBar(AppRoute.Home, onNav) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Header(state.driver, onLogout) }
            item { StatusSwitch(state.driver, onOnline) }
            item { EarningsCompact(state.driver, onToggleValues) }
            item {
                if (offer != null) UrgentCard(offer) { onUrgent(offer) }
                else EmptyCard("Nenhuma oferta agora. Fique disponível para receber corridas.")
            }
            item {
                if (active != null) ActiveRideCard(active, { onRide(active) }, { onNav(AppRoute.Mapa) })
                else MiniMapCard(active, onPermissions)
            }
            item {
                QuickGrid(
                    items = listOf(
                        "Permissões" to AppRoute.Permissoes,
                        "Notificações" to AppRoute.Notificacoes,
                        "Carteira" to AppRoute.Carteira,
                        "Ocorrência" to AppRoute.Ocorrencia,
                        "Sem internet" to AppRoute.SemInternet,
                        "Erro Firebase" to AppRoute.ErroFirebase
                    ),
                    onNav = onNav
                )
            }
        }
    }
}

@Composable
fun MiniMapCard(active: Ride?, onOpen: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        com.rodriguesacai.entregador.ui.components.MiniMapDrawing(
            modifier = Modifier.fillMaxWidth().height(190.dp)
        )
        com.rodriguesacai.entregador.ui.components.OutlineAction(
            if (active == null) "Ver permissões e operação" else "Abrir mapa da corrida",
            onOpen
        )
    }
}
