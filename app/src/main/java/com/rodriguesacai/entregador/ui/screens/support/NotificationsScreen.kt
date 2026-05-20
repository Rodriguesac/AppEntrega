package com.rodriguesacai.entregador.ui.screens.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.DriverNotification
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.CardLine
import com.rodriguesacai.entregador.ui.components.EmptyCard
import com.rodriguesacai.entregador.ui.shortDate
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun NotificationsScreen(items: List<DriverNotification>, onBack: () -> Unit) {
    BasePage("Notificações", "Alertas da operação", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (items.isEmpty()) EmptyCard("Nenhuma notificação.")
            items.forEach { item ->
                CardLine(item.titulo, "${shortDate(item.criadaEm)} • ${item.mensagem}", item.tipo, AppColors.Green)
            }
        }
    }
}
