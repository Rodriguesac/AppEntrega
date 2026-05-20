package com.rodriguesacai.entregador.ui.screens.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.AlertBox
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.Field
import com.rodriguesacai.entregador.ui.components.PrimaryButton
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun OccurrenceScreen(ride: Ride?, onBack: () -> Unit, onSend: (String, String, String) -> Unit) {
    var motivo by remember { mutableStateOf("") }
    var detalhe by remember { mutableStateOf("") }
    BasePage("Ocorrência", "Mantém a corrida aberta para solução", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (ride == null) AlertBox("Nenhuma corrida selecionada.", AppColors.Red)
            else AlertBox("Pedido ${ride.numeroPedido}", AppColors.Ink)
            Field(motivo, { motivo = it }, "Motivo")
            Field(detalhe, { detalhe = it }, "Detalhe da ocorrência")
            PrimaryButton("Registrar ocorrência") {
                if (ride != null) onSend(ride.id, motivo, detalhe)
            }
        }
    }
}
