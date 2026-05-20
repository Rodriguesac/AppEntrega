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
import com.rodriguesacai.entregador.ui.components.AlertBox
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.Field
import com.rodriguesacai.entregador.ui.components.PasswordChecklist
import com.rodriguesacai.entregador.ui.components.PrimaryButton
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun CreatePasswordScreen(
    message: String?,
    error: String?,
    onBack: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var cpf by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    BasePage("Criar senha", "Primeiro acesso do entregador", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Field(cpf, { cpf = it.filter(Char::isDigit).take(11) }, "CPF cadastrado", KeyboardType.Number)
            Field(senha, { senha = it }, "Nova senha", KeyboardType.Password, password = true)
            PasswordChecklist(senha)
            if (message != null) AlertBox(message, AppColors.Green)
            if (error != null) AlertBox(error, AppColors.Red)
            PrimaryButton("Salvar senha") { onCreate(cpf, senha) }
        }
    }
}
