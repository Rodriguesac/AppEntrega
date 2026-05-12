package com.rodrigues.entregador.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UrgentRideScreen(
    rideId: String,
    value: String,
    distance: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF14051F)).padding(22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("NOVA CORRIDA", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Card(shape = RoundedCornerShape(32.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(26.dp)) {
                Text(value, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                Text("Distância: $distance", fontSize = 20.sp)
                Text("Pedido: $rideId")
                Spacer(Modifier.height(24.dp))
                Button(onClick = onAccept, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(20.dp)) {
                    Text("ACEITAR CORRIDA", fontSize = 18.sp)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onReject, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(20.dp)) {
                    Text("Recusar")
                }
            }
        }
    }
}
