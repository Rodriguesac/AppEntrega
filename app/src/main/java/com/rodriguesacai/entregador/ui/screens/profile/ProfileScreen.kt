package com.rodriguesacai.entregador.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.ui.components.DriverPhoto
import com.rodriguesacai.entregador.ui.components.EmptyState
import com.rodriguesacai.entregador.ui.components.ProfileLine
import com.rodriguesacai.entregador.ui.components.SecondaryAction
import com.rodriguesacai.entregador.ui.components.UpCard
import com.rodriguesacai.entregador.ui.components.UpPage
import com.rodriguesacai.entregador.ui.humanStatus
import com.rodriguesacai.entregador.ui.navigation.AppRoute
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun ProfileScreen(driver: Driver?, onBack: () -> Unit, onPix: () -> Unit, onChange: () -> Unit, onLogout: () -> Unit, onNav: (AppRoute) -> Unit) {
    UpPage(title = "Perfil", onBack = onBack, current = AppRoute.Perfil, onNav = onNav) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (driver == null) {
                EmptyState("Perfil não sincronizado", "Os dados reais do entregador aparecem depois do login aprovado no Firebase.", Icons.Rounded.Person)
            } else {
                UpCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DriverPhoto(driver, size = 72.dp)
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(driver.nome.ifBlank { "Nome não informado" }, color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 20.sp, maxLines = 1)
                                if (driver.verificado) Text("  ✓", color = UpColors.Green, fontWeight = FontWeight.Black)
                            }
                            Text(humanStatus(driver.statusOperacional), color = UpColors.Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Divider(color = UpColors.Line)
                    ProfileLine(Icons.Rounded.Person, "CPF", driver.cpf)
                    ProfileLine(Icons.Rounded.Phone, "Telefone", driver.telefone, onClick = onChange)
                    ProfileLine(Icons.Rounded.Email, "E-mail", driver.email, onClick = onChange)
                    ProfileLine(Icons.Rounded.DirectionsBike, "Veículo", "Não informado")
                    ProfileLine(Icons.Rounded.CheckCircle, "Status cadastral", if (driver.verificado) "Aprovado" else "Em análise")
                }
                UpCard {
                    ProfileLine(Icons.Rounded.CreditCard, "Recebimento", if (driver.pixChave.isBlank()) "Pendente" else "Configurado", onClick = onPix)
                    ProfileLine(Icons.Rounded.Settings, "Dados pessoais", "Solicitar alteração", onClick = onChange)
                    ProfileLine(Icons.Rounded.Navigation, "Preferências de navegação", "Padrão do celular")
                }
                SecondaryAction("Sair da conta", onLogout, icon = Icons.Rounded.ExitToApp, red = true)
            }
        }
    }
}
