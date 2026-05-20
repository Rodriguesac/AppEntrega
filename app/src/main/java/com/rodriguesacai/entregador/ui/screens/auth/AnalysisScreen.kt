package com.rodriguesacai.entregador.ui.screens.auth

import androidx.compose.runtime.Composable
import com.rodriguesacai.entregador.ui.screens.support.StatusPage
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun AnalysisScreen(onBack: () -> Unit) {
    StatusPage(
        title = "Cadastro em análise",
        message = "Recebemos seus dados. A operação precisa aprovar o cadastro antes de liberar corridas.",
        color = AppColors.Green,
        onBack = onBack
    )
}
