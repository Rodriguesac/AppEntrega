package com.rodriguesacai.entregador.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.ui.components.FormField
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.SecondaryAction
import com.rodriguesacai.entregador.ui.components.UpCard
import com.rodriguesacai.entregador.ui.components.UpInfoBox
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.components.UploadBox
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun ChangeRequestScreen(onBack: () -> Unit, onSend: (String, String, String) -> Unit) {
    var tipo by remember { mutableStateOf("Telefone") }
    var novo by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    UpPage(title = "Solicitação de alteração", onBack = onBack) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Selecione o tipo de alteração", color = UpColors.Ink, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Telefone", "E-mail", "Pix", "Banco").forEach { item ->
                    FilterChip(selected = tipo == item, onClick = { tipo = item }, label = { Text(item) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UpColors.Green, selectedLabelColor = androidx.compose.ui.graphics.Color.White))
                }
            }
            UpCard {
                FormField(novo, { novo = it }, "Novo valor", if (tipo == "E-mail") Icons.Rounded.Email else Icons.Rounded.Phone)
                FormField(motivo, { motivo = it }, "Motivo da alteração", Icons.Rounded.Info, minLines = 4)
            }
            UploadBox("Comprovante opcional", "PNG, JPG ou PDF até 5MB", onClick = {})
            UpInfoBox("Análise da operação", "Sua solicitação será analisada antes de atualizar os dados sensíveis do cadastro.", Icons.Rounded.Info, UpColors.Green, UpColors.GreenSoft)
            Spacer(Modifier.weight(1f))
            PrimaryAction("Enviar solicitação", onClick = { onSend(tipo, novo, motivo) })
            SecondaryAction("Voltar", onBack)
        }
    }
}
