package com.rodriguesacai.entregador.ui.screens.rides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.AddressBlock
import com.rodriguesacai.entregador.ui.components.AlertBox
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.Metric
import com.rodriguesacai.entregador.ui.components.NativeMapPreview
import com.rodriguesacai.entregador.ui.components.OutlineAction
import com.rodriguesacai.entregador.ui.components.PrimaryButton
import com.rodriguesacai.entregador.ui.format1
import com.rodriguesacai.entregador.ui.humanStatus
import com.rodriguesacai.entregador.ui.money
import com.rodriguesacai.entregador.ui.nextActionText
import com.rodriguesacai.entregador.ui.statusColor
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun ActiveRideScreen(
    ride: Ride?,
    onBack: () -> Unit,
    onMap: () -> Unit,
    onAdvance: (Ride) -> Unit,
    onOccurrence: (Ride) -> Unit
) {
    BasePage("Corrida em andamento", ride?.let { humanStatus(it.status) } ?: "Sem corrida ativa", onBack) {
        if (ride == null) {
            AlertBox("Nenhuma corrida em andamento.", AppColors.Muted)
            return@BasePage
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NativeMapPreview(ride)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Metric("Valor", money(ride.valorCorrida), AppColors.Green, Modifier.weight(1f))
                Metric("Distância", "${ride.distanciaKm.format1()} km", AppColors.Ink, Modifier.weight(1f))
            }
            AlertBox(humanStatus(ride.status), statusColor(ride.status))
            AddressBlock("Coleta", ride.lojaNome, ride.lojaEndereco)
            AddressBlock("Entrega", ride.clienteNome, ride.clienteEnderecoCompleto)
            PrimaryButton(nextActionText(ride.status)) { onAdvance(ride) }
            OutlineAction("Abrir mapa maior") { onMap() }
            OutlineAction("Registrar ocorrência") { onOccurrence(ride) }
        }
    }
}
