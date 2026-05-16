package com.rodriguesacai.entregador.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodriguesacai.entregador.data.DriverStep
import com.rodriguesacai.entregador.data.RideOffer
import com.rodriguesacai.entregador.ui.theme.RodriguesTheme

@Composable
fun RodriguesDriverApp(vm: DriverViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    RodriguesTheme(dark = state.isDark) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 14.dp, vertical = 10.dp)) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when (state.activeTab) {
                        "home" -> HomeScreen(state.isDark, state.step, state.isAvailable, state.activeRide, state.error, { vm.setAvailable(true) }, { vm.setAvailable(false) }, { vm.setTab("route") })
                        "route" -> RouteScreen(state.activeRide, state.step) { vm.nextStep() }
                        "earnings" -> EarningsScreen(state.earningsVisible) { vm.toggleEarnings() }
                        "history" -> HistoryScreen()
                        "profile" -> ProfileScreen(state.isDark) { vm.toggleTheme() }
                    }
                }
                BottomNav(state.activeTab) { vm.setTab(it) }
            }
            state.pendingOffer?.let { OfferOverlay(it, { vm.acceptOffer() }, { vm.rejectOffer() }) }
        }
    }
}

@Composable
fun HomeScreen(isDark: Boolean, step: DriverStep, isAvailable: Boolean, activeRide: RideOffer?, error: String?, onAvailable: () -> Unit, onUnavailable: () -> Unit, onGoRoute: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Header(step, isDark)
        CleanMapCard(if (activeRide != null) "Corrida ativa" else "Aguardando corridas", activeRide?.let { "#${it.pedidoNumero} • ${it.bairro.ifBlank { it.endereco }}" } ?: "Fique disponível para receber ofertas da Central.")
        if (activeRide != null) PrimaryAction("Ver corrida ativa", Color(0xFF2563EB), onGoRoute) else if (isAvailable) PrimaryAction("Ficar indisponível", Color(0xFF111827), onUnavailable) else PrimaryAction("Ficar disponível", Color(0xFF16A34A), onAvailable)
        SmallOperationalSummary(error)
    }
}

@Composable
fun Header(step: DriverStep, isDark: Boolean) {
    val statusColor = when (step) { DriverStep.DISPONIVEL -> Color(0xFF16A34A); DriverStep.RESTRICAO -> Color(0xFFDC2626); DriverStep.INDISPONIVEL -> Color(0xFF374151); DriverStep.EM_OFERTA -> Color(0xFFEF233C); else -> Color(0xFF2563EB) }
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(26.dp), tonalElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(if (isDark) Color(0xFF1F2937) else Color(0xFFE5E7EB)), contentAlignment = Alignment.Center) { Text("D", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface) }
                Spacer(Modifier.width(12.dp))
                Column { Text("Olá, Diego", fontWeight = FontWeight.Black); Text("Rodrigues Açaí e Cia.", color = Color(0xFF6B7280)) }
            }
            Surface(color = statusColor, shape = RoundedCornerShape(99.dp)) { Text(statusLabel(step), modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}
fun statusLabel(step: DriverStep): String = when (step) { DriverStep.INDISPONIVEL -> "Indisponível"; DriverStep.DISPONIVEL -> "Disponível"; DriverStep.RESTRICAO -> "Restrição"; DriverStep.EM_OFERTA -> "Oferta"; DriverStep.INDO_COLETA -> "Coleta"; DriverStep.CHEGUEI_COLETA -> "Na loja"; DriverStep.PEDIDO_RETIRADO -> "Retirado"; DriverStep.INDO_ENTREGA -> "Entrega"; DriverStep.FINALIZADO -> "Finalizado" }

@Composable
fun CleanMapCard(title: String, subtitle: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(30.dp), tonalElevation = 1.dp) {
        Box(Modifier.fillMaxWidth().height(310.dp).padding(18.dp)) {
            Column { Text(title, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge); Text(subtitle, color = Color(0xFF6B7280)) }
            Box(Modifier.align(Alignment.Center).fillMaxWidth().height(120.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFE5E7EB).copy(alpha = 0.22f)), contentAlignment = Alignment.Center) { Text("Mapa / rota", color = Color(0xFF6B7280), fontWeight = FontWeight.Bold) }
            Row(Modifier.align(Alignment.BottomCenter), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MiniPill("Loja", "Rodrigues"); MiniPill("Atualização", "30s"); MiniPill("Navegação", "Maps") }
        }
    }
}
@Composable fun MiniPill(label: String, value: String) { Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(label, color = Color(0xFF6B7280), style = MaterialTheme.typography.labelSmall); Text(value, fontWeight = FontWeight.Black) } } }
@Composable fun PrimaryAction(text: String, color: Color, onClick: () -> Unit) { Button(onClick, modifier = Modifier.fillMaxWidth().height(62.dp), shape = RoundedCornerShape(22.dp), colors = ButtonDefaults.buttonColors(containerColor = color)) { Text(text, fontWeight = FontWeight.Black) } }
@Composable fun SmallOperationalSummary(error: String?) { Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Operação", fontWeight = FontWeight.Black); Text("Notificações, localização e serviço ativo precisam estar permitidos.", color = Color(0xFF6B7280)); if (error != null) Text("Atenção: $error", color = Color(0xFFDC2626)) } } }

@Composable
fun OfferOverlay(offer: RideOffer, onAccept: () -> Unit, onReject: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xDD000000)).padding(18.dp), contentAlignment = Alignment.Center) {
        Surface(color = Color.White, shape = RoundedCornerShape(34.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nova corrida", color = Color(0xFFEF233C), fontWeight = FontWeight.Black)
                Text("R$ %.2f".format(offer.valorEntrega), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = Color(0xFF111827))
                Text("Pedido #${offer.pedidoNumero}", color = Color(0xFF111827), fontWeight = FontWeight.Bold)
                Text(offer.bairro.ifBlank { offer.endereco }, color = Color(0xFF4B5563))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { MiniOfferInfo("${offer.distanciaKm} km"); MiniOfferInfo("${offer.tempoMin} min"); MiniOfferInfo(offer.formaPagamento.ifBlank { "Pagamento" }) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onReject, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151))) { Text("Rejeitar") }
                    Button(onAccept, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))) { Text("Aceitar") }
                }
            }
        }
    }
}
@Composable fun MiniOfferInfo(text: String) { Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(99.dp)) { Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color(0xFF111827), fontWeight = FontWeight.Bold) } }

@Composable
fun RouteScreen(offer: RideOffer?, step: DriverStep, onNext: () -> Unit) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Header(step, isDark = false)
        if (offer == null) { CleanMapCard("Nenhuma corrida ativa", "Aceite uma corrida para iniciar a rota."); return }
        CleanMapCard(if (step == DriverStep.INDO_COLETA || step == DriverStep.CHEGUEI_COLETA) "Ir para coleta" else "Ir para entrega", if (step == DriverStep.INDO_COLETA || step == DriverStep.CHEGUEI_COLETA) "Rodrigues Açaí e Cia." else offer.enderecoCompleto.ifBlank { offer.endereco })
        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Pedido #${offer.pedidoNumero}", fontWeight = FontWeight.Black); Text(offer.itens.ifBlank { "Itens do pedido" }, color = Color(0xFF6B7280)); Text("Valor da corrida: R$ %.2f".format(offer.valorEntrega), fontWeight = FontWeight.Bold) } }
        Button(onClick = { val destino = if (step == DriverStep.INDO_COLETA || step == DriverStep.CHEGUEI_COLETA) "Rodrigues Açaí e Cia Campo Grande" else offer.enderecoCompleto.ifBlank { offer.endereco }; context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${Uri.encode(destino)}"))) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) { Text("Iniciar navegação", fontWeight = FontWeight.Black) }
        PrimaryAction(nextLabel(step), Color(0xFF16A34A), onNext)
    }
}
fun nextLabel(step: DriverStep): String = when (step) { DriverStep.INDO_COLETA -> "Cheguei na coleta"; DriverStep.CHEGUEI_COLETA -> "Pedido retirado"; DriverStep.PEDIDO_RETIRADO -> "Ir para entrega"; DriverStep.INDO_ENTREGA -> "Finalizar entrega"; else -> "Continuar" }

@Composable fun EarningsScreen(visible: Boolean, onToggleVisible: () -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Ganhos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black); IconButton(onToggleVisible) { Icon(if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff, null) } }; MetricCard("Hoje", if (visible) "R$ 126,00" else "••••"); MetricCard("Semana", if (visible) "R$ 482,00" else "••••"); MetricCard("Próximo repasse", if (visible) "Sexta-feira" else "••••") } }
@Composable fun MetricCard(label: String, value: String) { Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text(label, color = Color(0xFF6B7280)); Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) } } }
@Composable fun HistoryScreen() { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Histórico", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black); repeat(4) { index -> Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Entrega finalizada", fontWeight = FontWeight.Bold); Text("Pedido #10$index • Rodrigues", color = Color(0xFF6B7280)) }; Text("R$ 7,00", fontWeight = FontWeight.Black) } } } } }
@Composable fun ProfileScreen(isDark: Boolean, onToggleTheme: () -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { Header(DriverStep.DISPONIVEL, isDark); Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)) { Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Conta", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge); Text("Pix: configurar", color = Color(0xFF6B7280)); Text("Banco: configurar", color = Color(0xFF6B7280)); Text("Repasse: semanal", color = Color(0xFF6B7280)); Button(onToggleTheme, shape = RoundedCornerShape(18.dp)) { Icon(if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, null); Spacer(Modifier.width(8.dp)); Text(if (isDark) "Tema claro" else "Tema escuro") } } } } }
@Composable fun BottomNav(tab: String, onTab: (String) -> Unit) { NavigationBar(containerColor = MaterialTheme.colorScheme.surface) { val items = listOf("home" to (Icons.Rounded.Home to "Início"), "route" to (Icons.Rounded.Map to "Rota"), "earnings" to (Icons.Rounded.AccountBalanceWallet to "Ganhos"), "history" to (Icons.Rounded.History to "Histórico"), "profile" to (Icons.Rounded.Person to "Conta")); items.forEach { (id, pair) -> NavigationBarItem(selected = tab == id, onClick = { onTab(id) }, icon = { Icon(pair.first, pair.second) }, label = { Text(pair.second) }) } } }
