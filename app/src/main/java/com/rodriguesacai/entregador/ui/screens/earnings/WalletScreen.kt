package com.rodriguesacai.entregador.ui.screens.earnings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.ui.components.AddressBlock
import com.rodriguesacai.entregador.ui.components.AlertBox
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.OutlineAction
import com.rodriguesacai.entregador.ui.moneyOrEmpty
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun WalletScreen(driver: Driver?, onBack: () -> Unit, onPix: () -> Unit) {
    val hidden = driver?.ocultarValores == true
    BasePage("Carteira", "Saldos e recebimento reais", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AddressBlock("Saldo disponível", moneyOrEmpty(driver?.saldoDisponivel, hidden), "Campo carteiraSaldoDisponivel/saldoDisponivel no Firebase.")
            AddressBlock("Saldo pendente", moneyOrEmpty(driver?.saldoPendente, hidden), "Campo carteiraSaldoPendente/saldoPendente no Firebase.")
            AddressBlock("Total a receber", moneyOrEmpty(driver?.totalAReceber, hidden), "Somente aparece quando o gestor lançar o valor.")
            AddressBlock("Pix", driver?.pixTipo.ifBlankFallback("Pix não cadastrado"), driver?.pixChave.ifBlankFallback("Sem chave Pix"))
            AddressBlock("Banco", driver?.banco.ifBlankFallback("Banco não informado"), "A conta precisa estar no nome do titular.")
            AlertBox("Sem lançamento de repasse no Firebase, a carteira não inventa valores.", AppColors.Muted)
            OutlineAction("Editar Pix e banco") { onPix() }
        }
    }
}

private fun String?.ifBlankFallback(fallback: String): String = this?.takeIf { it.isNotBlank() } ?: fallback
