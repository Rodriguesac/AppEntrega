package com.rodriguesacai.entregador.ui.screens.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.MetricBox
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.ProfileLine
import com.rodriguesacai.entregador.ui.components.UpCard
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.moneyOrEmpty
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun WalletScreen(driver: Driver?, onBack: () -> Unit, onPix: () -> Unit) {
    val hidden = driver?.ocultarValores == true
    UpPage(title = "Carteira", onBack = onBack) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.fillMaxWidth().height(124.dp).background(UpColors.DarkGradient, RoundedCornerShape(22.dp)).padding(20.dp)) {
                Column {
                    Text("Saldo disponível", color = Color.White.copy(.86f), fontWeight = FontWeight.Bold)
                    Text(moneyOrEmpty(driver?.saldoDisponivel, hidden), color = Color.White, fontWeight = FontWeight.Black, fontSize = 34.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricBox("Saldo pendente", moneyOrEmpty(driver?.saldoPendente, hidden), Modifier.weight(1f))
                MetricBox("Total a receber", moneyOrEmpty(driver?.totalAReceber, hidden), Modifier.weight(1f))
            }
            UpCard {
                Text("Recebimento", color = UpColors.Ink, fontWeight = FontWeight.Black)
                ProfileLine(Icons.Rounded.CreditCard, "Chave Pix", driver?.pixChave.orEmpty(), onClick = onPix)
                ProfileLine(Icons.Rounded.AccountBalanceWallet, "Banco", driver?.banco.orEmpty(), onClick = onPix)
            }
            if (driver == null) EmptyState("Carteira aguardando perfil", "Entre com uma conta aprovada para carregar repasse, Pix e dados bancários reais.", Icons.Rounded.AccountBalanceWallet)
            Spacer(Modifier.weight(1f))
            PrimaryAction("Atualizar dados bancários", onPix, icon = Icons.Rounded.CreditCard)
        }
    }
}
