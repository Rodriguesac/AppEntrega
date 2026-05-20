package com.rodriguesacai.entregador.ui.screens.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.OutlineAction
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    BasePage("Permissões", "Necessário para alerta urgente e rastreamento", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PermissionCard("Notificações", "Receber corrida urgente em segundo plano") {
                if (Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            PermissionCard("Localização", "Atualizar rota durante a corrida") {
                locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
            PermissionCard("Tela cheia urgente", "Permite abrir a oferta por cima da tela bloqueada") {}
            PermissionCard("Sem restrição de bateria", "Evita o sistema matar o app em corrida") {}
        }
    }
}

@Composable
private fun PermissionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = AppColors.Ink, fontWeight = FontWeight.Black)
            Text(subtitle, color = AppColors.Muted)
            OutlineAction("Configurar", onClick)
        }
    }
}
