package com.rodriguesacai.entregador.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.ui.components.AlertBox
import com.rodriguesacai.entregador.ui.components.BrandLogo
import com.rodriguesacai.entregador.ui.components.PrimaryButton
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun LoginScreen(
    loading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onCadastro: () -> Unit,
    onCriarSenha: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(AppColors.Bg).padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            BrandLogo()
            Text("Bem-vindo(a)!", color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 25.sp)
            Text("Acesse sua conta para continuar fazendo entregas.", color = AppColors.Muted)

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it.filter(Char::isDigit).take(14) },
                        label = { Text("CPF ou telefone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )
                    OutlinedTextField(
                        value = senha,
                        onValueChange = { senha = it },
                        label = { Text("Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )
                    if (error != null) AlertBox(error, AppColors.Red)
                    if (loading) CircularProgressIndicator(color = AppColors.Green) else PrimaryButton("Entrar") { onLogin(id, senha) }
                    TextButton(onClick = onCadastro, modifier = Modifier.fillMaxWidth()) { Text("Solicitar cadastro", color = AppColors.Green, fontWeight = FontWeight.Bold) }
                    TextButton(onClick = onCriarSenha, modifier = Modifier.fillMaxWidth()) { Text("Criar primeira senha", color = AppColors.Ink) }
                }
            }
        }
    }
}
