package com.rodriguesacai.entregador.ui.screens.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.ui.components.AlertBox
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.OutlineAction
import androidx.compose.ui.graphics.Color

@Composable
fun StatusPage(title: String, message: String, color: Color, onBack: () -> Unit) {
    BasePage(title, "Status operacional", onBack) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AlertBox(message, color)
            OutlineAction("Voltar") { onBack() }
        }
    }
}
