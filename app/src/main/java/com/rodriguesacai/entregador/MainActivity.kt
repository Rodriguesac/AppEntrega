package com.rodriguesacai.entregador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RodriguesLightTheme {
                CurrentRideScreen()
            }
        }
    }
}

private val AppGreen = Color(0xFF1E9D3A)
private val AppGreenDark = Color(0xFF08772A)
private val AppGreenSoft = Color(0xFFEAF7EE)
private val AppBg = Color(0xFFF6F8F5)
private val CardWhite = Color(0xFFFFFFFF)
private val Ink = Color(0xFF14171A)
private val Muted = Color(0xFF606A74)
private val Muted2 = Color(0xFF8D969F)
private val Border = Color(0xFFE1E6DF)
private val SoftLine = Color(0xFFE8EEE6)
private val MapBg = Color(0xFFF1F4F1)
private val AppFont = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_extrabold, FontWeight.ExtraBold)
)

@Composable
private fun RodriguesLightTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = MaterialTheme.typography.copy(
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = AppFont),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = AppFont),
            bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = AppFont),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = AppFont),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = AppFont),
            titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = AppFont),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = AppFont),
            labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = AppFont),
            labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = AppFont)
        ),
        content = content
    )
}

@Composable
private fun CurrentRideScreen() {
    Surface(color = AppBg, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 12.dp, bottom = 16.dp)
            ) {
                Header()
                Spacer(Modifier.height(16.dp))
                MapAndMetricsCard()
                Spacer(Modifier.height(16.dp))
                NextActionCard()
                Spacer(Modifier.height(14.dp))
                InfoRowCard(
                    icon = Icons.Outlined.AttachMoney,
                    title = "Pagamento",
                    subtitle = "Receber na entrega • Dinheiro • Troco para R$ 50,00"
                )
                Spacer(Modifier.height(10.dp))
                InfoRowCard(
                    icon = Icons.Outlined.Description,
                    title = "Observações",
                    subtitle = "Cliente pediu para tocar o interfone."
                )
                Spacer(Modifier.height(18.dp))
                SupportHint()
            }
            BottomBar()
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = Ink, modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Corrida atual",
            color = Ink,
            fontFamily = AppFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            modifier = Modifier.weight(1f)
        )
        StatusChip("Em rota para coleta")
    }
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Pedido ", color = Muted, fontSize = 15.sp, fontFamily = AppFont, fontWeight = FontWeight.Medium)
        Text("#2481", color = AppGreen, fontSize = 15.sp, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold)
        Text(" • iniciado às 15:42", color = Muted, fontSize = 15.sp, fontFamily = AppFont, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusChip(text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AppGreenSoft)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(AppGreen))
        Spacer(Modifier.width(8.dp))
        Text(text, color = AppGreenDark, fontSize = 13.sp, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun MapAndMetricsCard() {
    PremiumCard(padding = 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(218.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MapBg)
        ) {
            MapPreview(modifier = Modifier.fillMaxSize())
            MapPin(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 70.dp, top = 12.dp),
                label = "Coleta",
                icon = Icons.Outlined.Storefront,
                selected = true
            )
            MapPin(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 76.dp, bottom = 2.dp),
                label = "Entrega",
                icon = Icons.Outlined.Home,
                selected = false
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricItem(Icons.Outlined.AttachMoney, "Valor da corrida", "R$ 8,50", Modifier.weight(1f))
            VerticalSoftDivider()
            MetricItem(Icons.Outlined.LocationOn, "Distância", "3,2 km", Modifier.weight(1f))
            VerticalSoftDivider()
            MetricItem(Icons.Outlined.Schedule, "Tempo estimado", "12 min", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MapPreview(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val road = Color(0xFFDDE4DE)
            val roadSoft = Color(0xFFE9EEE9)
            val blue = Color(0xFFCDE1EA)
            val park = Color(0xFFDDEEDC)

            repeat(7) { i ->
                val y = h * (0.12f + i * 0.13f)
                drawLine(roadSoft, Offset(0f, y), Offset(w, y + if (i % 2 == 0) 30f else -25f), strokeWidth = 2.1f)
            }
            repeat(8) { i ->
                val x = w * (0.06f + i * 0.13f)
                drawLine(roadSoft, Offset(x, 0f), Offset(x + if (i % 2 == 0) 35f else -15f, h), strokeWidth = 2.0f)
            }
            drawLine(blue, Offset(w * .39f, -20f), Offset(w * .51f, h + 20f), strokeWidth = 12f, cap = StrokeCap.Round)
            drawLine(Color.White.copy(alpha = .85f), Offset(w * .42f, -20f), Offset(w * .55f, h + 20f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawCircle(park, radius = h * .19f, center = Offset(w * .45f, h * .73f))
            drawCircle(park.copy(alpha = .65f), radius = h * .12f, center = Offset(w * .72f, h * .24f))

            val route = Path().apply {
                moveTo(w * .16f, h * .52f)
                lineTo(w * .27f, h * .63f)
                lineTo(w * .47f, h * .45f)
                lineTo(w * .58f, h * .73f)
                lineTo(w * .66f, h * .50f)
                lineTo(w * .71f, h * .57f)
                lineTo(w * .76f, h * .44f)
                lineTo(w * .88f, h * .48f)
            }
            drawPath(route, Color(0xFF2FA447), style = Stroke(width = 7.2f, cap = StrokeCap.Round))
            drawPath(route, Color(0xFF2FA447).copy(alpha = .22f), style = Stroke(width = 15f, cap = StrokeCap.Round))
        }
        MapLabel("VILA YARA", Modifier.align(Alignment.TopStart).padding(start = 82.dp, top = 22.dp))
        MapLabel("JARDIM\nSÃO PAULO", Modifier.align(Alignment.TopCenter).padding(top = 45.dp))
        MapLabel("VILA LEOPOLDINA", Modifier.align(Alignment.BottomStart).padding(start = 82.dp, bottom = 24.dp))
        MapLabel("ALTO DA LAPA", Modifier.align(Alignment.BottomEnd).padding(end = 92.dp, bottom = 18.dp))
    }
}

@Composable
private fun MapLabel(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = Color(0xFF6D767C).copy(alpha = .78f),
        fontFamily = AppFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        lineHeight = 13.sp,
        textAlign = TextAlign.Center,
        letterSpacing = 1.1.sp
    )
}

@Composable
private fun MapPin(modifier: Modifier, label: String, icon: ImageVector, selected: Boolean) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (selected) AppGreen else Ink)
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF202326))
                .padding(horizontal = 11.dp, vertical = 6.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = AppFont
        )
    }
}

@Composable
private fun MetricItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(AppGreenSoft), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = AppGreen, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont)
        }
    }
}

@Composable
private fun NextActionCard() {
    PremiumCard {
        Text("Próxima ação", color = AppGreen, fontSize = 20.sp, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        Text("Você está indo para a coleta na loja.", color = Ink, fontSize = 15.sp, fontFamily = AppFont, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(18.dp))
        RouteDetailsCard()
        Spacer(Modifier.height(20.dp))
        Stepper()
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(62.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppGreen)
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(25.dp))
            Spacer(Modifier.width(10.dp))
            Text("Cheguei na coleta", color = Color.White, fontSize = 19.sp, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ActionOutlineButton(Icons.Outlined.Navigation, "Abrir navegação", Modifier.weight(1f))
            ActionOutlineButton(Icons.Outlined.WarningAmber, "Informar problema", Modifier.weight(1f))
        }
    }
}

@Composable
private fun RouteDetailsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Border, RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        RoutePlaceRow(
            icon = Icons.Outlined.Storefront,
            iconBg = AppGreen,
            title = "Rodrigues Açaí e Cia.",
            subtitle = "Rua das Palmeiras, 123 • Vila Yara",
            chip = "Coleta",
            chipColor = AppGreenSoft,
            chipText = AppGreenDark
        )
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(49.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.height(28.dp).width(2.dp)) {
                    drawLine(
                        color = Muted2.copy(alpha = .65f),
                        start = Offset(size.width / 2f, 0f),
                        end = Offset(size.width / 2f, size.height),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 7f))
                    )
                }
            }
            HorizontalDivider(Modifier.weight(1f).padding(top = 14.dp), color = SoftLine, thickness = 1.dp)
        }
        RoutePlaceRow(
            icon = Icons.Outlined.Home,
            iconBg = Color(0xFFF1F2F3),
            iconTint = Ink,
            title = "Cliente: Ana Paula S.",
            subtitle = "Rua das Orquídeas, 456 • Alto da Lapa",
            chip = "Entrega",
            chipColor = Color(0xFFF1F2F3),
            chipText = Muted
        )
    }
}

@Composable
private fun RoutePlaceRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color = Color.White,
    title: String,
    subtitle: String,
    chip: String,
    chipColor: Color,
    chipText: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.size(49.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            chip,
            modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(chipColor).padding(horizontal = 12.dp, vertical = 7.dp),
            color = chipText,
            fontFamily = AppFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun Stepper() {
    val labels = listOf("Aceita", "Na coleta", "Pedido\nretirado", "Cheguei no\ncliente", "Finalizar")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEachIndexed { index, label ->
            val done = index == 0
            val active = index == 1
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(.5f)
                                .height(2.dp)
                                .background(if (done || active) AppGreen else Border)
                        )
                    }
                    if (index < labels.lastIndex) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxWidth(.5f)
                                .height(2.dp)
                                .background(if (done) AppGreen else Border)
                        )
                    }
                    Box(
                        Modifier
                            .size(if (active) 29.dp else 25.dp)
                            .clip(CircleShape)
                            .background(if (done) AppGreen else if (active) AppGreenSoft else Color.White)
                            .border(if (active) 4.dp else 2.dp, if (done || active) AppGreen else Border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    label,
                    color = if (active) AppGreen else Muted,
                    fontFamily = AppFont,
                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp,
                    minLines = 2
                )
            }
        }
    }
}

@Composable
private fun ActionOutlineButton(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = {},
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Icon(icon, contentDescription = null, tint = AppGreen, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun InfoRowCard(icon: ImageVector, title: String, subtitle: String) {
    PremiumCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(AppGreenSoft), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AppGreen, modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text(subtitle, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Outlined.ExpandMore, contentDescription = null, tint = Muted, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun SupportHint() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Outlined.Security, contentDescription = null, tint = Muted, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Se houver imprevisto, registre uma ocorrência\npara o gestor acompanhar.",
            color = Muted,
            fontFamily = AppFont,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun BottomBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SoftLine)
            .navigationBarsPadding()
    ) {
        BottomItem(Icons.Outlined.Home, "Home", false)
        BottomItem(Icons.Outlined.Map, "Mapa", false)
        BottomItem(Icons.Outlined.LocalMall, "Corrida", true)
        BottomItem(Icons.Outlined.AccountBalanceWallet, "Ganhos", false)
        BottomItem(Icons.Outlined.Person, "Perfil", false)
    }
}

@Composable
private fun BottomItem(icon: ImageVector, label: String, selected: Boolean) {
    NavigationBarItem(
        selected = selected,
        onClick = {},
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(25.dp)) },
        label = { Text(label, fontFamily = AppFont, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = AppGreen,
            selectedTextColor = AppGreen,
            indicatorColor = AppGreenSoft,
            unselectedIconColor = Muted,
            unselectedTextColor = Muted
        )
    )
}

@Composable
private fun PremiumCard(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 18.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

@Composable
private fun VerticalSoftDivider() {
    Box(Modifier.height(42.dp).width(1.dp).background(SoftLine))
}
