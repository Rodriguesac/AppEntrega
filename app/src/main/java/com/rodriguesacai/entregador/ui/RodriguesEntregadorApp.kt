package com.rodriguesacai.entregador.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.data.FirebaseRepository
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.service.DriverLocationService
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private val Bg = Color(0xFFF5F7FA)
private val Ink = Color(0xFF0B1220)
private val Muted = Color(0xFF667085)
private val Line = Color(0xFFE5E7EB)
private val Green = Color(0xFF12B76A)
private val DarkGreen = Color(0xFF067647)
private val Red = Color(0xFFE53935)
private val Yellow = Color(0xFFFFB020)
private val Purple = Color(0xFF3F1D70)

private enum class Screen { Login, Cadastro, Analise, CriarSenha, Home, Permissoes, CorridaUrgente, CorridaAndamento, Mapa, Historico, Ganhos, Carteira, Notificacoes, Perfil, PixBanco, Solicitacao, Ocorrencia, Atualizacao, SemInternet, Manutencao, ErroFirebase }

@Composable
fun RodriguesEntregadorApp(vm: DriverViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var screen by remember { mutableStateOf(if (state.logged) Screen.Home else Screen.Login) }
    var selectedRide by remember { mutableStateOf<Ride?>(null) }
    val context = LocalContext.current

    LaunchedEffect(state.logged) {
        if (state.logged && screen == Screen.Login) screen = Screen.Home
        if (!state.logged) screen = Screen.Login
    }

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            when (screen) {
                Screen.Login -> LoginScreen(
                    loading = state.loading,
                    error = state.error,
                    onLogin = vm::login,
                    onDemo = vm::enterDemo,
                    onCadastro = { screen = Screen.Cadastro },
                    onCriarSenha = { screen = Screen.CriarSenha }
                )
                Screen.Cadastro -> CadastroScreen(onBack = { screen = Screen.Login }, onFinish = { screen = Screen.Analise })
                Screen.Analise -> AnaliseScreen(onBack = { screen = Screen.Login })
                Screen.CriarSenha -> CriarSenhaScreen(onBack = { screen = Screen.Login })
                Screen.Home -> HomeScreen(
                    state = state,
                    onOnline = { online ->
                        vm.setOnline(online)
                        if (online) startLocationService(context) else stopLocationService(context)
                    },
                    onPermissions = { screen = Screen.Permissoes },
                    onUrgent = { ride -> selectedRide = ride; screen = Screen.CorridaUrgente },
                    onRide = { ride -> selectedRide = ride; screen = Screen.CorridaAndamento },
                    onNav = { screen = it },
                    onLogout = vm::logout,
                    onToggleValues = vm::toggleValues
                )
                Screen.Permissoes -> PermissionsScreen(onBack = { screen = Screen.Home })
                Screen.CorridaUrgente -> UrgentRideScreen(
                    ride = selectedRide ?: state.activeRides.firstOrNull { it.status == "OFERTA_RECEBIDA" },
                    onBack = { screen = Screen.Home },
                    onAccept = { id -> vm.acceptRide(id); screen = Screen.CorridaAndamento },
                    onReject = { id, motivo -> vm.rejectRide(id, motivo); screen = Screen.Home }
                )
                Screen.CorridaAndamento -> ActiveRideScreen(
                    ride = selectedRide ?: state.activeRides.firstOrNull { it.status != "OFERTA_RECEBIDA" },
                    onBack = { screen = Screen.Home },
                    onMap = { screen = Screen.Mapa },
                    onAdvance = vm::advanceRide,
                    onOccurrence = { ride -> selectedRide = ride; screen = Screen.Ocorrencia }
                )
                Screen.Mapa -> MapRouteScreen(ride = selectedRide ?: state.activeRides.firstOrNull(), onBack = { screen = Screen.CorridaAndamento })
                Screen.Historico -> HistoryScreen(state.history, onBack = { screen = Screen.Home })
                Screen.Ganhos -> EarningsScreen(state.driver, state.history, onBack = { screen = Screen.Home }, onWallet = { screen = Screen.Carteira }, onToggleValues = vm::toggleValues)
                Screen.Carteira -> WalletScreen(state.driver, onBack = { screen = Screen.Ganhos }, onPix = { screen = Screen.PixBanco })
                Screen.Notificacoes -> NotificationsScreen(state.notifications, onBack = { screen = Screen.Home })
                Screen.Perfil -> ProfileScreen(state.driver, onBack = { screen = Screen.Home }, onPix = { screen = Screen.PixBanco }, onChange = { screen = Screen.Solicitacao }, onLogout = vm::logout)
                Screen.PixBanco -> PixBankScreen(state.driver, onBack = { screen = Screen.Perfil }, onSave = vm::savePix)
                Screen.Solicitacao -> ChangeRequestScreen(onBack = { screen = Screen.Perfil }, onSend = vm::requestChange)
                Screen.Ocorrencia -> OccurrenceScreen(ride = selectedRide ?: state.activeRides.firstOrNull(), onBack = { screen = Screen.CorridaAndamento }, onSend = vm::createOccurrence)
                Screen.Atualizacao -> StatusPage("Atualização do app", "Quando houver nova versão obrigatória, esta tela bloqueia o uso até atualizar.", Green, onBack = { screen = Screen.Home })
                Screen.SemInternet -> StatusPage("Sem internet", "Conexão indisponível. O app mantém a tela limpa e tenta recuperar os dados.", Red, onBack = { screen = Screen.Home })
                Screen.Manutencao -> StatusPage("Manutenção", "Operação pausada temporariamente pelo gestor.", Purple, onBack = { screen = Screen.Home })
                Screen.ErroFirebase -> StatusPage("Erro Firebase", state.error ?: "Falha ao sincronizar dados reais.", Red, onBack = { screen = Screen.Home })
            }
        }
    }
}

@Composable
private fun LoginScreen(
    loading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onDemo: () -> Unit,
    onCadastro: () -> Unit,
    onCriarSenha: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0B1220), Color(0xFF153B2D), Bg)))
            .padding(22.dp)
    ) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark()
            Spacer(Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Entrar", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
                    Text("Use CPF ou telefone cadastrado para receber corridas em tempo real.", color = Muted, fontSize = 13.sp)
                    Field(id, { id = it }, "CPF ou telefone", KeyboardType.Phone)
                    Field(senha, { senha = it }, "Senha", KeyboardType.Password, password = true)
                    if (!error.isNullOrBlank()) AlertBox(error, Red)
                    PrimaryButton(if (loading) "Entrando..." else "Entrar no app") { onLogin(id, senha) }
                    TextButton(onClick = onCriarSenha, modifier = Modifier.fillMaxWidth()) { Text("Criar ou recuperar senha", color = DarkGreen, fontWeight = FontWeight.Bold) }
                    Divider(color = Line)
                    OutlineButton("Cadastrar entregador") { onCadastro() }
                    TextButton(onClick = onDemo, modifier = Modifier.fillMaxWidth()) { Text("Entrar em homologação", color = Muted, fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable
private fun CadastroScreen(onBack: () -> Unit, onFinish: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    BasePage("Cadastro", "Dados do entregador", onBack) {
        ProgressSteps(1, 4)
        Field(nome, { nome = it }, "Nome completo")
        Field(cpf, { cpf = it }, "CPF", KeyboardType.Number)
        Field(telefone, { telefone = it }, "Telefone", KeyboardType.Phone)
        Field(placa, { placa = it.uppercase() }, "Placa da moto")
        UploadCard("Documento", "Foto legível do documento ou CNH")
        UploadCard("Selfie", "Foto nítida do rosto para aprovação")
        PrimaryButton("Enviar cadastro para análise") { onFinish() }
    }
}

@Composable
private fun AnaliseScreen(onBack: () -> Unit) = StatusPage(
    title = "Cadastro em análise",
    message = "Seu cadastro foi recebido. Assim que o gestor aprovar, o acesso às corridas será liberado.",
    color = Green,
    onBack = onBack
)

@Composable
private fun CriarSenhaScreen(onBack: () -> Unit) {
    var doc by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    BasePage("Criar senha", "Primeiro acesso", onBack) {
        ProgressSteps(3, 4)
        Field(doc, { doc = it }, "CPF ou telefone", KeyboardType.Phone)
        Field(senha, { senha = it }, "Nova senha", KeyboardType.Password, password = true)
        PasswordChecklist(senha)
        PrimaryButton("Salvar senha") { onBack() }
    }
}

@Composable
private fun HomeScreen(
    state: DriverUiState,
    onOnline: (Boolean) -> Unit,
    onPermissions: () -> Unit,
    onUrgent: (Ride) -> Unit,
    onRide: (Ride) -> Unit,
    onNav: (Screen) -> Unit,
    onLogout: () -> Unit,
    onToggleValues: (Boolean) -> Unit
) {
    val driver = state.driver
    val urgent = state.activeRides.firstOrNull { it.status == "OFERTA_RECEBIDA" }
    val active = state.activeRides.firstOrNull { it.status != "OFERTA_RECEBIDA" }
    Scaffold(
        bottomBar = { BottomBar(onNav) },
        containerColor = Bg
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Header(driver, onLogout)
            }
            item {
                StatusSwitch(driver, onOnline)
            }
            if (urgent != null) item { UrgentCard(urgent) { onUrgent(urgent) } }
            if (active != null) item { ActiveRideCard(active, onOpen = { onRide(active) }, onMap = { onRide(active); onNav(Screen.Mapa) }) }
            item {
                EarningsCompact(driver, onToggleValues)
            }
            item {
                MiniMapCard(active, onOpen = { if (active != null) onRide(active) else onPermissions() })
            }
            item {
                QuickGrid(
                    listOf(
                        "Permissões" to Screen.Permissoes,
                        "Histórico" to Screen.Historico,
                        "Notificações" to Screen.Notificacoes,
                        "Carteira" to Screen.Carteira,
                        "Atualização" to Screen.Atualizacao,
                        "Sem internet" to Screen.SemInternet,
                        "Manutenção" to Screen.Manutencao,
                        "Firebase" to Screen.ErroFirebase
                    ),
                    onNav
                )
            }
        }
    }
}

@Composable
private fun PermissionsScreen(onBack: () -> Unit) {
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val locLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    BasePage("Permissões", "Checklist inicial", onBack) {
        PermissionRow("Notificações", "Receber corrida urgente", Green) {
            if (Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        PermissionRow("Localização", "Atualizar rota durante corrida", Green) {
            locLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
        PermissionRow("Tela cheia", "Permitir alerta urgente sobre a tela bloqueada", Yellow) {}
        PermissionRow("Bateria", "Evitar restrição do Android durante operação", Yellow) {}
    }
}

@Composable
private fun UrgentRideScreen(ride: Ride?, onBack: () -> Unit, onAccept: (String) -> Unit, onReject: (String, String) -> Unit) {
    if (ride == null) {
        StatusPage("Sem corrida urgente", "Nenhuma oferta ativa no momento.", Green, onBack)
        return
    }
    Box(Modifier.fillMaxSize().background(Ink).padding(18.dp)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("CORRIDA RECEBIDA", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Card(shape = RoundedCornerShape(34.dp), colors = CardDefaults.cardColors(Color.White)) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Pedido #${ride.numeroPedido}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Metric("Valor", money(ride.valorCorrida), Green, Modifier.weight(1f))
                        Metric("Distância", "${ride.distanciaKm.format1()} km", Ink, Modifier.weight(1f))
                    }
                    AddressBlock("Coleta", ride.lojaNome, ride.lojaEndereco)
                    AddressBlock("Entrega", ride.clienteBairro, "Endereço completo liberado após aceite/coleta")
                    PrimaryButton("Aceitar corrida") { onAccept(ride.id) }
                    DangerButton("Recusar") { onReject(ride.id, "Recusada pelo entregador") }
                }
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar", color = Color.White) }
        }
    }
}

@Composable
private fun ActiveRideScreen(ride: Ride?, onBack: () -> Unit, onMap: () -> Unit, onAdvance: (Ride) -> Unit, onOccurrence: (Ride) -> Unit) {
    if (ride == null) {
        StatusPage("Nenhuma corrida", "Você ainda não tem corrida em andamento.", Green, onBack)
        return
    }
    BasePage("Corrida em andamento", humanStatus(ride.status), onBack) {
        NativeMapPreview(ride, Modifier.height(230.dp).fillMaxWidth().clip(RoundedCornerShape(28.dp)).clickable { onMap() })
        AddressBlock("Coleta", ride.lojaNome, ride.lojaEndereco)
        val deliveryAddress = if (ride.status in listOf("PEDIDO_RETIRADO", "INDO_ENTREGA", "ENTREGADOR_NO_LOCAL", "OCORRENCIA")) ride.clienteEnderecoCompleto else "${ride.clienteBairro} • endereço completo após retirada"
        AddressBlock("Entrega", ride.clienteNome, deliveryAddress)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("Valor", money(ride.valorCorrida), Green, Modifier.weight(1f))
            Metric("Tempo", "${ride.tempoEstimadoMin} min", Ink, Modifier.weight(1f))
            Metric("KM", ride.distanciaKm.format1(), Ink, Modifier.weight(1f))
        }
        PrimaryButton(nextActionText(ride.status)) { onAdvance(ride) }
        OutlineButton("Abrir mapa maior") { onMap() }
        if (ride.status in listOf("INDO_ENTREGA", "ENTREGADOR_NO_LOCAL")) DangerButton("Registrar ocorrência") { onOccurrence(ride) }
    }
}

@Composable
private fun MapRouteScreen(ride: Ride?, onBack: () -> Unit) {
    BasePage("Mapa/rota", "Rota limpa, sem cards sobre o mapa", onBack) {
        if (ride == null) {
            MiniMapDrawing(Modifier.height(420.dp).fillMaxWidth())
        } else {
            NativeMapPreview(ride, Modifier.height(480.dp).fillMaxWidth().clip(RoundedCornerShape(30.dp)))
            PrimaryButton("Iniciar navegação") {
                // Link externo será plugado com Google Maps/Waze conforme preferência do entregador.
            }
        }
    }
}

@Composable
private fun HistoryScreen(history: List<Ride>, onBack: () -> Unit) = BasePage("Histórico", "Uma corrida por linha", onBack) {
    if (history.isEmpty()) EmptyCard("Nenhuma corrida no histórico ainda.")
    history.forEach { ride ->
        CardLine(
            title = "Pedido #${ride.numeroPedido}",
            subtitle = humanStatus(ride.status),
            trailing = money(ride.valorCorrida),
            color = statusColor(ride.status)
        )
    }
}

@Composable
private fun EarningsScreen(driver: Driver?, history: List<Ride>, onBack: () -> Unit, onWallet: () -> Unit, onToggleValues: (Boolean) -> Unit) = BasePage("Ganhos", "Hoje, semana e mês", onBack) {
    EarningsCompact(driver, onToggleValues)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Metric("Corridas", "${driver?.corridasHoje ?: 0}", Ink, Modifier.weight(1f))
        Metric("Finalizadas", "${history.count { it.status == "FINALIZADA" }}", Green, Modifier.weight(1f))
    }
    PrimaryButton("Ver carteira/repasse") { onWallet() }
}

@Composable
private fun WalletScreen(driver: Driver?, onBack: () -> Unit, onPix: () -> Unit) = BasePage("Carteira/repasse", "Recebimentos", onBack) {
    Metric("Próximo repasse", if (driver?.ocultarValores == true) "••••" else money(driver?.saldoSemana ?: 0.0), Green, Modifier.fillMaxWidth())
    CardLine("Chave Pix", driver?.pixChave?.ifBlank { "Não cadastrada" } ?: "Não cadastrada", "Editar", Green)
    CardLine("Banco", driver?.banco?.ifBlank { "Não informado" } ?: "Não informado", "", Ink)
    PrimaryButton("Editar Pix/banco") { onPix() }
}

@Composable
private fun NotificationsScreen(items: List<com.rodriguesacai.entregador.data.DriverNotification>, onBack: () -> Unit) = BasePage("Notificações", "Alertas da operação", onBack) {
    if (items.isEmpty()) EmptyCard("Nenhuma notificação recebida.")
    items.forEach { CardLine(it.titulo, it.mensagem, if (it.lida) "Lida" else "Nova", if (it.lida) Muted else Green) }
}

@Composable
private fun ProfileScreen(driver: Driver?, onBack: () -> Unit, onPix: () -> Unit, onChange: () -> Unit, onLogout: () -> Unit) = BasePage("Perfil", "Conta do entregador", onBack) {
    Header(driver, onLogout)
    CardLine("Telefone", driver?.telefone?.ifBlank { "Solicite alteração" } ?: "Solicite alteração", "Alterar", Green)
    CardLine("Conta", if (driver?.verificado == true) "Verificado profissional" else "Pendente", "", if (driver?.verificado == true) Green else Yellow)
    PrimaryButton("Pix e banco") { onPix() }
    OutlineButton("Solicitar alteração") { onChange() }
    DangerButton("Sair") { onLogout() }
}

@Composable
private fun PixBankScreen(driver: Driver?, onBack: () -> Unit, onSave: (String, String, String) -> Unit) {
    var chave by remember(driver) { mutableStateOf(driver?.pixChave ?: "") }
    var tipo by remember(driver) { mutableStateOf(driver?.pixTipo ?: "CPF") }
    var banco by remember(driver) { mutableStateOf(driver?.banco ?: "") }
    BasePage("Pix/banco", "Recebimento", onBack) {
        Field(chave, { chave = it }, "Chave Pix")
        Field(tipo, { tipo = it }, "Tipo da chave")
        Field(banco, { banco = it }, "Banco")
        AlertBox("A conta precisa estar no nome do titular cadastrado.", Yellow)
        PrimaryButton("Salvar recebimento") { onSave(chave, tipo, banco) }
    }
}

@Composable
private fun ChangeRequestScreen(onBack: () -> Unit, onSend: (String, String, String) -> Unit) {
    var tipo by remember { mutableStateOf("telefone") }
    var valor by remember { mutableStateOf("") }
    var obs by remember { mutableStateOf("") }
    BasePage("Solicitação de alteração", "Aprovação pelo gestor", onBack) {
        Field(tipo, { tipo = it }, "Tipo: telefone ou e-mail")
        Field(valor, { valor = it }, "Novo valor")
        Field(obs, { obs = it }, "Observação")
        PrimaryButton("Enviar solicitação") { onSend(tipo, valor, obs) }
    }
}

@Composable
private fun OccurrenceScreen(ride: Ride?, onBack: () -> Unit, onSend: (String, String, String) -> Unit) {
    var motivo by remember { mutableStateOf("Cliente não atende") }
    var detalhe by remember { mutableStateOf("") }
    BasePage("Ocorrência", "Mantém corrida aberta", onBack) {
        Text("Pedido #${ride?.numeroPedido ?: "--"}", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Ink)
        Field(motivo, { motivo = it }, "Motivo")
        Field(detalhe, { detalhe = it }, "Detalhes")
        DangerButton("Registrar ocorrência") { if (ride != null) onSend(ride.id, motivo, detalhe) }
    }
}

@Composable
fun UrgentRideStandaloneScreen(rideId: String, title: String, body: String, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { FirebaseRepository(context) }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF120B0B), Red, Ink))).padding(22.dp)) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("ALERTA URGENTE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text(title, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(body, color = Color.White.copy(alpha = 0.86f), fontSize = 16.sp, textAlign = TextAlign.Center)
            PrimaryButton("Aceitar", background = Color.White, content = Ink) {
                scope.launch { if (rideId.isNotBlank()) repo.acceptRide(rideId); onClose() }
            }
            DangerButton("Recusar") {
                scope.launch { if (rideId.isNotBlank()) repo.rejectRide(rideId, "Recusada na tela urgente"); onClose() }
            }
        }
    }
}

@Composable
private fun BasePage(title: String, subtitle: String, onBack: () -> Unit, content: @Composable Column.() -> Unit) {
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Voltar", color = DarkGreen, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Ink)
                    Text(subtitle, fontSize = 12.sp, color = Muted)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
            }
        }
    }
}

@Composable
private fun BottomBar(onNav: (Screen) -> Unit) {
    Surface(color = Color.White, shadowElevation = 10.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
            BottomItem("Início") { onNav(Screen.Home) }
            BottomItem("Rota") { onNav(Screen.Mapa) }
            BottomItem("Ganhos") { onNav(Screen.Ganhos) }
            BottomItem("Histórico") { onNav(Screen.Historico) }
            BottomItem("Perfil") { onNav(Screen.Perfil) }
        }
    }
}

@Composable
private fun BottomItem(text: String, onClick: () -> Unit) {
    Text(text, modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(10.dp), color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun Header(driver: Driver?, onLogout: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(54.dp).clip(CircleShape).background(Green), contentAlignment = Alignment.Center) {
            Text((driver?.nome ?: "E").take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Olá, ${driver?.nome?.substringBefore(' ') ?: "entregador"}", color = Ink, fontWeight = FontWeight.Black, fontSize = 21.sp)
            Text(if (driver?.verificado == true) "Verificado profissional" else "Conta pendente", color = if (driver?.verificado == true) Green else Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onLogout) { Text("Sair", color = Muted) }
    }
}

@Composable
private fun StatusSwitch(driver: Driver?, onOnline: (Boolean) -> Unit) {
    val online = driver?.online == true || driver?.statusOperacional == "DISPONIVEL"
    val color = if (online) Green else Ink
    Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(color)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (online) "Disponível" else "Indisponível", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                Text(if (online) "Pronto para receber corridas" else "Toque para entrar na operação", color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp)
            }
            Button(onClick = { onOnline(!online) }, colors = ButtonDefaults.buttonColors(Color.White, color), shape = RoundedCornerShape(20.dp)) {
                Text(if (online) "Pausar" else "Ficar online", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun UrgentCard(ride: Ride, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(Red)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Nova corrida", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                Text("Pedido #${ride.numeroPedido} • ${ride.clienteBairro}", color = Color.White.copy(alpha = 0.84f))
            }
            Text(money(ride.valorCorrida), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

@Composable
private fun ActiveRideCard(ride: Ride, onOpen: () -> Unit, onMap: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Em andamento", color = Green, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Text("Pedido #${ride.numeroPedido}", color = Ink, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    Text(humanStatus(ride.status), color = Muted, fontSize = 13.sp)
                }
                Text(money(ride.valorCorrida), color = Ink, fontWeight = FontWeight.Black)
            }
            MiniMapDrawing(Modifier.height(120.dp).fillMaxWidth().clickable(onClick = onMap))
        }
    }
}

@Composable
private fun EarningsCompact(driver: Driver?, onToggleValues: (Boolean) -> Unit) {
    val hidden = driver?.ocultarValores == true
    Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(Color.White)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Ganhos de hoje", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(if (hidden) "••••" else money(driver?.saldoHoje ?: 0.0), color = Ink, fontWeight = FontWeight.Black, fontSize = 30.sp)
                }
                TextButton(onClick = { onToggleValues(!hidden) }) { Text(if (hidden) "Mostrar" else "Ocultar", color = DarkGreen, fontWeight = FontWeight.Bold) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Metric("Semana", if (hidden) "••••" else money(driver?.saldoSemana ?: 0.0), Green, Modifier.weight(1f))
                Metric("Mês", if (hidden) "••••" else money(driver?.saldoMes ?: 0.0), Ink, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniMapCard(active: Ride?, onOpen: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (active == null) "Área de operação" else "Preview da rota", fontWeight = FontWeight.Black, color = Ink, fontSize = 20.sp)
            MiniMapDrawing(Modifier.height(180.dp).fillMaxWidth())
        }
    }
}

@Composable
private fun QuickGrid(items: List<Pair<String, Screen>>, onNav: (Screen) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    Card(Modifier.weight(1f).clickable { onNav(item.second) }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(Color.White)) {
                        Text(item.first, Modifier.padding(18.dp), color = Ink, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NativeMapPreview(ride: Ride, modifier: Modifier) {
    val mapView = rememberMapViewWithLifecycle()
    AndroidView(factory = { mapView }, modifier = modifier) { view ->
        view.getMapAsync { map ->
            val loja = LatLng(ride.lojaLat.takeIf { it != 0.0 } ?: -20.4697, ride.lojaLng.takeIf { it != 0.0 } ?: -54.6201)
            val cliente = LatLng(ride.clienteLat.takeIf { it != 0.0 } ?: -20.4620, ride.clienteLng.takeIf { it != 0.0 } ?: -54.6250)
            map.clear()
            map.uiSettings.isZoomControlsEnabled = false
            map.uiSettings.isMapToolbarEnabled = false
            map.addMarker(MarkerOptions().position(loja).title("Coleta"))
            map.addMarker(MarkerOptions().position(cliente).title("Entrega"))
            map.addPolyline(PolylineOptions().add(loja, cliente).width(8f).color(0xFF12B76A.toInt()))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(loja, 13.4f))
        }
    }
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context).apply { onCreate(null) } }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> Unit
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return mapView
}

@Composable
private fun MiniMapDrawing(modifier: Modifier) {
    Canvas(modifier.clip(RoundedCornerShape(28.dp)).background(Color(0xFFEAF7F0)).border(1.dp, Color(0xFFD7EDE1), RoundedCornerShape(28.dp))) {
        val w = size.width
        val h = size.height
        drawLine(Color(0xFFD0E4DB), Offset(w * .08f, h * .2f), Offset(w * .9f, h * .14f), strokeWidth = 5f, cap = StrokeCap.Round)
        drawLine(Color(0xFFD0E4DB), Offset(w * .1f, h * .75f), Offset(w * .95f, h * .68f), strokeWidth = 5f, cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(w * .16f, h * .7f)
            cubicTo(w * .28f, h * .28f, w * .54f, h * .86f, w * .78f, h * .28f)
        }
        drawPath(path, Green, style = Stroke(width = 8f, cap = StrokeCap.Round))
        drawCircle(Ink, 14f, Offset(w * .16f, h * .7f))
        drawCircle(Red, 14f, Offset(w * .78f, h * .28f))
    }
}

@Composable
private fun Field(value: String, onValue: (String) -> Unit, label: String, type: KeyboardType = KeyboardType.Text, password: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = type),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF9FAFB),
            focusedIndicatorColor = Green,
            unfocusedIndicatorColor = Line
        )
    )
}

@Composable
private fun PrimaryButton(text: String, background: Color = Green, content: Color = Color.White, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(background, content)) {
        Text(text, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun DangerButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(Red, Color.White)) {
        Text(text, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun OutlineButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(54.dp).border(1.dp, Line, RoundedCornerShape(20.dp))) {
        Text(text, color = Ink, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun Metric(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(color.copy(alpha = 0.09f))) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AddressBlock(label: String, title: String, text: String) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color(0xFFF9FAFB))) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(label.uppercase(), color = Green, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(title, color = Ink, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(text, color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CardLine(title: String, subtitle: String, trailing: String, color: Color) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color(0xFFF9FAFB))) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontWeight = FontWeight.Black)
                Text(subtitle, color = Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(trailing, color = color, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AlertBox(text: String, color: Color) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.10f)).padding(14.dp)) {
        Text(text, color = if (color == Yellow) Ink else color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun EmptyCard(text: String) = AlertBox(text, Muted)

@Composable
private fun UploadCard(title: String, subtitle: String) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color(0xFFF9FAFB)), border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Ink, fontWeight = FontWeight.Black)
            Text(subtitle, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ProgressSteps(done: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(Modifier.weight(1f).height(9.dp).clip(RoundedCornerShape(99.dp)).background(if (i < done) Green else Line))
        }
    }
}

@Composable
private fun PasswordChecklist(senha: String) {
    AlertBox("Senha: mínimo 6 caracteres, evite dados pessoais e use algo fácil de lembrar.", if (senha.length >= 6) Green else Yellow)
}

@Composable
private fun PermissionRow(title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(color.copy(alpha = 0.10f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontWeight = FontWeight.Black)
                Text(subtitle, color = Muted, fontSize = 12.sp)
            }
            Text("Abrir", color = color, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
}

@Composable
private fun BrandMark() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(88.dp).clip(RoundedCornerShape(28.dp)).background(Green), contentAlignment = Alignment.Center) {
            Text("R", color = Color.White, fontWeight = FontWeight.Black, fontSize = 44.sp, fontFamily = FontFamily.SansSerif)
        }
        Text("Rodrigues Entregador", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text("Operação nativa", color = Color.White.copy(alpha = 0.74f), fontSize = 12.sp)
    }
}

@Composable
private fun StatusPage(title: String, message: String, color: Color, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Bg).padding(22.dp)) {
        Card(Modifier.align(Alignment.Center), shape = RoundedCornerShape(34.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(76.dp).clip(CircleShape).background(color.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(30.dp).clip(CircleShape).background(color))
                }
                Text(title, color = Ink, fontWeight = FontWeight.Black, fontSize = 26.sp, textAlign = TextAlign.Center)
                Text(message, color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
                PrimaryButton("Voltar") { onBack() }
            }
        }
    }
}

private fun startLocationService(context: Context) {
    val intent = Intent(context, DriverLocationService::class.java)
    ContextCompat.startForegroundService(context, intent)
}

private fun stopLocationService(context: Context) {
    context.stopService(Intent(context, DriverLocationService::class.java))
}

private fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
private fun Double.format1(): String = String.format(Locale("pt", "BR"), "%.1f", this)
private fun humanStatus(status: String): String = when (status) {
    "OFERTA_RECEBIDA" -> "Oferta recebida"
    "ACEITA" -> "Aceita"
    "INDO_COLETA" -> "Indo para coleta"
    "CHEGUEI_COLETA" -> "Na coleta"
    "PEDIDO_RETIRADO" -> "Pedido retirado"
    "INDO_ENTREGA" -> "Em rota"
    "ENTREGADOR_NO_LOCAL" -> "Chegou no cliente"
    "FINALIZADA" -> "Finalizada"
    "RECUSADA" -> "Recusada"
    "EXPIRADA" -> "Expirada"
    "OCORRENCIA" -> "Ocorrência aberta"
    else -> status
}
private fun nextActionText(status: String): String = when (status) {
    "ACEITA" -> "Iniciar ida para coleta"
    "INDO_COLETA" -> "Cheguei na coleta"
    "CHEGUEI_COLETA" -> "Pedido retirado"
    "PEDIDO_RETIRADO" -> "Ir para entrega"
    "INDO_ENTREGA" -> "Cheguei no cliente"
    "ENTREGADOR_NO_LOCAL" -> "Finalizar entrega"
    else -> "Avançar etapa"
}
private fun statusColor(status: String): Color = when (status) {
    "FINALIZADA" -> Green
    "RECUSADA", "EXPIRADA" -> Red
    "OCORRENCIA" -> Yellow
    else -> Ink
}
