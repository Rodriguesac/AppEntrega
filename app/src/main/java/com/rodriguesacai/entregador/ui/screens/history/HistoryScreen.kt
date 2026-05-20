package com.rodriguesacai.entregador.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.CompactList
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.HistoryRow
import com.rodriguesacai.entregador.ui.components.SectionTitle
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.navigation.AppRoute

@Composable
fun HistoryScreen(history: List<Ride>, onBack: () -> Unit, onNav: (AppRoute) -> Unit) {
    UpPage(title = "Histórico", onBack = onBack, current = AppRoute.Historico, onNav = onNav) {
        if (history.isEmpty()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                EmptyState("Sem histórico ainda", "As corridas finalizadas, recusadas e expiradas aparecem aqui como uma linha por corrida.", Icons.Rounded.History)
            }
        } else {
            SectionTitle("Corridas")
            CompactList { items(history, key = { it.id }) { HistoryRow(it) } }
        }
    }
}
