package com.rodriguesacai.entregador

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun RodriguesNativeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val montserratLike = RodriguesFonts.Montserrat
    val base = Typography()
    val typography = base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = montserratLike),
        displayMedium = base.displayMedium.copy(fontFamily = montserratLike),
        displaySmall = base.displaySmall.copy(fontFamily = montserratLike),
        headlineLarge = base.headlineLarge.copy(fontFamily = montserratLike),
        headlineMedium = base.headlineMedium.copy(fontFamily = montserratLike),
        headlineSmall = base.headlineSmall.copy(fontFamily = montserratLike),
        titleLarge = base.titleLarge.copy(fontFamily = montserratLike),
        titleMedium = base.titleMedium.copy(fontFamily = montserratLike),
        titleSmall = base.titleSmall.copy(fontFamily = montserratLike),
        bodyLarge = base.bodyLarge.copy(fontFamily = montserratLike),
        bodyMedium = base.bodyMedium.copy(fontFamily = montserratLike),
        bodySmall = base.bodySmall.copy(fontFamily = montserratLike),
        labelLarge = base.labelLarge.copy(fontFamily = montserratLike),
        labelMedium = base.labelMedium.copy(fontFamily = montserratLike),
        labelSmall = base.labelSmall.copy(fontFamily = montserratLike)
    )

    val dark = darkColorScheme(
        primary = Color(0xFF82C91E),
        onPrimary = Color(0xFF10200A),
        secondary = Color(0xFF9B6DFF),
        background = Color(0xFF050507),
        onBackground = Color.White,
        surface = Color(0xFF15151C),
        onSurface = Color.White,
        surfaceVariant = Color(0xFF20202A),
        onSurfaceVariant = Color(0xFFC9C6D3),
        outline = Color(0xFF3B3644),
        error = Color(0xFFFF4D6D)
    )

    val light = lightColorScheme(
        primary = Color(0xFF2E7D00),
        onPrimary = Color.White,
        secondary = Color(0xFF3D7A16),
        background = Color(0xFFF8FAF6),
        onBackground = Color(0xFF14171F),
        surface = Color.White,
        onSurface = Color(0xFF14171F),
        surfaceVariant = Color(0xFFF1F6EA),
        onSurfaceVariant = Color(0xFF5D6672),
        outline = Color(0xFFE2EBDD),
        error = Color(0xFFD92D4A)
    )

    MaterialTheme(colorScheme = if (darkTheme) dark else light, typography = typography, content = content)
}
