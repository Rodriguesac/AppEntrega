package com.rodriguesacai.entregador.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun UrgentRideScreen(
    rideId: String,
    value: String,
    distance: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF110018), Color(0xFF3B0964), Color(0xFF5B0F91))))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(18.dp))
            Text(
                "NOVA CORRIDA",
                color = Color.White.copy(.82f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                value,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Oferta urgente para aceitar agora",
                color = Color.White.copy(.72f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(18.dp))
            MapOfferPreview()
            Spacer(Modifier.height(16.dp))
            OfferInfo(distance = distance, rideId = rideId)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color(0xFF06120A))
            ) {
                Text("ACEITAR CORRIDA", fontFamily = FontFamily.SansSerif, fontSize = 17.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Recusar", fontFamily = FontFamily.SansSerif, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MapOfferPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF221033), Color(0xFF59358B))))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(Color.White.copy(.06f), radius = w * .35f, center = Offset(w * .12f, h * .18f))
            drawCircle(Color.White.copy(.05f), radius = w * .28f, center = Offset(w * .88f, h * .88f))
            val path = Path().apply {
                moveTo(w * .14f, h * .78f)
                cubicTo(w * .28f, h * .62f, w * .35f, h * .22f, w * .54f, h * .45f)
                cubicTo(w * .68f, h * .62f, w * .76f, h * .22f, w * .90f, h * .28f)
            }
            drawPath(path, Color(0xFF22C55E), style = Stroke(width = 12f, cap = StrokeCap.Round))
            drawCircle(Color.White, radius = 13f, center = Offset(w * .14f, h * .78f))
            drawCircle(Color(0xFF7C3AED), radius = 18f, center = Offset(w * .90f, h * .28f))
        }
        Text(
            "Rota liberada por etapa",
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        )
    }
}

@Composable
private fun OfferInfo(distance: String, rideId: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(.12f)), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Dot(Color(0xFF22C55E))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Coleta", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Loja parceira Rodrigues Açaí", color = Color.White.copy(.68f), fontFamily = FontFamily.SansSerif, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Dot(Color(0xFF7C3AED))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Entrega", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("$distance • endereço completo após aceitar", color = Color.White.copy(.68f), fontFamily = FontFamily.SansSerif, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Pedido: $rideId", color = Color.White.copy(.55f), fontFamily = FontFamily.SansSerif, fontSize = 12.sp, textAlign = TextAlign.Start)
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(color))
}
