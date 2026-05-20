package com.rodriguesacai.entregador

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.rodriguesacai.entregador.data.FirebaseRepository
import com.rodriguesacai.entregador.ui.screens.rides.UrgentRideStandaloneScreen
import kotlinx.coroutines.launch

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
        val repo = FirebaseRepository(this)

        setContent {
            UrgentRideStandaloneScreen(
                rideId = rideId,
                title = title,
                body = body,
                onAccept = {
                    lifecycleScope.launch {
                        runCatching { repo.acceptRide(rideId) }
                        finish()
                    }
                },
                onReject = {
                    lifecycleScope.launch {
                        runCatching { repo.rejectRide(rideId, "Recusada pela tela urgente") }
                        finish()
                    }
                },
                onClose = { finish() }
            )
        }
    }
}
