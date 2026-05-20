package com.rodriguesacai.entregador.ui.screens.support

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.rodriguesacai.entregador.ui.components.StatusHero
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun StatusPage(title: String, message: String, color: Color, onBack: () -> Unit) {
    val icon: ImageVector = when {
        title.contains("internet", ignoreCase = true) -> Icons.Rounded.WifiOff
        title.contains("manutenção", ignoreCase = true) -> Icons.Rounded.Build
        title.contains("firebase", ignoreCase = true) -> Icons.Rounded.Error
        title.contains("atualização", ignoreCase = true) -> Icons.Rounded.Info
        else -> Icons.Rounded.Error
    }
    StatusHero(title = title, message = message, icon = icon, color = color, action = "Atualizar status", onAction = onBack, secondary = "Voltar", onSecondary = onBack)
}
