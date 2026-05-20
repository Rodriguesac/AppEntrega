package com.rodriguesacai.entregador

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class UrgentOfferActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(0xFFF7F9F8.toInt())
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(22), dp(22), dp(22))
            background = round(0xFFFFFFFF.toInt(), 28, 0xFFE5E7EB.toInt())
        }
        card.addView(label("Nova corrida", 26f, 0xFF111827.toInt(), true))
        card.addView(label(intent.getStringExtra("body") ?: "Abra o app para ver os detalhes da oferta.", 15f, 0xFF6B7280.toInt(), false))
        val open = Button(this).apply {
            text = "Abrir app"
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            background = round(0xFF16A34A.toInt(), 18, 0)
            setOnClickListener { startActivity(Intent(this@UrgentOfferActivity, MainActivity::class.java)); finish() }
        }
        card.addView(open, LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(18) })
        val close = Button(this).apply {
            text = "Agora não"
            setTextColor(0xFF111827.toInt())
            background = round(0xFFFFFFFF.toInt(), 18, 0xFFE5E7EB.toInt())
            setOnClickListener { finish() }
        }
        card.addView(close, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(10) })
        root.addView(card, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }

    private fun label(text: String, sp: Float, color: Int, bold: Boolean): TextView = TextView(this).apply {
        this.text = text
        textSize = sp
        setTextColor(color)
        gravity = Gravity.CENTER
        if (bold) typeface = Typeface.DEFAULT_BOLD
        setPadding(0, dp(4), 0, dp(4))
    }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun round(color: Int, radius: Int, stroke: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != 0) setStroke(dp(1), stroke)
    }
}
