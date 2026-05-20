package com.rodriguesacai.entregador.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.ui.components.FormField
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.SecondaryAction
import com.rodriguesacai.entregador.ui.components.UpCard
import com.rodriguesacai.entregador.ui.components.UpInfoBox
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun PixBankScreen(driver: Driver?, onBack: () -> Unit, onSave: (String, String, String) -> Unit) {
    var chave by remember(driver?.pixChave) { mutableStateOf(driver?.pixChave.orEmpty()) }
    var tipo by remember(driver?.pixTipo) { mutableStateOf(driver?.pixTipo.orEmpty()) }
    var banco by remember(driver?.banco) { mutableStateOf(driver?.banco.orEmpty()) }
    var agencia by remember(driver?.agencia) { mutableStateOf(driver?.agencia.orEmpty()) }
    var conta by remember(driver?.conta) { mutableStateOf(driver?.conta.orEmpty()) }

    UpPage(title = "Recebimento", onBack = onBack) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            UpInfoBox(if (chave.isBlank()) "Chave Pix pendente" else "Chave Pix ativa", if (chave.isBlank()) "Cadastre uma chave Pix para receber repasses quando a operação liberar." else "Seus repasses serão enviados conforme programação da operação.", Icons.Rounded.CheckCircle, UpColors.Green, UpColors.GreenSoft)
            UpCard {
                Text("Chave Pix", color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 17.sp)
                FormField(chave, { chave = it }, "Chave Pix", Icons.Rounded.CreditCard, KeyboardType.Text)
                FormField(tipo, { tipo = it }, "Tipo da chave", Icons.Rounded.CreditCard)
            }
            UpCard {
                Text("Dados bancários", color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 17.sp)
                FormField(banco, { banco = it }, "Banco", Icons.Rounded.AccountBalanceWallet)
                FormField(agencia, { agencia = it }, "Agência", Icons.Rounded.AccountBalanceWallet, KeyboardType.Number)
                FormField(conta, { conta = it }, "Conta", Icons.Rounded.AccountBalanceWallet, KeyboardType.Number)
            }
            UpInfoBox("Titularidade", "A conta precisa estar no nome do entregador titular. A operação pode revisar alterações antes do próximo repasse.", Icons.Rounded.Lock, UpColors.Green, UpColors.GreenSoft)
            Spacer(Modifier.weight(1f))
            PrimaryAction("Salvar dados", onClick = { onSave(chave, tipo, banco) })
            SecondaryAction("Voltar", onBack)
        }
    }
}
