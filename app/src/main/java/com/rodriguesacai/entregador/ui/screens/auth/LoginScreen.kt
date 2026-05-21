package com.rodriguesacai.entregador.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.ui.components.AuthCard
import com.rodriguesacai.entregador.ui.components.AppVersionChip
import com.rodriguesacai.entregador.ui.components.FormField
import com.rodriguesacai.entregador.ui.components.LoginMotoIllustration
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.SecondaryAction
import com.rodriguesacai.entregador.ui.components.UpInfoBox
import com.rodriguesacai.entregador.ui.components.UpLogo
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun LoginScreen(
    loading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onCadastro: () -> Unit,
    onCriarSenha: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    AuthCard {
        Spacer(Modifier.height(8.dp))
        UpLogo()
        AppVersionChip()
        Spacer(Modifier.height(10.dp))
        Text("Bem-vindo(a)!", color = UpColors.Ink, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Text("Acesse sua conta para continuar fazendo entregas com a Up.", color = UpColors.Text, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FormField(identifier, { identifier = it }, "CPF ou telefone", Icons.Rounded.Person, KeyboardType.Number)
            FormField(password, { password = it }, "Senha", Icons.Rounded.Lock, KeyboardType.Password, password = true)
        }
        if (!error.isNullOrBlank()) UpInfoBox("Não foi possível entrar", error, Icons.Rounded.Lock, UpColors.Red, UpColors.RedSoft)
        if (loading) CircularProgressIndicator(color = UpColors.Green)
        PrimaryAction("Entrar", enabled = !loading, onClick = { onLogin(identifier, password) })
        SecondaryAction("Solicitar cadastro", onCadastro, icon = Icons.Rounded.PersonAdd)
        androidx.compose.material3.TextButton(onClick = onCriarSenha) {
            Text("Esqueci minha senha", color = UpColors.Green, fontWeight = FontWeight.Bold)
        }
        LoginMotoIllustration(Modifier.fillMaxWidth().heightIn(min = 135.dp, max = 180.dp).padding(top = 8.dp))
    }
}
