package com.rodriguesacai.entregador.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.data.Driver
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.format1
import com.rodriguesacai.entregador.ui.humanStatus
import com.rodriguesacai.entregador.ui.money
import com.rodriguesacai.entregador.ui.navigation.AppRoute
import com.rodriguesacai.entregador.ui.statusColor
import com.rodriguesacai.entregador.ui.theme.AppColors

@Composable
fun BasePage(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable Column.() -> Unit
) {
    Scaffold(
        containerColor = AppColors.Bg,
        bottomBar = { bottomBar?.invoke() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Voltar", color = AppColors.Ink) }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = AppColors.Ink)
                    Text(subtitle, color = AppColors.Muted, fontSize = 13.sp)
                }
            }
            content()
        }
    }
}

@Composable
fun AppBottomBar(current: AppRoute, onNav: (AppRoute) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        val items = listOf(
            AppRoute.Home to "Início",
            AppRoute.Historico to "Histórico",
            AppRoute.Ganhos to "Ganhos",
            AppRoute.Perfil to "Perfil"
        )
        items.forEach { (route, label) ->
            NavigationBarItem(
                selected = current == route,
                onClick = { onNav(route) },
                icon = { Text(label.take(1), fontWeight = FontWeight.Bold) },
                label = { Text(label, fontSize = 11.sp) }
            )
        }
    }
}

@Composable
fun Header(driver: Driver?, onLogout: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(28.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AppColors.Ink),
                contentAlignment = Alignment.Center
            ) {
                Text((driver?.nome ?: "E").take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Olá, ${driver?.nome ?: "entregador"}", color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(if (driver?.verificado == true) "Verificado profissional" else "Conta do entregador", color = AppColors.Muted, fontSize = 12.sp)
            }
            TextButton(onClick = onLogout) { Text("Sair", color = AppColors.Red) }
        }
    }
}

@Composable
fun StatusSwitch(driver: Driver?, onOnline: (Boolean) -> Unit) {
    val online = driver?.online == true
    val restricted = driver?.statusOperacional == "RESTRICAO"
    val text = if (restricted) "Restrição" else if (online) "Disponível" else "Indisponível"
    val color = if (restricted) AppColors.Red else if (online) AppColors.Green else AppColors.Ink
    Button(
        onClick = { if (!restricted) onOnline(!online) },
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth().height(58.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
    }
}

@Composable
fun PrimaryButton(text: String, background: Color = AppColors.Green, content: Color = Color.White, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = background, contentColor = content),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) { Text(text, fontWeight = FontWeight.Black) }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit) = PrimaryButton(text, AppColors.Red, Color.White, onClick)

@Composable
fun OutlineAction(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().height(50.dp)) {
        Text(text, color = AppColors.Ink, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Field(
    value: String,
    onValue: (String) -> Unit,
    label: String,
    type: KeyboardType = KeyboardType.Text,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = type),
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = !label.contains("observação", ignoreCase = true) && !label.contains("detalhe", ignoreCase = true),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = AppColors.Green,
            unfocusedIndicatorColor = AppColors.Line
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun Metric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = AppColors.Muted, fontSize = 12.sp)
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1)
        }
    }
}

@Composable
fun AlertBox(text: String, color: Color = AppColors.Muted) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
        Text(text, color = color, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EmptyCard(text: String) = AlertBox(text, AppColors.Muted)

@Composable
fun AddressBlock(label: String, title: String, text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = AppColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(title, color = AppColors.Ink, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(text, color = AppColors.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
fun CardLine(title: String, subtitle: String, trailing: String, color: Color = AppColors.Green) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = AppColors.Ink, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = AppColors.Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(trailing, color = color, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun UrgentCard(ride: Ride, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColors.Ink),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Nova corrida", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("Pedido ${ride.numeroPedido} • ${ride.clienteBairro}", color = Color.White.copy(alpha = .82f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Metric("Valor", money(ride.valorCorrida), AppColors.Green, Modifier.weight(1f))
                Metric("Distância", "${ride.distanciaKm.format1()} km", AppColors.Ink, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ActiveRideCard(ride: Ride, onOpen: () -> Unit, onMap: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(28.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Pedido ${ride.numeroPedido}", color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 19.sp)
                    Text(humanStatus(ride.status), color = statusColor(ride.status), fontWeight = FontWeight.Bold)
                }
                Text(money(ride.valorCorrida), color = AppColors.Green, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Divider(color = AppColors.Line)
            Text("${ride.distanciaKm.format1()} km • ${ride.tempoEstimadoMin} min • ${ride.clienteBairro}", color = AppColors.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpen, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("Abrir") }
                OutlinedButton(onClick = onMap, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("Mapa") }
            }
        }
    }
}

@Composable
fun EarningsCompact(driver: Driver?, onToggleValues: (Boolean) -> Unit) {
    val hidden = driver?.ocultarValores == true
    val hoje = if (hidden) "••••" else money(driver?.saldoHoje ?: 0.0)
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(28.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Ganhos de hoje", color = AppColors.Muted, fontSize = 12.sp)
                Text(hoje, color = AppColors.Ink, fontWeight = FontWeight.Black, fontSize = 26.sp)
                Text("${driver?.corridasHoje ?: 0} corridas finalizadas", color = AppColors.Muted, fontSize = 12.sp)
            }
            TextButton(onClick = { onToggleValues(!hidden) }) { Text(if (hidden) "Mostrar" else "Ocultar") }
        }
    }
}

@Composable
fun QuickGrid(items: List<Pair<String, AppRoute>>, onNav: (AppRoute) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    Card(
                        modifier = Modifier.weight(1f).height(76.dp).clickable { onNav(item.second) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Box(Modifier.fillMaxWidth().padding(14.dp), contentAlignment = Alignment.CenterStart) {
                            Text(item.first, color = AppColors.Ink, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MiniMapDrawing(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clip(RoundedCornerShape(28.dp)).background(Color.White)) {
        val w = size.width
        val h = size.height
        drawLine(AppColors.Line, Offset(w * .1f, h * .25f), Offset(w * .9f, h * .2f), strokeWidth = 10f, cap = StrokeCap.Round)
        drawLine(AppColors.Line, Offset(w * .18f, h * .75f), Offset(w * .82f, h * .8f), strokeWidth = 10f, cap = StrokeCap.Round)
        drawCircle(AppColors.Green, 18f, Offset(w * .25f, h * .5f))
        drawCircle(AppColors.Red, 18f, Offset(w * .75f, h * .5f))
        drawLine(AppColors.Green, Offset(w * .25f, h * .5f), Offset(w * .75f, h * .5f), strokeWidth = 8f, cap = StrokeCap.Round)
        drawCircle(Color.White, 30f, Offset(w * .5f, h * .5f), style = Stroke(width = 6f))
    }
}

@Composable
fun UploadCard(title: String, subtitle: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().border(1.dp, AppColors.Line, RoundedCornerShape(22.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = AppColors.Ink, fontWeight = FontWeight.Black)
            Text(subtitle, color = AppColors.Muted, textAlign = TextAlign.Center, fontSize = 12.sp)
        }
    }
}

@Composable
fun PasswordChecklist(password: String) {
    val checks = listOf(
        "mínimo 6 caracteres" to (password.length >= 6),
        "contém número" to password.any { it.isDigit() },
        "contém letra" to password.any { it.isLetter() }
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        checks.forEach { (label, ok) ->
            Text((if (ok) "OK " else "-- ") + label, color = if (ok) AppColors.Green else AppColors.Muted, fontSize = 12.sp)
        }
    }
}
