package com.rodriguesacai.entregador.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
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

private val AppFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val AppFontName = GoogleFont("Inter")

val AppFont = FontFamily(
    Font(googleFont = AppFontName, fontProvider = AppFontProvider, weight = FontWeight.Normal),
    Font(googleFont = AppFontName, fontProvider = AppFontProvider, weight = FontWeight.Medium),
    Font(googleFont = AppFontName, fontProvider = AppFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = AppFontName, fontProvider = AppFontProvider, weight = FontWeight.Bold),
    Font(googleFont = AppFontName, fontProvider = AppFontProvider, weight = FontWeight.ExtraBold)
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
