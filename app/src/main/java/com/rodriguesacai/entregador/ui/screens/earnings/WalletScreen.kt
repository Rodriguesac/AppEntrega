package com.rodriguesacai.entregador.ui.screens.earnings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.ui.components.AddressBlock
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.OutlineAction

@Composable
fun WalletScreen(driver: Driver?, onBack: () -> Unit, onPix: () -> Unit) {
    BasePage("Carteira/repasse", "Dados de recebimento do entregador", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AddressBlock("Pix", driver?.pixTipo ?: "Tipo não informado", driver?.pixChave ?: "Chave Pix não cadastrada")
            AddressBlock("Banco", driver?.banco ?: "Banco não informado", "A conta precisa estar no nome do titular.")
            OutlineAction("Editar Pix e banco") { onPix() }
        }
    }
}
