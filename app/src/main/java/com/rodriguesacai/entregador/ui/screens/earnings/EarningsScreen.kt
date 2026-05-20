package com.rodriguesacai.entregador.ui.screens.earnings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.AppBottomBar
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.Metric
import com.rodriguesacai.entregador.ui.components.OutlineAction
import com.rodriguesacai.entregador.ui.money
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
    fun value(v: Double) = if (hidden) "••••" else money(v)
    BasePage("Ganhos", "Hoje, semana, mês e próximo repasse", onBack, bottomBar = { AppBottomBar(AppRoute.Ganhos, onNav) }) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Metric("Hoje", value(driver?.saldoHoje ?: 0.0), AppColors.Green, Modifier.weight(1f))
                Metric("Semana", value(driver?.saldoSemana ?: 0.0), AppColors.Ink, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Metric("Mês", value(driver?.saldoMes ?: 0.0), AppColors.Ink, Modifier.weight(1f))
                Metric("Corridas", "${driver?.corridasHoje ?: 0}", AppColors.Ink, Modifier.weight(1f))
            }
            OutlineAction(if (hidden) "Mostrar valores" else "Ocultar valores") { onToggleValues(!hidden) }
            OutlineAction("Abrir carteira/repasse") { onWallet() }
            Metric("Total lido do histórico", value(history.sumOf { it.valorCorrida }), AppColors.Green)
        }
    }
}
