package com.rodriguesacai.entregador.ui.screens.earnings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.AlertBox
import com.rodriguesacai.entregador.ui.components.AppBottomBar
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.Metric
import com.rodriguesacai.entregador.ui.components.OutlineAction
import com.rodriguesacai.entregador.ui.moneyOrEmpty
import com.rodriguesacai.entregador.ui.navigation.AppRoute
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun EarningsScreen(
    driver: Driver?,
    history: List<Ride>,
    onBack: () -> Unit,
    onWallet: () -> Unit,
    onToggleValues: (Boolean) -> Unit,
    onNav: (AppRoute) -> Unit
) {
    val hidden = driver?.ocultarValores == true
    val finishedTotal = history.filter { it.status == "FINALIZADA" }.mapNotNull { it.valorCorrida }.takeIf { it.isNotEmpty() }?.sum()
    BasePage("Ganhos", "Valores vindos do Firebase", onBack, bottomBar = { AppBottomBar(AppRoute.Ganhos, onNav) }) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (driver == null) AlertBox("Sincronizando dados financeiros do entregador.", AppColors.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Metric("Hoje", moneyOrEmpty(driver?.saldoHoje, hidden), AppColors.Green, Modifier.weight(1f))
                Metric("Semana", moneyOrEmpty(driver?.saldoSemana, hidden), AppColors.Ink, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Metric("Mês", moneyOrEmpty(driver?.saldoMes, hidden), AppColors.Ink, Modifier.weight(1f))
                Metric("Corridas", driver?.corridasHoje?.toString() ?: "Sem dado", AppColors.Ink, Modifier.weight(1f))
            }
            Metric("Total lido do histórico", moneyOrEmpty(finishedTotal, hidden), AppColors.Green)
            OutlineAction(if (hidden) "Mostrar valores" else "Ocultar valores") { onToggleValues(!hidden) }
            OutlineAction("Abrir carteira/repasse") { onWallet() }
        }
    }
}
