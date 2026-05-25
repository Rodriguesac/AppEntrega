package com.rodriguesacai.entregador.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.core.AppTab

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(padding)) { content() }
    }
}

@Composable
fun StatusChip(text: String, positive: Boolean = true) {
    val color = if (positive) AppGreen else Danger
    val bg = if (positive) AppGreenSoft else Color(0xFFFFEBEE)
    Row(
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(bg).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(text, color = color, fontSize = 12.sp, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
fun EmptyState(title: String, body: String, icon: ImageVector) {
    PremiumCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
            Box(Modifier.size(68.dp).clip(CircleShape).background(AppGreenSoft), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AppGreen, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(title, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(body, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
        }
    }
}

@Composable
fun PrimaryButton(text: String, icon: ImageVector? = null, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppGreen)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(23.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, color = Color.White, fontSize = 17.sp, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SecondaryButton(text: String, icon: ImageVector? = null, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = AppGreen, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun InfoLine(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    PremiumCard(modifier = modifier, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(AppGreenSoft), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AppGreen, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(subtitle, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Outlined.ExpandMore, contentDescription = null, tint = Muted, modifier = Modifier.size(25.dp))
        }
    }
}

@Composable
fun MiniMap(modifier: Modifier = Modifier, showPins: Boolean = true) {
    Box(modifier = modifier.clip(RoundedCornerShape(22.dp)).background(Color(0xFFF1F4F1))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            repeat(8) { i ->
                val y = h * (0.10f + i * 0.13f)
                drawLine(Color(0xFFE0E7E0), Offset(0f, y), Offset(w, y + if (i % 2 == 0) 24f else -18f), strokeWidth = 2.2f)
            }
            repeat(8) { i ->
                val x = w * (0.06f + i * 0.13f)
                drawLine(Color(0xFFE9EEE9), Offset(x, 0f), Offset(x + if (i % 2 == 0) 28f else -12f, h), strokeWidth = 2f)
            }
            drawLine(Color(0xFFCDE1EA), Offset(w * .39f, -20f), Offset(w * .51f, h + 20f), strokeWidth = 12f, cap = StrokeCap.Round)
            val route = Path().apply {
                moveTo(w * .15f, h * .55f)
                lineTo(w * .27f, h * .66f)
                lineTo(w * .47f, h * .45f)
                lineTo(w * .58f, h * .74f)
                lineTo(w * .67f, h * .50f)
                lineTo(w * .74f, h * .58f)
                lineTo(w * .82f, h * .44f)
                lineTo(w * .90f, h * .47f)
            }
            drawPath(route, AppGreen.copy(alpha = .20f), style = Stroke(width = 15f, cap = StrokeCap.Round))
            drawPath(route, AppGreen, style = Stroke(width = 7f, cap = StrokeCap.Round))
        }
        if (showPins) {
            MapPin(Modifier.align(Alignment.CenterStart).padding(start = 44.dp), "Coleta", Icons.Outlined.Storefront, true)
            MapPin(Modifier.align(Alignment.CenterEnd).padding(end = 44.dp), "Entrega", Icons.Outlined.Home, false)
        }
    }
}

@Composable
fun MapPin(modifier: Modifier, label: String, icon: ImageVector, selected: Boolean) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(if (selected) AppGreen else Ink).border(3.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, modifier = Modifier.clip(RoundedCornerShape(9.dp)).background(Color(0xFF202326)).padding(horizontal = 10.dp, vertical = 5.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont)
    }
}

@Composable
fun MetricBlock(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(AppGreenSoft), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = AppGreen, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(9.dp))
        Column {
            Text(label, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFont, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = AppFont, maxLines = 1)
        }
    }
}

@Composable
fun BottomNav(selected: AppTab, unread: Int, onSelect: (AppTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, SoftLine)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(AppTab.Home, selected, Icons.Outlined.Home, "Home", onSelect, Modifier.weight(1f))
        NavItem(AppTab.Mapa, selected, Icons.Outlined.Map, "Mapa", onSelect, Modifier.weight(1f))
        NavItem(AppTab.Corrida, selected, Icons.Outlined.LocalMall, "Corrida", onSelect, Modifier.weight(1f))
        NavItem(AppTab.Ganhos, selected, Icons.Outlined.AccountBalanceWallet, "Ganhos", onSelect, Modifier.weight(1f))
        NavItem(AppTab.Perfil, selected, Icons.Outlined.Person, "Perfil", onSelect, Modifier.weight(1f), unread)
    }
}

@Composable
private fun NavItem(tab: AppTab, selected: AppTab, icon: ImageVector, label: String, onSelect: (AppTab) -> Unit, modifier: Modifier = Modifier, badge: Int = 0) {
    val active = selected == tab
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) AppGreenSoft else Color.Transparent)
            .clickable { onSelect(tab) }
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(icon, contentDescription = null, tint = if (active) AppGreen else Muted, modifier = Modifier.size(23.dp))
            if (badge > 0) Box(Modifier.size(8.dp).clip(CircleShape).background(Danger))
        }
        Spacer(Modifier.height(2.dp))
        Text(label, color = if (active) AppGreen else Muted, fontFamily = AppFont, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = Ink, fontFamily = AppFont, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Muted, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
fun ThinDivider() = HorizontalDivider(color = SoftLine, thickness = 1.dp)
