package com.rodriguesacai.entregador

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rodriguesacai.entregador.data.DriverRepository
import com.rodriguesacai.entregador.ui.UrgentRideScreen

class UrgentRideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        val rideId = intent.getStringExtra("rideId") ?: "sem-id"
        val value = intent.getStringExtra("value").orEmpty()
        val distance = intent.getStringExtra("distance").orEmpty()
        val duration = intent.getStringExtra("duration").orEmpty()
        val pickup = intent.getStringExtra("pickup").orEmpty()
        val dropoff = intent.getStringExtra("dropoff").orEmpty()
        val paymentMethod = intent.getStringExtra("paymentMethod").orEmpty()
        val paymentStatus = intent.getStringExtra("paymentStatus").orEmpty()
        val amountToCollect = intent.getStringExtra("amountToCollect").orEmpty()
        val changeFor = intent.getStringExtra("changeFor").orEmpty()
        val requiresMachine = intent.getStringExtra("requiresMachine").orEmpty()
        setContent {
            RodriguesNativeTheme(darkTheme = AppSettings.isDarkTheme(this)) {
                UrgentRideScreen(
                    rideId = rideId,
                    value = value,
                    distance = distance,
                    duration = duration,
                    pickup = pickup,
                    dropoff = dropoff,
                    paymentMethod = paymentMethod,
                    paymentStatus = paymentStatus,
                    amountToCollect = amountToCollect,
                    changeFor = changeFor,
                    requiresMachine = requiresMachine,
                    onAccept = { DriverRepository.acceptRide(this, rideId, onDone = { finish() }, onError = { finish() }) },
                    onReject = { DriverRepository.rejectRide(this, rideId, onDone = { finish() }, onError = { finish() }) },
                    onExpired = { DriverRepository.expireRide(this, rideId, onDone = { finish() }, onError = { finish() }) }
                )
            }
        }
    }
}
