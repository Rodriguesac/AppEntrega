package com.rodrigues.entregador.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriverHomeScreen(onGoOnline: () -> Unit, onGoOffline: () -> Unit) {
    var online by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF2B0A45), Color(0xFF5B189A))))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Rodrigues Entregador", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))

            Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp)) {
                    Text(if (online) "Você está disponível" else "Você está offline", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(if (online) "Aguardando novas entregas próximas." else "Toque para ficar online e receber corridas.")
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            online = !online
                            if (online) onGoOnline() else onGoOffline()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(if (online) "Ficar offline" else "Ficar online", fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("Operação", fontWeight = FontWeight.Bold)
                    Text("GPS ativo somente quando estiver online.")
                    Text("Pedidos urgentes abrem em tela cheia.")
                    Text("Navegação será aberta no app escolhido.")
                }
            }
        }

        Text(
            "v1.0.0 nativo",
            color = Color.White.copy(alpha = 0.75f),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
