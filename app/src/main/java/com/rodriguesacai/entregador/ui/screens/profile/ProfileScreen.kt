package com.rodriguesacai.entregador.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.ui.components.AddressBlock
import com.rodriguesacai.entregador.ui.components.AppBottomBar
import com.rodriguesacai.entregador.ui.components.BasePage
import com.rodriguesacai.entregador.ui.components.DangerButton
import com.rodriguesacai.entregador.ui.components.OutlineAction
import com.rodriguesacai.entregador.ui.navigation.AppRoute

@Composable
fun ProfileScreen(
    driver: Driver?,
    onBack: () -> Unit,
    onPix: () -> Unit,
    onChange: () -> Unit,
    onLogout: () -> Unit,
    onNav: (AppRoute) -> Unit
) {
    BasePage("Perfil", "Conta real do entregador", onBack, bottomBar = { AppBottomBar(AppRoute.Perfil, onNav) }) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AddressBlock("Nome", driver?.nome.ifBlankFallback("Nome não carregado"), if (driver?.verificado == true) "Verificado profissional" else "Aguardando validação")
            AddressBlock("Telefone", driver?.telefone.ifBlankFallback("Não informado"), "Para alterar, envie solicitação ao gestor.")
            AddressBlock("E-mail", driver?.email.ifBlankFallback("Não informado"), "Para alterar, envie solicitação ao gestor.")
            AddressBlock("Recebimento", driver?.pixTipo.ifBlankFallback("Pix não cadastrado"), driver?.pixChave.ifBlankFallback("Sem chave Pix"))
            OutlineAction("Editar Pix/banco") { onPix() }
            OutlineAction("Solicitar alteração de telefone/e-mail") { onChange() }
            DangerButton("Sair da conta") { onLogout() }
        }
    }
}

private fun String?.ifBlankFallback(fallback: String): String = this?.takeIf { it.isNotBlank() } ?: fallback
