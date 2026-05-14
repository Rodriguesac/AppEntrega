package com.rodriguesacai.entregador

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
fun RodriguesNativeTheme(content: @Composable () -> Unit) {
    val base = Typography()
    val typography = base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = FontFamily.SansSerif),
        displayMedium = base.displayMedium.copy(fontFamily = FontFamily.SansSerif),
        displaySmall = base.displaySmall.copy(fontFamily = FontFamily.SansSerif),
        headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.SansSerif),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.SansSerif),
        headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.SansSerif),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.SansSerif),
        titleMedium = base.titleMedium.copy(fontFamily = FontFamily.SansSerif),
        titleSmall = base.titleSmall.copy(fontFamily = FontFamily.SansSerif),
        bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
        bodySmall = base.bodySmall.copy(fontFamily = FontFamily.SansSerif),
        labelLarge = base.labelLarge.copy(fontFamily = FontFamily.SansSerif),
        labelMedium = base.labelMedium.copy(fontFamily = FontFamily.SansSerif),
        labelSmall = base.labelSmall.copy(fontFamily = FontFamily.SansSerif)
    )
    MaterialTheme(typography = typography, content = content)
}
