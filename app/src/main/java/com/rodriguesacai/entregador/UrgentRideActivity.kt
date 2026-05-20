package com.rodriguesacai.entregador

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rodriguesacai.entregador.ui.UrgentRideStandaloneScreen

class UrgentRideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        val rideId = intent.getStringExtra("corridaId").orEmpty()
        val title = intent.getStringExtra("titulo") ?: "Nova corrida"
        val body = intent.getStringExtra("mensagem") ?: "Corrida aguardando resposta"
        setContent {
            UrgentRideStandaloneScreen(
                rideId = rideId,
                title = title,
                body = body,
                onClose = { finish() }
            )
        }
    }
}
