package com.rodriguesacai.entregador.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Bg = Color(0xFFF5F7FA)
    val Surface = Color(0xFFFFFFFF)
    val Ink = Color(0xFF0B1220)
    val Muted = Color(0xFF667085)
    val Line = Color(0xFFE5E7EB)
    val Green = Color(0xFF12B76A)
    val DarkGreen = Color(0xFF067647)
    val Red = Color(0xFFE53935)
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

@Composable
fun RodriguesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
