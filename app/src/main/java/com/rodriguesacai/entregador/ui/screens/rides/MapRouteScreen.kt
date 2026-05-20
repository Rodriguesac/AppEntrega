package com.rodriguesacai.entregador.ui.screens.rides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.AddressBlock
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.NativeMapPreview
import com.rodriguesacai.entregador.ui.components.OutlineAction

@Composable
fun MapRouteScreen(ride: Ride?, onBack: () -> Unit) {
    BasePage("Mapa da rota", "Mapa limpo sem cards cobrindo a rota", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NativeMapPreview(ride, Modifier.fillMaxWidth().height(420.dp))
            if (ride != null) {
                AddressBlock("Coleta", ride.lojaNome, ride.lojaEndereco)
                AddressBlock("Entrega", ride.clienteNome, ride.clienteEnderecoCompleto)
            }
            OutlineAction("Iniciar navegação no app padrão") {}
        }
    }
}
