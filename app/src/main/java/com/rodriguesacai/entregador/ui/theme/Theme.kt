package com.rodriguesacai.entregador.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

object AppColors {
    val Bg = Color(0xFFF4F6F8)
    val Surface = Color(0xFFFFFFFF)
    val Ink = Color(0xFF101828)
    val Muted = Color(0xFF667085)
    val SoftMuted = Color(0xFF98A2B3)
    val Line = Color(0xFFE4E7EC)
    val Green = Color(0xFF16A34A)
    val DarkGreen = Color(0xFF067647)
    val Red = Color(0xFFDC3545)
    val Yellow = Color(0xFFFFB020)
    val Purple = Color(0xFF3F1D70)
}

private val Scheme = lightColorScheme(
    primary = AppColors.Green,
    onPrimary = Color.White,
    background = AppColors.Bg,
    onBackground = AppColors.Ink,
    surface = AppColors.Surface,
    onSurface = AppColors.Ink,
    error = AppColors.Red
)

private val AppFont = FontFamily.SansSerif
private fun TextStyle.fixedFont() = copy(fontFamily = AppFont)

private val AppTypography = Typography().let { t ->
    t.copy(
        displayLarge = t.displayLarge.fixedFont(),
        displayMedium = t.displayMedium.fixedFont(),
        displaySmall = t.displaySmall.fixedFont(),
        headlineLarge = t.headlineLarge.fixedFont(),
        headlineMedium = t.headlineMedium.fixedFont(),
        headlineSmall = t.headlineSmall.fixedFont(),
        titleLarge = t.titleLarge.fixedFont(),
        titleMedium = t.titleMedium.fixedFont(),
        titleSmall = t.titleSmall.fixedFont(),
        bodyLarge = t.bodyLarge.fixedFont(),
        bodyMedium = t.bodyMedium.fixedFont(),
        bodySmall = t.bodySmall.fixedFont(),
        labelLarge = t.labelLarge.fixedFont(),
        labelMedium = t.labelMedium.fixedFont(),
        labelSmall = t.labelSmall.fixedFont()
    )
}

@Composable
fun RodriguesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = AppTypography, content = content)
}
