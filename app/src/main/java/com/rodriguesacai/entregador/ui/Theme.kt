package com.rodriguesacai.entregador.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rodriguesacai.entregador.R

val AppGreen = Color(0xFF1E9D3A)
val AppGreenDark = Color(0xFF08772A)
val AppGreenSoft = Color(0xFFEAF7EE)
val AppBg = Color(0xFFF6F8F5)
val CardWhite = Color(0xFFFFFFFF)
val Ink = Color(0xFF14171A)
val Muted = Color(0xFF606A74)
val Muted2 = Color(0xFF8D969F)
val Border = Color(0xFFE1E6DF)
val SoftLine = Color(0xFFE8EEE6)
val Danger = Color(0xFFD93025)
val Warning = Color(0xFFFFB020)

val AppFont = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_extrabold, FontWeight.ExtraBold)
)

@Composable
fun RodriguesTheme(content: @Composable () -> Unit) {
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
