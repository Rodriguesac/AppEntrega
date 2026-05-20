package com.rodriguesacai.entregador.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.DriverUiState
import com.rodriguesacai.entregador.ui.components.ActiveRideCard
import com.rodriguesacai.entregador.ui.components.AppBottomBar
import com.rodriguesacai.entregador.ui.components.EarningsCompact
import com.rodriguesacai.entregador.ui.components.Header
import com.rodriguesacai.entregador.ui.components.OutlineAction
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
    val restricted = state.driver?.statusOperacional == "RESTRICAO"
    val offer = if (restricted) null else state.activeRides.firstOrNull { it.status == "OFERTA_RECEBIDA" }
    val active = state.activeRides.firstOrNull { it.status != "OFERTA_RECEBIDA" }

    Scaffold(
        containerColor = AppColors.Bg,
        bottomBar = { AppBottomBar(AppRoute.Home, onNav) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Header(state.driver, onLogout) }
            item { StatusSwitch(state.driver, onOnline) }
            item { EarningsCompact(state.driver, onToggleValues) }
            item {
                when {
                    state.driver == null -> OperationCard("Sincronizando perfil", "Aguardando dados reais do Firebase.", "Abrir diagnóstico", onPermissions)
                    restricted -> OperationCard("Operação com restrição", state.driver.restricaoMotivo.ifBlank { "Corrija permissões, bateria, GPS ou status cadastral." }, "Ver permissões", onPermissions)
                    offer != null -> UrgentCard(offer) { onUrgent(offer) }
                    active != null -> ActiveRideCard(active, { onRide(active) }, { onNav(AppRoute.Mapa) })
                    else -> OperationCard("Nenhuma corrida disponível", "Quando a operação enviar uma corrida para este entregador, ela aparecerá aqui.", "Ver permissões", onPermissions)
                }
            }
            item {
                HomeActionGrid(
                    onHistory = { onNav(AppRoute.Historico) },
                    onEarnings = { onNav(AppRoute.Ganhos) },
                    onMap = { onNav(AppRoute.Mapa) },
                    onSupport = { onNav(AppRoute.Notificacoes) }
                )
            }
        }
    }
}

@Composable
private fun OperationCard(title: String, message: String, action: String, onOpen: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 19.sp)
            Text(message, color = AppColors.Muted, fontSize = 13.sp)
            OutlineAction(action, onOpen)
        }
    }
}

@Composable
private fun HomeActionGrid(
    onHistory: () -> Unit,
    onEarnings: () -> Unit,
    onMap: () -> Unit,
    onSupport: () -> Unit
) {
    val items = listOf(
        Triple("Histórico", "Corridas reais", onHistory),
        Triple("Ganhos", "Resumo financeiro", onEarnings),
        Triple("Mapa", "Corrida ativa", onMap),
        Triple("Suporte", "Avisos e ajuda", onSupport)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (title, subtitle, action) ->
                    Card(
                        modifier = Modifier.weight(1f).height(94.dp).clickable { action() },
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.fillMaxSizeCompat().padding(16.dp), verticalArrangement = Arrangement.Center) {
                            Text(title, color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(4.dp))
                            Text(subtitle, color = AppColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.fillMaxSizeCompat(): Modifier = this.then(Modifier.fillMaxSize())
