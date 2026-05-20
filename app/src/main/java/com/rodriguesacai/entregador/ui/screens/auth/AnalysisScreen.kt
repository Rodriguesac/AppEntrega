package com.rodriguesacai.entregador.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.ui.components.AnalysisIllustration
import com.rodriguesacai.entregador.ui.components.AuthCard
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.SecondaryAction
import com.rodriguesacai.entregador.ui.components.UpInfoBox
import com.rodriguesacai.entregador.ui.components.UpLogo
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun AnalysisScreen(onBack: () -> Unit) {
    AuthCard {
        UpLogo()
        AnalysisIllustration(Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 210.dp))
        Text("Cadastro em análise", color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 25.sp)
        Text("Recebemos seus dados e nossa equipe está analisando as informações. Você receberá atualização quando a operação aprovar ou solicitar ajuste.", color = UpColors.Text, textAlign = TextAlign.Center, fontSize = 14.sp, lineHeight = 20.sp)
        UpInfoBox("Status do cadastro", "Pendente de aprovação", Icons.Rounded.AccessTime, UpColors.Green, UpColors.GreenSoft)
        PrimaryAction("Atualizar status", onClick = { }, icon = Icons.Rounded.Refresh)
        SecondaryAction("Voltar ao login", onBack)
    }
}
