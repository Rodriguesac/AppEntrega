package com.rodriguesacai.entregador.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.ui.components.AlertBox
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.Field
import com.rodriguesacai.entregador.ui.components.PrimaryButton
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun PixBankScreen(driver: Driver?, onBack: () -> Unit, onSave: (String, String, String) -> Unit) {
    var chave by remember(driver?.pixChave) { mutableStateOf(driver?.pixChave.orEmpty()) }
    var tipo by remember(driver?.pixTipo) { mutableStateOf(driver?.pixTipo.orEmpty()) }
    var banco by remember(driver?.banco) { mutableStateOf(driver?.banco.orEmpty()) }
    BasePage("Pix/banco", "Dados para repasse", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AlertBox("A conta precisa estar no nome do titular cadastrado.", AppColors.Muted)
            Field(tipo, { tipo = it }, "Tipo da chave Pix")
            Field(chave, { chave = it }, "Chave Pix")
            Field(banco, { banco = it }, "Banco")
            PrimaryButton("Salvar recebimento") { onSave(chave, tipo, banco) }
        }
    }
}
