package com.rodriguesacai.entregador.ui.screens.rides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.AddressBlock
import com.rodriguesacai.entregador.ui.components.AlertBox
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.NativeMapPreview
import com.rodriguesacai.entregador.ui.components.OutlineAction
import com.rodriguesacai.entregador.ui.deliveryAddressVisible
import com.rodriguesacai.entregador.ui.openNavigation
import com.rodriguesacai.entregador.ui.pickupVisibleAddress
import com.rodriguesacai.entregador.ui.safeDeliveryAddress
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun MapRouteScreen(ride: Ride?, onBack: () -> Unit) {
    val context = LocalContext.current
    BasePage("Mapa da rota", "Mapa real quando a corrida tiver coordenadas", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NativeMapPreview(ride, Modifier.fillMaxWidth().height(420.dp))
            if (ride != null) {
                val entregaTitulo = if (ride.deliveryAddressVisible()) ride.clienteNome.ifBlank { "Cliente" } else ride.clienteBairro.ifBlank { "Bairro pendente" }
                AddressBlock("Coleta", ride.lojaNome.ifBlank { "Coleta" }, ride.pickupVisibleAddress())
                AddressBlock("Entrega", entregaTitulo, ride.safeDeliveryAddress())
                OutlineAction("Iniciar navegação") { openNavigation(context, ride) }
            } else {
                AlertBox("Nenhuma corrida ativa para abrir rota.", AppColors.Muted)
            }
        }
    }
}
