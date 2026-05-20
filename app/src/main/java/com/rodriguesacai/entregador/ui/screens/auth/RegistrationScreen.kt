package com.rodriguesacai.entregador.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.Field
import com.rodriguesacai.entregador.ui.components.PrimaryButton
import com.rodriguesacai.entregador.ui.components.UploadCard

@Composable
fun RegistrationScreen(
    onBack: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit,
    onFinish: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }

    BasePage("Cadastro", "Envie seus dados para análise", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Field(nome, { nome = it }, "Nome completo")
            Field(cpf, { cpf = it.filter(Char::isDigit).take(11) }, "CPF", KeyboardType.Number)
            Field(telefone, { telefone = it.filter(Char::isDigit).take(11) }, "Telefone", KeyboardType.Phone)
            Field(placa, { placa = it.uppercase().take(8) }, "Placa da moto")
            UploadCard("Documento com foto", "Espaço reservado para câmera/galeria na próxima etapa")
            UploadCard("Selfie do entregador", "Validação visual para aprovação do gestor")
            PrimaryButton("Enviar para análise") {
                onSubmit(nome, cpf, telefone, placa)
                onFinish()
            }
        }
    }
}
