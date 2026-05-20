package com.rodriguesacai.entregador.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rodriguesacai.entregador.data.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DriverLocationService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(7701, NotificationHelper.trackingNotification(this))
        val repo = FirebaseRepository(this)
        val fused = LocationServices.getFusedLocationProviderClient(this)
        job?.cancel()
        job = scope.launch {
            while (true) {
                val hasPermission = ActivityCompat.checkSelfPermission(this@DriverLocationService, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this@DriverLocationService, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (hasPermission && repo.driverId.isNotBlank()) {
                    runCatching {
                        val location = fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                        if (location != null) repo.updateLocation(location.latitude, location.longitude)
                    }
                }
                delay(30_000)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
