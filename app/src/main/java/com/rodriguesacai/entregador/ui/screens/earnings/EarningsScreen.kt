package com.rodriguesacai.entregador.ui.screens.earnings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.MetricBox
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.RoundIcon
import com.rodriguesacai.entregador.ui.components.UpCard
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.moneyOrEmpty
import com.rodriguesacai.entregador.ui.navigation.AppRoute
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun EarningsScreen(driver: Driver?, history: List<Ride>, onBack: () -> Unit, onWallet: () -> Unit, onToggleValues: (Boolean) -> Unit, onNav: (AppRoute) -> Unit) {
    val hidden = driver?.ocultarValores == true
    UpPage(title = "Ganhos", onBack = onBack, current = AppRoute.Ganhos, onNav = onNav, right = { RoundIcon(if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, { onToggleValues(!hidden) }, tint = UpColors.Green, bg = UpColors.GreenSoft) }) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            UpCard {
                Text("Total de ganhos", color = UpColors.Muted, fontWeight = FontWeight.SemiBold)
                Text(moneyOrEmpty(driver?.saldoHoje, hidden), color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 42.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricBox("Corridas finalizadas", (driver?.corridasHoje ?: 0).toString(), Modifier.weight(1f))
                    MetricBox("Semana", moneyOrEmpty(driver?.saldoSemana, hidden), Modifier.weight(1f))
                }
            }
            UpCard {
                Text("Resumo do dia", color = UpColors.Ink, fontWeight = FontWeight.Black)
                if (history.any { it.valorCorrida != null }) EarningsChart(history)
                else EmptyState("Sem valores calculados", "Quando houver corridas finalizadas com valor, o gráfico real aparece aqui.", Icons.Rounded.BarChart)
            }
            PrimaryAction("Ver carteira e repasse", onWallet, icon = Icons.Rounded.AccountBalanceWallet)
        }
    }
}

@Composable
private fun EarningsChart(history: List<Ride>) {
    val values = history.take(16).map { (it.valorCorrida ?: 0.0).toFloat().coerceAtLeast(0f) }.ifEmpty { listOf(0f) }
    val max = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    Canvas(Modifier.fillMaxWidth().height(180.dp).padding(top = 14.dp)) {
        val step = size.width / values.size.coerceAtLeast(1)
        repeat(4) { i ->
            val y = size.height * (i + 1) / 5f
            drawLine(UpColors.Line, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
        }
        values.forEachIndexed { i, v ->
            val h = (size.height - 24f) * (v / max)
            drawRoundRect(UpColors.Green.copy(alpha = .35f + (.55f * v / max)), topLeft = Offset(i * step + step * .24f, size.height - h), size = androidx.compose.ui.geometry.Size(step * .52f, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f,9f))
        }
    }
}
