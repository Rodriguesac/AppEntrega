package com.rodriguesacai.entregador.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.rodriguesacai.entregador.ui.design.UpColors
import com.rodriguesacai.entregador.ui.design.UpTheme

object AppColors {
    val Bg: Color = UpColors.Screen
    val Surface: Color = UpColors.Surface
    val Ink: Color = UpColors.Ink
    val Muted: Color = UpColors.Muted
    val SoftMuted: Color = UpColors.Subtle
    val Line: Color = UpColors.Line
    val Green: Color = UpColors.Green
    val DarkGreen: Color = UpColors.GreenDark
    val Red: Color = UpColors.Red
    val Yellow: Color = UpColors.Orange
    val Purple: Color = UpColors.Blue
}

@Composable
fun RodriguesTheme(content: @Composable () -> Unit) = UpTheme(content)
