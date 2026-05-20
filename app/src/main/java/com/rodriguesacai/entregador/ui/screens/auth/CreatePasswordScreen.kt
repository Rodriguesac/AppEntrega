package com.rodriguesacai.entregador.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.ui.components.AuthCard
import com.rodriguesacai.entregador.ui.components.FormField
import com.rodriguesacai.entregador.ui.components.LockIllustration
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.SecondaryAction
import com.rodriguesacai.entregador.ui.components.UpInfoBox
import com.rodriguesacai.entregador.ui.components.UpLogo
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun CreatePasswordScreen(message: String?, error: String?, onBack: () -> Unit, onCreate: (String, String) -> Unit) {
    var cpf by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val checks = listOf(
        "Mínimo de 8 caracteres" to (pass.length >= 8),
        "Pelo menos uma letra maiúscula" to pass.any { it.isUpperCase() },
        "Pelo menos uma letra minúscula" to pass.any { it.isLowerCase() },
        "Pelo menos um número" to pass.any { it.isDigit() },
        "Confirmação igual" to (pass.isNotBlank() && pass == confirm)
    )
    AuthCard {
        UpLogo()
        LockIllustration(Modifier.fillMaxWidth().heightIn(min = 135.dp, max = 175.dp))
        Text("Criar sua senha", color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 25.sp)
        Text("Defina uma senha segura para acessar sua conta de entregador.", color = UpColors.Text, textAlign = TextAlign.Center, fontSize = 14.sp)
        FormField(cpf, { cpf = it.filter(Char::isDigit).take(11) }, "CPF cadastrado", Icons.Rounded.Lock, KeyboardType.Number)
        FormField(pass, { pass = it }, "Nova senha", Icons.Rounded.Lock, KeyboardType.Password, password = true)
        FormField(confirm, { confirm = it }, "Confirmar senha", Icons.Rounded.Lock, KeyboardType.Password, password = true)
        Column(Modifier.fillMaxWidth()) { checks.forEach { (label, ok) -> Text("✓ $label", color = if (ok) UpColors.Green else UpColors.Muted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) } }
        if (!message.isNullOrBlank()) UpInfoBox("Senha", message, Icons.Rounded.CheckCircle, UpColors.Green, UpColors.GreenSoft)
        if (!error.isNullOrBlank()) UpInfoBox("Atenção", error, Icons.Rounded.Lock, UpColors.Red, UpColors.RedSoft)
        PrimaryAction("Salvar e continuar", enabled = checks.all { it.second }, onClick = { onCreate(cpf, pass) })
        SecondaryAction("Voltar ao login", onBack)
    }
}
