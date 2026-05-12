package com.rodrigues.entregador

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rodrigues.entregador.ui.UrgentRideScreen

class UrgentRideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        val rideId = intent.getStringExtra("rideId") ?: "sem-id"
        val value = intent.getStringExtra("value") ?: "R$ --"
        val distance = intent.getStringExtra("distance") ?: "-- km"
        setContent {
            UrgentRideScreen(
                rideId = rideId,
                value = value,
                distance = distance,
                onAccept = { finish() },
                onReject = { finish() }
            )
        }
    }
}
