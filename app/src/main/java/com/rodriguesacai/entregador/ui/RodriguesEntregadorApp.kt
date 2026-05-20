package com.rodriguesacai.entregador.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.service.DriverLocationService
import com.rodriguesacai.entregador.ui.navigation.AppRoute
import com.rodriguesacai.entregador.ui.screens.auth.AnalysisScreen
import com.rodriguesacai.entregador.ui.screens.auth.CreatePasswordScreen
import com.rodriguesacai.entregador.ui.screens.auth.LoginScreen
import com.rodriguesacai.entregador.ui.screens.auth.RegistrationScreen
import com.rodriguesacai.entregador.ui.screens.earnings.EarningsScreen
import com.rodriguesacai.entregador.ui.screens.earnings.WalletScreen
import com.rodriguesacai.entregador.ui.screens.history.HistoryScreen
import com.rodriguesacai.entregador.ui.screens.home.HomeScreen
import com.rodriguesacai.entregador.ui.screens.permissions.PermissionsScreen
import com.rodriguesacai.entregador.ui.screens.profile.ChangeRequestScreen
import com.rodriguesacai.entregador.ui.screens.profile.PixBankScreen
import com.rodriguesacai.entregador.ui.screens.profile.ProfileScreen
import com.rodriguesacai.entregador.ui.screens.rides.ActiveRideScreen
import com.rodriguesacai.entregador.ui.screens.rides.MapRouteScreen
import com.rodriguesacai.entregador.ui.screens.rides.UrgentRideScreen
import com.rodriguesacai.entregador.ui.screens.support.NotificationsScreen
import com.rodriguesacai.entregador.ui.screens.support.OccurrenceScreen
import com.rodriguesacai.entregador.ui.screens.support.StatusPage
import com.rodriguesacai.entregador.ui.theme.AppColors
import com.rodriguesacai.entregador.ui.theme.RodriguesTheme

@Composable
fun RodriguesEntregadorApp(vm: DriverViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var route by remember { mutableStateOf(if (state.logged) AppRoute.Home else AppRoute.Login) }
    var selectedRide by remember { mutableStateOf<Ride?>(null) }
    val context = LocalContext.current

    LaunchedEffect(state.logged) {
        if (state.logged && route == AppRoute.Login) route = AppRoute.Home
        if (!state.logged) route = AppRoute.Login
    }

    RodriguesTheme {
        Surface(Modifier.fillMaxSize(), color = AppColors.Bg) {
            when (route) {
                AppRoute.Login -> LoginScreen(
                    loading = state.loading,
                    error = state.error,
                    onLogin = vm::login,
                    onDemo = vm::enterDemo,
                    onCadastro = { route = AppRoute.Cadastro },
                    onCriarSenha = { route = AppRoute.CriarSenha }
                )
                AppRoute.Cadastro -> RegistrationScreen(
                    onBack = { route = AppRoute.Login },
                    onSubmit = vm::submitRegistration,
                    onFinish = { route = AppRoute.Analise }
                )
                AppRoute.Analise -> AnalysisScreen(onBack = { route = AppRoute.Login })
                AppRoute.CriarSenha -> CreatePasswordScreen(
                    message = state.message,
                    error = state.error,
                    onBack = { route = AppRoute.Login },
                    onCreate = vm::createPassword
                )
                AppRoute.Home -> HomeScreen(
                    state = state,
                    onOnline = { online ->
                        vm.setOnline(online)
                        if (online) startLocationService(context) else stopLocationService(context)
                    },
                    onPermissions = { route = AppRoute.Permissoes },
                    onUrgent = { ride -> selectedRide = ride; route = AppRoute.CorridaUrgente },
                    onRide = { ride -> selectedRide = ride; route = AppRoute.CorridaAndamento },
                    onNav = { route = it },
                    onLogout = vm::logout,
                    onToggleValues = vm::toggleValues
                )
                AppRoute.Permissoes -> PermissionsScreen(onBack = { route = AppRoute.Home })
                AppRoute.CorridaUrgente -> UrgentRideScreen(
                    ride = selectedRide ?: state.activeRides.firstOrNull { it.status == "OFERTA_RECEBIDA" },
                    onBack = { route = AppRoute.Home },
                    onAccept = { id -> vm.acceptRide(id); selectedRide = null; route = AppRoute.CorridaAndamento },
                    onReject = { id, motivo -> vm.rejectRide(id, motivo); route = AppRoute.Home }
                )
                AppRoute.CorridaAndamento -> ActiveRideScreen(
                    ride = selectedRide ?: state.activeRides.firstOrNull { it.status != "OFERTA_RECEBIDA" },
                    onBack = { route = AppRoute.Home },
                    onMap = { route = AppRoute.Mapa },
                    onAdvance = { ride -> vm.advanceRide(ride); selectedRide = null },
                    onOccurrence = { ride -> selectedRide = ride; route = AppRoute.Ocorrencia }
                )
                AppRoute.Mapa -> MapRouteScreen(
                    ride = selectedRide ?: state.activeRides.firstOrNull(),
                    onBack = { route = AppRoute.CorridaAndamento }
                )
                AppRoute.Historico -> HistoryScreen(state.history, onBack = { route = AppRoute.Home }, onNav = { route = it })
                AppRoute.Ganhos -> EarningsScreen(
                    driver = state.driver,
                    history = state.history,
                    onBack = { route = AppRoute.Home },
                    onWallet = { route = AppRoute.Carteira },
                    onToggleValues = vm::toggleValues,
                    onNav = { route = it }
                )
                AppRoute.Carteira -> WalletScreen(state.driver, onBack = { route = AppRoute.Ganhos }, onPix = { route = AppRoute.PixBanco })
                AppRoute.Notificacoes -> NotificationsScreen(state.notifications, onBack = { route = AppRoute.Home })
                AppRoute.Perfil -> ProfileScreen(
                    driver = state.driver,
                    onBack = { route = AppRoute.Home },
                    onPix = { route = AppRoute.PixBanco },
                    onChange = { route = AppRoute.Solicitacao },
                    onLogout = vm::logout,
                    onNav = { route = it }
                )
                AppRoute.PixBanco -> PixBankScreen(state.driver, onBack = { route = AppRoute.Perfil }, onSave = vm::savePix)
                AppRoute.Solicitacao -> ChangeRequestScreen(onBack = { route = AppRoute.Perfil }, onSend = vm::requestChange)
                AppRoute.Ocorrencia -> OccurrenceScreen(
                    ride = selectedRide ?: state.activeRides.firstOrNull(),
                    onBack = { route = AppRoute.CorridaAndamento },
                    onSend = { rideId, motivo, detalhe -> vm.createOccurrence(rideId, motivo, detalhe); selectedRide = null }
                )
                AppRoute.Atualizacao -> StatusPage("Atualização do app", "Quando houver nova versão obrigatória, esta tela bloqueia o uso até atualizar.", AppColors.Green, onBack = { route = AppRoute.Home })
                AppRoute.SemInternet -> StatusPage("Sem internet", "Conexão indisponível. O app mantém a tela limpa e tenta recuperar os dados.", AppColors.Red, onBack = { route = AppRoute.Home })
                AppRoute.Manutencao -> StatusPage("Manutenção", "Operação pausada temporariamente pelo gestor.", AppColors.Purple, onBack = { route = AppRoute.Home })
                AppRoute.ErroFirebase -> StatusPage("Erro Firebase", state.error ?: "Falha ao sincronizar dados reais.", AppColors.Red, onBack = { route = AppRoute.Home })
            }
        }
    }
}

private fun startLocationService(context: Context) {
    val intent = Intent(context, DriverLocationService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.startForegroundService(context, intent)
    else context.startService(intent)
}

private fun stopLocationService(context: Context) {
    context.stopService(Intent(context, DriverLocationService::class.java))
}
