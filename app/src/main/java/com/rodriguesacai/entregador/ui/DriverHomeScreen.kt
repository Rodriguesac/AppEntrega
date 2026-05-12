package com.rodriguesacai.entregador.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PurpleDark = Color(0xFF17031F)
private val PurpleMid = Color(0xFF38105F)
private val PurpleAccent = Color(0xFF7C3AED)
private val Green = Color(0xFF22C55E)
private val Red = Color(0xFFEF4444)
private val CardDark = Color(0xFF241134)
private val CardSoft = Color(0xFFF7F2FF)

@Composable
fun DriverHomeScreen(onGoOnline: () -> Unit, onGoOffline: () -> Unit) {
    var online by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = PurpleDark,
        bottomBar = { BottomMenu() }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(PurpleDark, PurpleMid, Color(0xFF4C0D7D))))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Header(online = online)
                Spacer(Modifier.height(14.dp))
                StatusCard(
                    online = online,
                    onToggle = {
                        online = !online
                        if (online) onGoOnline() else onGoOffline()
                    }
                )
                Spacer(Modifier.height(14.dp))
                RoutePreviewCard(online = online)
                Spacer(Modifier.height(14.dp))
                TodaySummaryCard()
                Spacer(Modifier.height(14.dp))
                QuickActionsCard()
            }
        }
    }
}

@Composable
private fun Header(online: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Rodrigues Entregador",
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (online) "Operação ativa agora" else "Toque para iniciar a operação",
                color = Color.White.copy(alpha = 0.68f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (online) Green.copy(alpha = .18f) else Color.White.copy(alpha = .12f))
                .border(1.dp, if (online) Green.copy(alpha = .55f) else Color.White.copy(alpha = .18f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (online) "ONLINE" else "OFFLINE",
                color = if (online) Green else Color.White.copy(alpha = .78f),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun StatusCard(online: Boolean, onToggle: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSoft),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(if (online) Green else Red)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (online) "Disponível para entregas" else "Você está fora da operação",
                    color = Color(0xFF1A1024),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (online) "Aguardando chamadas próximas. O app mantém o serviço ativo." else "Fique online para receber chamadas urgentes em tela cheia.",
                color = Color(0xFF3F334A),
                fontFamily = FontFamily.SansSerif,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (online) Color(0xFF1F102F) else PurpleAccent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (online) "Ficar offline" else "Ficar online agora",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun RoutePreviewCard(online: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = .96f)),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Área de entrega", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(if (online) "monitorando" else "pausado", color = if (online) Green else Color.White.copy(.55f), fontFamily = FontFamily.SansSerif, fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF2A1640), Color(0xFF53327E))))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawCircle(Color.White.copy(alpha = .06f), radius = w * .28f, center = Offset(w * .78f, h * .18f))
                    drawCircle(Color.White.copy(alpha = .05f), radius = w * .22f, center = Offset(w * .10f, h * .92f))
                    val path = Path().apply {
                        moveTo(w * .12f, h * .70f)
                        cubicTo(w * .28f, h * .30f, w * .46f, h * .86f, w * .62f, h * .48f)
                        cubicTo(w * .72f, h * .25f, w * .82f, h * .36f, w * .90f, h * .18f)
                    }
                    drawPath(path, color = Green, style = Stroke(width = 10f, cap = StrokeCap.Round))
                    drawCircle(Color.White, radius = 11f, center = Offset(w * .12f, h * .70f))
                    drawCircle(PurpleAccent, radius = 15f, center = Offset(w * .90f, h * .18f))
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text("Preview seguro da rota", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("O endereço completo libera na etapa certa", color = Color.White.copy(.75f), fontFamily = FontFamily.SansSerif, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun TodaySummaryCard() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SmallMetric("Hoje", "R$ 0,00", Modifier.weight(1f))
        SmallMetric("Entregas", "0", Modifier.weight(1f))
        SmallMetric("Tempo", "0h", Modifier.weight(1f))
    }
}

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .10f)), shape = RoundedCornerShape(22.dp), modifier = modifier) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color.White.copy(.67f), fontFamily = FontFamily.SansSerif, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun QuickActionsCard() {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .10f)), shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Ações rápidas", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ActionChip("Histórico", Modifier.weight(1f))
                ActionChip("Ganhos", Modifier.weight(1f))
                ActionChip("Ajustes", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ActionChip(text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = .12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontFamily = FontFamily.SansSerif, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BottomMenu() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(Color(0xFF0D0614))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BottomItem("Início", true)
        BottomItem("Pedidos", false)
        BottomItem("Ganhos", false)
        BottomItem("Mais", false)
    }
}

@Composable
private fun BottomItem(text: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(if (selected) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(if (selected) PurpleAccent else Color.White.copy(alpha = .28f))
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = text,
            color = if (selected) Color.White else Color.White.copy(alpha = .55f),
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
