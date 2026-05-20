package com.rodriguesacai.entregador

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
fun Activity.dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
fun medium(): Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
fun bold(): Typeface = Typeface.create("sans-serif", Typeface.BOLD)
fun normal(): Typeface = Typeface.create("sans-serif", Typeface.NORMAL)

fun rounded(color: Int, radius: Float, strokeColor: Int? = null, strokeWidth: Int = 0): GradientDrawable = GradientDrawable().apply {
    setColor(color)
    cornerRadius = radius
    if (strokeColor != null && strokeWidth > 0) setStroke(strokeWidth, strokeColor)
}

fun Context.label(text: String, size: Float = 14f, color: Int = 0xFF45515C.toInt(), style: Typeface = normal()): TextView = TextView(this).apply {
    this.text = text
    textSize = size
    setTextColor(color)
    typeface = style
    includeFontPadding = true
}

fun Context.actionButton(text: String, primary: Boolean = true, onClick: () -> Unit): Button = Button(this).apply {
    this.text = text
    textSize = 15f
    isAllCaps = false
    typeface = medium()
    setTextColor(if (primary) Color.WHITE else 0xFF18A85A.toInt())
    background = if (primary) rounded(0xFF18A85A.toInt(), dp(18).toFloat()) else rounded(Color.WHITE, dp(18).toFloat(), 0xFFBCEFD1.toInt(), dp(1))
    setOnClickListener { onClick() }
}

fun Activity.actionButton(text: String, primary: Boolean = true, onClick: () -> Unit): Button = (this as Context).actionButton(text, primary, onClick)

fun Context.card(padding: Int = 16, radius: Int = 22): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
    background = rounded(Color.WHITE, dp(radius).toFloat(), 0xFFE7ECEA.toInt(), dp(1))
    elevation = dp(2).toFloat()
}

fun LinearLayout.addSpace(height: Int) {
    addView(View(context), LinearLayout.LayoutParams(1, context.dp(height)))
}

fun LinearLayout.addDivider() {
    addView(View(context).apply { setBackgroundColor(0xFFEAEFEC.toInt()) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.dp(1)).apply { setMargins(0, context.dp(12), 0, context.dp(12)) })
}

fun horizontal(context: Context, gravity: Int = Gravity.CENTER_VERTICAL): LinearLayout = LinearLayout(context).apply {
    orientation = LinearLayout.HORIZONTAL
    this.gravity = gravity
}
