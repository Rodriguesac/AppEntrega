package com.rodriguesacai.entregador

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class UrgentRideActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
        val title = intent.getStringExtra("title") ?: "Nova corrida disponível"
        val body = intent.getStringExtra("body") ?: "Abra o app para analisar a oferta."
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(32), dp(24), dp(32))
            background = rounded(0xFF18A85A.toInt(), 0f)
        }
        val icon = TextView(this).apply {
            text = "UP"
            textSize = 30f
            setTextColor(0xFF18A85A.toInt())
            gravity = Gravity.CENTER
            typeface = medium()
            background = rounded(0xFFFFFFFF.toInt(), dp(28).toFloat())
        }
        root.addView(icon, LinearLayout.LayoutParams(dp(82), dp(82)))
        root.addView(TextView(this).apply {
            text = title
            textSize = 28f
            setTextColor(0xFFFFFFFF.toInt())
            typeface = bold()
            gravity = Gravity.CENTER
            setPadding(0, dp(28), 0, dp(8))
        })
        root.addView(TextView(this).apply {
            text = body
            textSize = 16f
            setTextColor(0xFFE9FFF2.toInt())
            gravity = Gravity.CENTER
            setPadding(dp(12), 0, dp(12), dp(28))
        })
        root.addView(actionButton("Abrir app", true) {
            startActivity(android.content.Intent(this, MainActivity::class.java))
            finish()
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)).apply { setMargins(0, dp(8), 0, dp(8)) })
        root.addView(actionButton("Fechar", false) { finish() }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)).apply { setMargins(0, dp(8), 0, 0) })
        setContentView(root)
    }
}
