package com.rodriguesacai.entregador.ui.screens.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rodriguesacai.entregador.data.DriverNotification
import com.rodriguesacai.entregador.ui.components.CompactList
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.NotificationRow
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.navigation.AppRoute

@Composable
fun NotificationsScreen(notifications: List<DriverNotification>, onBack: () -> Unit) {
    UpPage(title = "Notificações", onBack = onBack) {
        if (notifications.isEmpty()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                EmptyState("Nenhum aviso no momento", "Corridas, repasses e comunicados da operação aparecem aqui quando forem enviados.", Icons.Rounded.Notifications)
            }
        } else {
            CompactList {
                items(notifications, key = { it.id }) { item ->
                    NotificationRow(item.titulo, item.mensagem, item.tipo, unread = !item.lida)
                }
            }
        }
    }
}
