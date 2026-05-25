package com.rodriguesacai.entregador.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.core.AppTab
import com.rodriguesacai.entregador.core.AppViewModel
import com.rodriguesacai.entregador.core.Offer
import com.rodriguesacai.entregador.core.Ride
import com.rodriguesacai.entregador.core.UiState
import com.rodriguesacai.entregador.core.kmBr
import com.rodriguesacai.entregador.core.minBr
import com.rodriguesacai.entregador.core.moneyBr
import com.rodriguesacai.entregador.core.shortDate
import com.rodriguesacai.entregador.ui.AppBg
import com.rodriguesacai.entregador.ui.AppFont
import com.rodriguesacai.entregador.ui.AppGreen
import com.rodriguesacai.entregador.ui.AppGreenDark
import com.rodriguesacai.entregador.ui.AppGreenSoft
import com.rodriguesacai.entregador.ui.Border
import com.rodriguesacai.entregador.ui.BottomNav
import com.rodriguesacai.entregador.ui.CardWhite
import com.rodriguesacai.entregador.ui.Danger
import com.rodriguesacai.entregador.ui.EmptyState
import com.rodriguesacai.entregador.ui.InfoLine
import com.rodriguesacai.entregador.ui.Ink
import com.rodriguesacai.entregador.ui.MetricBlock
import com.rodriguesacai.entregador.ui.MiniMap
import com.rodriguesacai.entregador.ui.Muted
import com.rodriguesacai.entregador.ui.PremiumCard
import com.rodriguesacai.entregador.ui.PrimaryButton
import com.rodriguesacai.entregador.ui.SecondaryButton
import com.rodriguesacai.entregador.ui.SectionTitle
import com.rodriguesacai.entregador.ui.SoftLine
import com.rodriguesacai.entregador.ui.StatusChip
import com.rodriguesacai.entregador.ui.ThinDivider
import com.rodriguesacai.entregador.ui.Warning

@Composable
fun RodriguesDriverApp(viewModel: AppViewModel) {
    val state = viewModel.state
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .statusBarsPadding()
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (viewModel.selectedTab) {
                AppTab.Home -> HomeScreen(state, viewModel)
                AppTab.Mapa -> MapScreen(state, viewModel)
                AppTab.Corrida -> RideScreen(state, viewModel)
                AppTab.Ganhos -> EarningsScreen(state, viewModel)
                AppTab.Perfil -> ProfileScreen(state, viewModel)
                AppTab.Notificacoes -> NotificationsScreen(state, viewModel)
            }
        }
        BottomNav(viewModel.selectedTab, state.unreadCount) { viewModel.select(it) }
    }
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 18.dp),
        content = content
    )
}

@Composable
private fun HomeScreen(state: UiState, vm: AppViewModel) = ScreenColumn {
    HeaderHome(state, vm)
    Spacer(Modifier.height(16.dp))
    FirebaseBanner(state)
    if (state.lastError.isNotBlank()) {
        Spacer(Modifier.height(10.dp))
        ErrorBanner(state.lastError)
    }
    Spacer(Modifier.height(14.dp))
    TodaySummary(state, vm)
    Spacer(Modifier.height(14.dp))
    state.activeOffer?.let {
        OfferCard(it, onAccept = vm::acceptOffer, onReject = { vm.rejectOffer() })
        Spacer(Modifier.height(14.dp))
    }
    state.activeRide?.let {
        ActiveRideShortcut(it) { vm.select(AppTab.Corrida) }
        Spacer(Modifier.height(14.dp))
    }
    OperationsGrid(state, vm)
    Spacer(Modifier.height(14.dp))
    DeliveryCarousel()
}

@Composable
private fun HeaderHome(state: UiState, vm: AppViewModel) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(AppGreenSoft), contentAlignment = Alignment.Center) {
            Text(state.profile.nome.firstOrNull()?.uppercase() ?: "E", color = AppGreen, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Olá, ${state.profile.nome.split(' ').firstOrNull().orEmpty().ifBlank { "Entregador" }}", color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, maxLines = 1)
            Text(if (state.activeRide != null) "Corrida em andamento" else state.locationText, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(46.dp).clip(CircleShape).background(Color.White).border(1.dp, Border, CircleShape).clickable { vm.select(AppTab.Notificacoes) }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = if (state.unreadCount > 0) AppGreen else Muted, modifier = Modifier.size(24.dp))
            if (state.unreadCount > 0) Box(Modifier.size(9.dp).clip(CircleShape).background(Danger).align(Alignment.TopEnd))
        }
    }
    Spacer(Modifier.height(12.dp))
    PrimaryButton(
        text = if (state.profile.online) "Disponível para entregas" else "Indisponível",
        icon = Icons.Outlined.PowerSettingsNew,
        onClick = { vm.setAvailability(!state.profile.online) }
    )
}

@Composable
private fun FirebaseBanner(state: UiState) {
    val ok = state.firebaseReady
    PremiumCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(if (ok) AppGreenSoft else Color(0xFFFFF5E0)), contentAlignment = Alignment.Center) {
                Icon(if (ok) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber, contentDescription = null, tint = if (ok) AppGreen else Warning)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(if (ok) "Banco de dados conectado" else "Banco não configurado", color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text(state.firebaseMessage, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ErrorBanner(text: String) {
    PremiumCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = Danger)
            Spacer(Modifier.width(10.dp))
            Text(text, color = Danger, fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TodaySummary(state: UiState, vm: AppViewModel) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1.3f)) {
                Text("Ganhos de hoje", color = Muted, fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (state.profile.ocultarValores) "••••" else state.todayEarnings.moneyBr(), color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(if (state.profile.ocultarValores) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = null, tint = Muted, modifier = Modifier.size(22.dp))
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                MiniStat("Corridas", state.todayRides.toString())
                Spacer(Modifier.height(8.dp))
                MiniStat("Finalizadas", state.todayFinished.toString())
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp)
        Spacer(Modifier.width(8.dp))
        Text(value, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
private fun OfferCard(offer: Offer, onAccept: () -> Unit, onReject: () -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Nova corrida", "Oferta ativa enviada pelo Gestor")
            Spacer(Modifier.weight(1f))
            StatusChip(offer.prioridade)
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricBlock(Icons.Outlined.AttachMoney, "Corrida", offer.valorCorrida.moneyBr(), Modifier.weight(1f))
            MetricBlock(Icons.Outlined.LocationOn, "Distância", offer.distanciaKm.kmBr(), Modifier.weight(1f))
            MetricBlock(Icons.Outlined.Route, "Tempo", offer.tempoMin.minBr(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        PlaceCompact(Icons.Outlined.Storefront, offer.lojaNome, offer.lojaEndereco, "Coleta")
        Spacer(Modifier.height(8.dp))
        PlaceCompact(Icons.Outlined.Home, "Entrega", offer.clienteBairro, "Bairro")
        Spacer(Modifier.height(14.dp))
        Text("Pagamento: ${offer.pagamentoForma} • Receber: ${offer.valorReceberCliente.moneyBr()}", color = Muted, fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryButton("Recusar", Icons.Outlined.Close, onReject, Modifier.weight(1f))
            PrimaryButton(text = "Aceitar", icon = Icons.Outlined.Check, onClick = onAccept, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ActiveRideShortcut(ride: Ride, onOpen: () -> Unit) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(AppGreenSoft), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.DirectionsBike, contentDescription = null, tint = AppGreen)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Corrida em andamento", color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text("Pedido #${ride.pedidoId.ifBlank { ride.id.takeLast(5) }} • ${ride.statusHumano}", color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            }
            TextButton(onClick = onOpen) { Text("Abrir", color = AppGreen, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
private fun OperationsGrid(state: UiState, vm: AppViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        SmallAction(Icons.Outlined.Map, "Mapa", "GPS e rota", Modifier.weight(1f)) { vm.select(AppTab.Mapa) }
        SmallAction(Icons.Outlined.SupportAgent, "Suporte", "Ocorrência", Modifier.weight(1f)) { vm.select(AppTab.Corrida) }
    }
}

@Composable
private fun SmallAction(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    androidx.compose.material3.OutlinedButton(onClick = onClick, modifier = modifier.height(92.dp), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Border), colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = AppGreen)
            Spacer(Modifier.height(8.dp))
            Text(title, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DeliveryCarousel() {
    PremiumCard {
        Text("Central do entregador", color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text("Fique disponível só quando estiver pronto. As ofertas agora só aparecem quando o Gestor despachar corretamente.", color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun RideScreen(state: UiState, vm: AppViewModel) = ScreenColumn {
    var showOccurrence by remember { mutableStateOf(false) }
    var showFinish by remember { mutableStateOf(false) }
    RideHeader(state, vm)
    Spacer(Modifier.height(16.dp))
    state.activeOffer?.let {
        OfferCard(it, onAccept = vm::acceptOffer, onReject = { vm.rejectOffer() })
        Spacer(Modifier.height(14.dp))
    }
    val ride = state.activeRide
    if (ride == null) {
        EmptyState("Nenhuma corrida ativa", "Quando o Gestor enviar uma oferta válida, ela aparece aqui e também toca no app.", Icons.Outlined.DirectionsBike)
    } else {
        CurrentRideContent(ride, onAdvance = {
            if (ride.currentStage >= 3) showFinish = true else vm.advanceRide()
        }, onProblem = { showOccurrence = true })
    }
    if (showOccurrence && ride != null) OccurrenceDialog(onDismiss = { showOccurrence = false }, onConfirm = { reason, details -> vm.registerOccurrence(reason, details); showOccurrence = false })
    if (showFinish && ride != null) FinishDialog(onDismiss = { showFinish = false }, onConfirm = { code -> vm.finishRide(code); showFinish = false })
}

@Composable
private fun RideHeader(state: UiState, vm: AppViewModel) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = Ink, modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(10.dp))
        Text("Corrida atual", color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, modifier = Modifier.weight(1f))
        StatusChip(state.activeRide?.statusHumano ?: if (state.activeOffer != null) "Oferta recebida" else "Aguardando", state.activeRide?.status != "OCORRENCIA")
    }
    Spacer(Modifier.height(10.dp))
    Text("Entregador ${state.driverId.ifBlank { "não identificado" }}", color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp)
}

@Composable
private fun CurrentRideContent(ride: Ride, onAdvance: () -> Unit, onProblem: () -> Unit) {
    PremiumCard(padding = 0.dp) {
        MiniMap(modifier = Modifier.fillMaxWidth().height(218.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            MetricBlock(Icons.Outlined.AttachMoney, "Corrida", ride.valorCorrida.moneyBr(), Modifier.weight(1f))
            Box(Modifier.height(40.dp).width(1.dp).background(SoftLine))
            MetricBlock(Icons.Outlined.LocationOn, "Distância", ride.distanciaKm.kmBr(), Modifier.weight(1f))
            Box(Modifier.height(40.dp).width(1.dp).background(SoftLine))
            MetricBlock(Icons.Outlined.Route, "Tempo", ride.tempoMin.minBr(), Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(16.dp))
    PremiumCard {
        Text("Próxima ação", color = AppGreen, fontSize = 20.sp, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(7.dp))
        Text(ride.statusHumano, color = Ink, fontSize = 15.sp, fontFamily = AppFont, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(16.dp))
        RouteBox(ride)
        Spacer(Modifier.height(18.dp))
        Stepper(ride.currentStage)
        Spacer(Modifier.height(18.dp))
        PrimaryButton(text = ride.nextAction, icon = Icons.Outlined.CheckCircle, onClick = onAdvance)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryButton("Abrir navegação", Icons.Outlined.Navigation, {}, Modifier.weight(1f))
            SecondaryButton("Informar problema", Icons.Outlined.WarningAmber, onProblem, Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(12.dp))
    val pay = buildString {
        append("${ride.pagamentoStatus} • ${ride.pagamentoForma}")
        if (ride.valorReceberCliente > 0.0) append(" • Receber ${ride.valorReceberCliente.moneyBr()}")
        if (ride.precisaTroco) append(" • Troco para ${ride.trocoPara.moneyBr()}")
    }
    InfoLine(Icons.Outlined.AttachMoney, "Pagamento", pay)
    Spacer(Modifier.height(10.dp))
    InfoLine(Icons.Outlined.Description, "Observações", ride.observacoes)
    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Outlined.Security, contentDescription = null, tint = Muted, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(10.dp))
        Text("Se houver imprevisto, registre uma ocorrência para o gestor acompanhar.", color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun RouteBox(ride: Ride) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).border(1.dp, Border, RoundedCornerShape(20.dp)).background(Color.White).padding(14.dp)) {
        PlaceCompact(Icons.Outlined.Storefront, ride.lojaNome, ride.lojaEndereco, "Coleta")
        ThinDivider()
        PlaceCompact(Icons.Outlined.Home, "Cliente: ${ride.clienteNome}", ride.clienteEndereco.ifBlank { ride.clienteBairro }, "Entrega")
    }
}

@Composable
private fun PlaceCompact(icon: ImageVector, title: String, subtitle: String, chip: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(Modifier.size(46.dp).clip(CircleShape).background(if (chip == "Coleta") AppGreen else Color(0xFFF1F2F3)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = if (chip == "Coleta") Color.White else Ink, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(chip, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (chip == "Coleta") AppGreenSoft else Color(0xFFF1F2F3)).padding(horizontal = 10.dp, vertical = 6.dp), color = if (chip == "Coleta") AppGreenDark else Muted, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
    }
}

@Composable
private fun Stepper(current: Int) {
    val labels = listOf("Aceita", "Na coleta", "Retirado", "No cliente", "Finalizar")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(Modifier.size(if (index == current) 30.dp else 25.dp).clip(CircleShape).background(if (index < current) AppGreen else if (index == current) AppGreenSoft else Color.White).border(if (index == current) 4.dp else 2.dp, if (index <= current) AppGreen else Border, CircleShape), contentAlignment = Alignment.Center) {
                    if (index < current) Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.height(7.dp))
                Text(label, color = if (index == current) AppGreen else Muted, fontFamily = AppFont, fontWeight = if (index == current) FontWeight.ExtraBold else FontWeight.SemiBold, fontSize = 10.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun MapScreen(state: UiState, vm: AppViewModel) = ScreenColumn {
    SectionTitle("Mapa", if (state.activeRide == null) "Sua posição operacional e disponibilidade" else "Rota da corrida atual")
    Spacer(Modifier.height(14.dp))
    PremiumCard(padding = 0.dp) {
        MiniMap(modifier = Modifier.fillMaxWidth().height(420.dp), showPins = state.activeRide != null)
    }
    Spacer(Modifier.height(12.dp))
    InfoLine(Icons.Outlined.LocationOn, "Localização", state.locationText)
    Spacer(Modifier.height(10.dp))
    PrimaryButton(text = if (state.profile.online) "Disponível" else "Ficar disponível", icon = Icons.Outlined.PowerSettingsNew, onClick = { vm.setAvailability(true) })
}

@Composable
private fun EarningsScreen(state: UiState, vm: AppViewModel) = ScreenColumn {
    SectionTitle("Ganhos", "Resumo conectado às corridas finalizadas no Firebase")
    Spacer(Modifier.height(14.dp))
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Hoje", color = Muted, fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(if (state.profile.ocultarValores) "••••" else state.todayEarnings.moneyBr(), color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)
            }
            androidx.compose.material3.IconButton(onClick = { vm.setHideValues(!state.profile.ocultarValores) }) {
                Icon(if (state.profile.ocultarValores) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = null, tint = Muted)
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = SoftLine)
        Spacer(Modifier.height(12.dp))
        MiniStat("Corridas registradas", state.todayRides.toString())
        Spacer(Modifier.height(8.dp))
        MiniStat("Finalizadas", state.todayFinished.toString())
    }
    Spacer(Modifier.height(12.dp))
    InfoLine(Icons.Outlined.AccountBalanceWallet, "Repasse", "O acerto aparece conforme o Gestor gravar repassesEntregadores")
}

@Composable
private fun ProfileScreen(state: UiState, vm: AppViewModel) = ScreenColumn {
    var driverId by remember(state.driverId) { mutableStateOf(state.driverId) }
    var pix by remember(state.profile.pix) { mutableStateOf(state.profile.pix) }
    var banco by remember(state.profile.banco) { mutableStateOf(state.profile.banco) }
    SectionTitle("Perfil", "Configuração do entregador e conexão real com o Firebase")
    Spacer(Modifier.height(14.dp))
    PremiumCard {
        OutlinedTextField(value = driverId, onValueChange = { driverId = it }, label = { Text("UID/código do entregador") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        PrimaryButton(text = "Salvar e conectar", icon = Icons.Outlined.Check, onClick = { vm.setDriverId(driverId) })
    }
    Spacer(Modifier.height(12.dp))
    PremiumCard {
        Text("Recebimento", color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = pix, onValueChange = { pix = it }, label = { Text("Chave Pix") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = banco, onValueChange = { banco = it }, label = { Text("Banco") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        PrimaryButton(text = "Salvar Pix/banco", icon = Icons.Outlined.Check, onClick = { vm.updatePixBank(pix, banco) })
    }
    Spacer(Modifier.height(12.dp))
    InfoLine(Icons.Outlined.Lock, "Segurança", "Alterações ficam gravadas no documento do entregador")
}

@Composable
private fun NotificationsScreen(state: UiState, vm: AppViewModel) = ScreenColumn {
    SectionTitle("Notificações", "Avisos do Gestor e alertas operacionais")
    Spacer(Modifier.height(14.dp))
    if (state.notifications.isEmpty()) {
        EmptyState("Sem notificações", "Quando o Gestor enviar avisos, eles aparecem aqui.", Icons.Outlined.Notifications)
    } else {
        state.notifications.forEach { note ->
            PremiumCard {
                Row(verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(if (note.lida) Color(0xFFF1F2F3) else AppGreenSoft), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = if (note.lida) Muted else AppGreen)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(note.titulo, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(note.corpo, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(note.createdAt.shortDate(), color = Muted, fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    }
                    if (!note.lida) TextButton(onClick = { vm.markNotificationRead(note.id) }) { Text("Lida", color = AppGreen, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold) }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun OccurrenceDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var reason by remember { mutableStateOf("Cliente não atende") }
    var details by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Informar problema", fontFamily = AppFont, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("Detalhes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(reason, details) }) { Text("Registrar", color = AppGreen, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Voltar") } }
    )
}

@Composable
private fun FinishDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar entrega", fontFamily = AppFont, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                Text("Digite o código do cliente. Se não existir código no pedido, use liberação interna.", color = Muted, fontFamily = AppFont, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Código") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(code) }) { Text("Finalizar", color = AppGreen, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
