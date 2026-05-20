package com.rodriguesacai.entregador.ui.screens.rides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Route
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.LocationRow
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.RoutePreviewCard
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.openNavigation
import com.rodriguesacai.entregador.ui.safeDeliveryAddress
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun MapRouteScreen(ride: Ride?, onBack: () -> Unit) {
    val context = LocalContext.current
    UpPage(title = "Mapa da rota", onBack = onBack) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (ride == null) {
                EmptyState("Rota indisponível", "A corrida atual não foi encontrada no Firebase.", Icons.Rounded.Route, "Voltar", onBack)
            } else {
                RoutePreviewCard(ride = ride, height = 390.dp)
                LocationRow("Coleta", ride.lojaNome.ifBlank { "Coleta pendente" }, ride.lojaEndereco.ifBlank { "Endereço da coleta pendente" }, UpColors.Green)
                LocationRow("Entrega", ride.clienteBairro.ifBlank { "Bairro pendente" }, ride.safeDeliveryAddress(), UpColors.Orange)
                Spacer(Modifier.weight(1f))
                PrimaryAction("Iniciar navegação", onClick = { openNavigation(context, ride) }, icon = Icons.Rounded.Navigation)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
