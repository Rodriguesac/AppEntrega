package com.rodriguesacai.entregador.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.DriverUiState
import com.rodriguesacai.entregador.ui.components.AvailabilityPill
import com.rodriguesacai.entregador.ui.components.DriverHeader
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.FinancialMini
import com.rodriguesacai.entregador.ui.components.OperationBanner
import com.rodriguesacai.entregador.ui.components.RideOfferCard
import com.rodriguesacai.entregador.ui.components.ShortcutGrid
import com.rodriguesacai.entregador.ui.components.UpBottomBar
import com.rodriguesacai.entregador.ui.components.UpInfoBox
import com.rodriguesacai.entregador.ui.navigation.AppRoute
import com.rodriguesacai.entregador.ui.design.UpColors
import androidx.compose.material3.Scaffold

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
    val running = state.activeRides.firstOrNull { it.status != "OFERTA_RECEBIDA" }
    Scaffold(
        containerColor = UpColors.Screen,
        bottomBar = { UpBottomBar(AppRoute.Home, onNav) }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp, vertical = 14.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DriverHeader(state.driver, onNotifications = { onNav(AppRoute.Notificacoes) }, onMenu = { onNav(AppRoute.Perfil) })
            AvailabilityPill(state.driver, onOnline)
            if (state.error != null) UpInfoBox("Falha de sincronização", state.error, Icons.Rounded.Warning, UpColors.Orange, UpColors.OrangeSoft)
            if (state.driver?.statusOperacional == "RESTRICAO") {
                UpInfoBox("Operação com restrição", state.driver.restricaoMotivo.ifBlank { "Verifique permissões, bateria, internet ou liberação da operação." }, Icons.Rounded.Warning, UpColors.Red, UpColors.RedSoft)
            }
            FinancialMini(state.driver, onToggleValues, onWallet = { onNav(AppRoute.Carteira) })
            OperationBanner("Novidades da operação", "Comunicados e melhorias aparecem aqui quando forem publicados pelo gestor.", onClick = { onNav(AppRoute.Notificacoes) })
            ShortcutGrid(
                onHistory = { onNav(AppRoute.Historico) },
                onEarnings = { onNav(AppRoute.Ganhos) },
                onMap = { if (running != null) onRide(running) else onNav(AppRoute.Permissoes) },
                onSupport = { onNav(AppRoute.Perfil) }
            )
            when {
                offer != null -> RideOfferCard(offer, onClick = { onUrgent(offer) })
                running != null -> RideOfferCard(running, onClick = { onRide(running) })
                state.driver == null -> EmptyState("Perfil aguardando sincronização", "Entre com uma conta aprovada para carregar disponibilidade, corridas, ganhos e avisos reais.", Icons.Rounded.Info)
                else -> EmptyState("Nenhuma corrida disponível", "Quando a operação enviar uma nova corrida para seu perfil, ela aparecerá aqui e na tela urgente.", Icons.Rounded.DeliveryDining)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
