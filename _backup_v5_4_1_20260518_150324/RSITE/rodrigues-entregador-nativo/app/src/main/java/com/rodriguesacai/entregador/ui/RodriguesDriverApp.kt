package com.rodriguesacai.entregador.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodriguesacai.entregador.data.DriverStep
import com.rodriguesacai.entregador.data.DriverUiState
import com.rodriguesacai.entregador.data.RideOffer
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RodriguesDriverApp(viewModel: DriverViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            DriverBottomBar(
                active = state.activeTab,
                onSelect = viewModel::setTab
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.activeTab) {
                "route" -> RouteScreen(state, viewModel)
                "earnings" -> EarningsScreen(state, viewModel)
                "history" -> HistoryScreen(state)
                "profile" -> ProfileScreen(state, viewModel)
                else -> HomeScreen(state, viewModel)
            }

            if (state.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }

            state.pendingOffer?.let { offer ->
                OfferDialog(
                    offer = offer,
                    onAccept = viewModel::acceptOffer,
                    onReject = viewModel::rejectOffer
                )
            }

            state.error?.let {
                ErrorCard(it, Modifier.align(Alignment.TopCenter).padding(12.dp))
            }
        }
    }
}

@Composable
private fun HomeScreen(state: DriverUiState, viewModel: DriverViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DriverHeader(state)
        CleanMapPreview(
            title = if (state.isAvailable) "Aguardando corrida" else "Mapa operacional",
            subtitle = if (state.isAvailable) "Você está disponível para receber ofertas." else "Fique disponível para entrar no radar."
        )
        AvailabilityCard(state, viewModel)
        OperationCard(state)
    }
}

@Composable
private fun DriverHeader(state: DriverUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(54.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text("D", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Olá, Diego", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Rodrigues Açaí e Cia.", style = MaterialTheme.typography.bodyMedium)
        }
        StatusPill(state.step)
    }
}

@Composable
private fun StatusPill(step: DriverStep) {
    val label = when (step) {
        DriverStep.DISPONIVEL -> "Disponível"
        DriverStep.RESTRICAO -> "Restrição"
        DriverStep.EM_OFERTA -> "Oferta"
        DriverStep.INDO_COLETA, DriverStep.CHEGUEI_COLETA, DriverStep.PEDIDO_RETIRADO, DriverStep.INDO_ENTREGA -> "Em corrida"
        else -> "Indisponível"
    }
    AssistChip(onClick = {}, label = { Text(label) })
}

@Composable
private fun CleanMapPreview(title: String, subtitle: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(230.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant).padding(18.dp)
        ) {
            Column(Modifier.align(Alignment.TopStart)) {
                Icon(Icons.Rounded.Map, contentDescription = null)
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = {}, label = { Text("Loja") }, leadingIcon = { Icon(Icons.Rounded.Store, null) })
                AssistChip(onClick = {}, label = { Text("Rota") }, leadingIcon = { Icon(Icons.Rounded.Navigation, null) })
            }
        }
    }
}

@Composable
private fun AvailabilityCard(state: DriverUiState, viewModel: DriverViewModel) {
    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.PowerSettingsNew, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (state.isAvailable) "Disponível para entregas" else "Indisponível",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Toque para entrar ou sair do radar.", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = state.isAvailable,
                onCheckedChange = viewModel::setAvailable
            )
        }
    }
}

@Composable
private fun OperationCard(state: DriverUiState) {
    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumo rápido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ganhos hoje")
                Text(money(state.ganhosHoje), fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Corridas hoje")
                Text("${state.corridasHoje}", fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Atualização")
                Text("30s", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RouteScreen(state: DriverUiState, viewModel: DriverViewModel) {
    val ride = state.activeRide
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Mapa da rota", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (ride == null) {
            CleanMapPreview("Nenhuma corrida ativa", "Aceite uma oferta para visualizar a rota.")
            Text("Quando uma corrida for aceita, esta tela abre automaticamente.")
        } else {
            CleanMapPreview(
                title = routeTitle(state.step),
                subtitle = "${ride.distanciaKm} km • ${ride.tempoMin} min • ${money(ride.valorEntrega)}"
            )
            RideDetails(ride)
            RouteActions(state, viewModel)
        }
    }
}

@Composable
private fun RideDetails(ride: RideOffer) {
    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoLine(Icons.Rounded.Store, "Coleta", ride.lojaNome)
            InfoLine(Icons.Rounded.LocationOn, "Entrega", ride.enderecoCompleto.ifBlank { ride.bairro })
            InfoLine(Icons.Rounded.Payments, "Pagamento", ride.formaPagamento.ifBlank { "Não informado" })
            if (ride.itens.isNotBlank()) {
                Text(ride.itens, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RouteActions(state: DriverUiState, viewModel: DriverViewModel) {
    val context = LocalContext.current
    val ride = state.activeRide ?: return
    val destination = when (state.step) {
        DriverStep.INDO_COLETA, DriverStep.CHEGUEI_COLETA -> "Rodrigues Açaí e Cia Campo Grande MS"
        else -> ride.enderecoCompleto.ifBlank { ride.bairro }
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = {
                val uri = Uri.parse("google.navigation:q=${Uri.encode(destination)}")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.Navigation, null)
            Spacer(Modifier.width(6.dp))
            Text("Navegar")
        }
        Button(
            onClick = viewModel::nextStep,
            modifier = Modifier.weight(1f)
        ) {
            Text(nextButtonLabel(state.step))
        }
    }
}

@Composable
private fun EarningsScreen(state: DriverUiState, viewModel: DriverViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ganhos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = viewModel::toggleEarningsVisibility) {
                Icon(if (state.earningsVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff, null)
            }
        }
        BalanceCard("Hoje", state.ganhosHoje, state.earningsVisible)
        BalanceCard("Semana", state.ganhosHoje + 84.0, state.earningsVisible)
        BalanceCard("Próximo repasse", state.ganhosHoje + 120.0, state.earningsVisible)
    }
}

@Composable
private fun BalanceCard(label: String, value: Double, visible: Boolean) {
    ElevatedCard(shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(if (visible) money(value) else "••••", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HistoryScreen(state: DriverUiState) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Histórico", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (state.corridasHoje == 0) {
            Text("Nenhuma corrida finalizada nesta sessão.")
        } else {
            repeat(state.corridasHoje) { index ->
                ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Corrida #${index + 1}")
                        Text("Finalizada")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(state: DriverUiState, viewModel: DriverViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Perfil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        ElevatedCard(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        Text("D", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Diego", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Entregador verificado")
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Tema escuro")
                    Switch(checked = state.isDark, onCheckedChange = { viewModel.toggleTheme() })
                }
                Text("Pix e dados bancários devem ser controlados pelo painel gestor.")
            }
        }
    }
}

@Composable
private fun OfferDialog(offer: RideOffer, onAccept: () -> Unit, onReject: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        icon = { Icon(Icons.Rounded.DeliveryDining, contentDescription = null) },
        title = { Text("Nova corrida") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(money(offer.valorEntrega), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text("Pedido ${offer.pedidoNumero.ifBlank { offer.id.take(6) }}")
                Text("${offer.bairro} • ${offer.distanciaKm} km • ${offer.tempoMin} min")
                Text("Pagamento: ${offer.formaPagamento.ifBlank { "não informado" }}")
                if (offer.itens.isNotBlank()) Text(offer.itens)
            }
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text("Aceitar")
            }
        },
        dismissButton = {
            TextButton(onClick = onReject) {
                Text("Rejeitar")
            }
        }
    )
}

@Composable
private fun ErrorCard(message: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Warning, null)
            Spacer(Modifier.width(8.dp))
            Text(message)
        }
    }
}

@Composable
private fun DriverBottomBar(active: String, onSelect: (String) -> Unit) {
    val items = listOf(
        BottomItem("home", "Início", Icons.Rounded.Home),
        BottomItem("route", "Rota", Icons.Rounded.Map),
        BottomItem("earnings", "Ganhos", Icons.Rounded.Payments),
        BottomItem("history", "Histórico", Icons.Rounded.History),
        BottomItem("profile", "Perfil", Icons.Rounded.Person)
    )
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = active == item.id,
                onClick = { onSelect(item.id) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

private data class BottomItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

private fun routeTitle(step: DriverStep): String = when (step) {
    DriverStep.INDO_COLETA -> "Indo para a loja"
    DriverStep.CHEGUEI_COLETA -> "Na coleta"
    DriverStep.PEDIDO_RETIRADO -> "Pedido retirado"
    DriverStep.INDO_ENTREGA -> "Indo para entrega"
    else -> "Corrida ativa"
}

private fun nextButtonLabel(step: DriverStep): String = when (step) {
    DriverStep.INDO_COLETA -> "Cheguei na coleta"
    DriverStep.CHEGUEI_COLETA -> "Pedido retirado"
    DriverStep.PEDIDO_RETIRADO -> "Ir para entrega"
    DriverStep.INDO_ENTREGA -> "Finalizar"
    else -> "Avançar"
}

private fun money(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
}
