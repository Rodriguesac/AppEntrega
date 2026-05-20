package com.rodriguesacai.entregador.ui.screens.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.ui.components.PermissionItem
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.SecondaryAction
import com.rodriguesacai.entregador.ui.components.UpLogo
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val notificationGranted = remember { mutableStateOf(false) }
    val locationGranted = remember { mutableStateOf(false) }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notificationGranted.value = it }
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result -> locationGranted.value = result.values.any { it } }

    UpPage(title = "Permissões", onBack = onBack) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            UpLogo(compact = true)
            Text("Permissões do app", color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 26.sp, textAlign = TextAlign.Center)
            Text("Precisamos destas liberações para receber corridas com segurança e manter a operação funcionando.", color = UpColors.Text, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 21.sp)
            PermissionItem("Notificações", "Receba alertas de novas corridas e atualizações.", Icons.Rounded.NotificationsActive, notificationGranted.value) {
                if (Build.VERSION.SDK_INT >= 33) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else notificationGranted.value = true
            }
            PermissionItem("Localização", "Permite encontrar corridas e atualizar rota durante entrega.", Icons.Rounded.GpsFixed, locationGranted.value) {
                locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
            PermissionItem("Alerta em tela cheia", "Garante que a oferta urgente apareça mesmo fora do app.", Icons.Rounded.Security, false) {
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
            }
            PermissionItem("Bateria sem restrição", "Evita que o sistema encerre localização e notificações.", Icons.Rounded.BatteryAlert, false) {
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
            }
            Spacer(Modifier.weight(1f))
            PrimaryAction("Configurar permissões", onClick = {
                if (Build.VERSION.SDK_INT >= 33) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            })
            SecondaryAction("Continuar", onBack)
            Spacer(Modifier.height(8.dp))
        }
    }
}
