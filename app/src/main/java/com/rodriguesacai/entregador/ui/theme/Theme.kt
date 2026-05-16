package com.rodriguesacai.entregador.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val LightScheme = lightColorScheme(background = Color(0xFFF7F8FA), surface = Color(0xFFFFFFFF), primary = Color(0xFF16A34A), secondary = Color(0xFFEF233C), onBackground = Color(0xFF111827), onSurface = Color(0xFF111827))
private val DarkScheme = darkColorScheme(background = Color(0xFF070B10), surface = Color(0xFF111923), primary = Color(0xFF22C55E), secondary = Color(0xFFEF233C), onBackground = Color(0xFFF9FAFB), onSurface = Color(0xFFF9FAFB))
@Composable fun RodriguesTheme(dark: Boolean, content: @Composable () -> Unit) { MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content) }
