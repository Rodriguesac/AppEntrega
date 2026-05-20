package com.rodriguesacai.entregador.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.Field
import com.rodriguesacai.entregador.ui.components.PrimaryButton

@Composable
fun ChangeRequestScreen(onBack: () -> Unit, onSend: (String, String, String) -> Unit) {
    var tipo by remember { mutableStateOf("Telefone") }
    var valor by remember { mutableStateOf("") }
    var obs by remember { mutableStateOf("") }
    BasePage("Solicitação", "Alteração depende de aprovação", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Field(tipo, { tipo = it }, "Tipo: telefone ou e-mail")
            Field(valor, { valor = it }, "Novo valor")
            Field(obs, { obs = it }, "Observação")
            PrimaryButton("Enviar solicitação") { onSend(tipo, valor, obs) }
        }
    }
}
