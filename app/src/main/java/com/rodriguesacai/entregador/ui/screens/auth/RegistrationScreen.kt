package com.rodriguesacai.entregador.ui.screens.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.ui.components.AlertBox
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.Field
import com.rodriguesacai.entregador.ui.components.OutlineAction
import com.rodriguesacai.entregador.ui.components.PrimaryButton
import com.rodriguesacai.entregador.ui.components.UploadCard
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun RegistrationScreen(
    onBack: () -> Unit,
    onSubmit: (String, String, String, String, String?, String?) -> Unit,
    onFinish: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var documentoUri by remember { mutableStateOf<String?>(null) }
    var selfieUri by remember { mutableStateOf<String?>(null) }

    val documentoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> documentoUri = uri?.toString() }
    val selfiePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> selfieUri = uri?.toString() }

    BasePage("Solicitar cadastro", "Envio real para análise no Firebase", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Field(nome, { nome = it }, "Nome completo")
            Field(cpf, { cpf = it.filter(Char::isDigit).take(11) }, "CPF", KeyboardType.Number)
            Field(telefone, { telefone = it.filter(Char::isDigit).take(11) }, "Telefone", KeyboardType.Phone)
            Field(placa, { placa = it.uppercase().take(8) }, "Placa da moto")
            UploadCard("CNH ou documento", if (documentoUri == null) "Toque abaixo para selecionar arquivo real" else "Documento selecionado")
            OutlineAction("Selecionar documento") { documentoPicker.launch("*/*") }
            UploadCard("Selfie do entregador", if (selfieUri == null) "Toque abaixo para selecionar imagem real" else "Selfie selecionada")
            OutlineAction("Selecionar selfie") { selfiePicker.launch("image/*") }
            AlertBox("O cadastro ficará pendente até aprovação no painel gestor.", AppColors.Muted)
            PrimaryButton("Enviar cadastro") {
                onSubmit(nome, cpf, telefone, placa, documentoUri, selfieUri)
                onFinish()
            }
        }
    }
}
