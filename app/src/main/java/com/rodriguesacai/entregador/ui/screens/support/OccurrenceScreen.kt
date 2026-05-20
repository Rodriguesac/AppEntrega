package com.rodriguesacai.entregador.ui.screens.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.FormField
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.RideOfferCard
import com.rodriguesacai.entregador.ui.components.SecondaryAction
import com.rodriguesacai.entregador.ui.components.UpCard
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun OccurrenceScreen(ride: Ride?, onBack: () -> Unit, onSend: (String, String, String) -> Unit) {
    var motivo by remember { mutableStateOf("Cliente não atende") }
    var detalhe by remember { mutableStateOf("") }
    val motivos = listOf("Cliente não atende", "Endereço divergente", "Pagamento pendente", "Local inseguro", "Pedido danificado", "Cliente ausente", "Aguardando cliente", "Outro motivo")
    UpPage(title = "Ocorrência", onBack = onBack) {
        if (ride == null) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) { EmptyState("Sem corrida para ocorrência", "Selecione uma corrida em andamento para registrar ocorrência.", Icons.Rounded.Warning, "Voltar", onBack) }
        } else {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                RideOfferCard(ride, onClick = {}, compact = true)
                UpCard {
                    Text("Selecione o motivo", color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    motivos.forEach { item ->
                        FilterChip(
                            selected = motivo == item,
                            onClick = { motivo = item },
                            label = { Text(item) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UpColors.Green, selectedLabelColor = Color.White)
                        )
                    }
                }
                FormField(detalhe, { detalhe = it }, "Descreva a ocorrência", Icons.Rounded.Warning, minLines = 5)
                Spacer(Modifier.weight(1f))
                PrimaryAction("Registrar ocorrência", onClick = { onSend(ride.id, motivo, detalhe) }, icon = Icons.Rounded.Warning)
                SecondaryAction("Voltar para entrega", onBack)
            }
        }
    }
}
