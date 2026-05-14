package com.rodriguesacai.entregador

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.rodriguesacai.entregador.service.OnlineDriverService
import com.rodriguesacai.entregador.ui.DriverHomeScreen

class MainActivity : ComponentActivity() {
    private var pendingOnlineStart: Boolean = false

    private val notificationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val locationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (pendingOnlineStart && (fine || coarse)) startOnlineService()
        pendingOnlineStart = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermissionOnly()
        setContent {
            RodriguesNativeTheme {
                DriverHomeScreen(
                    onGoOnline = { requestLocationAndStartOnline() },
                    onGoOffline = { stopService(Intent(this, OnlineDriverService::class.java)) },
                    onOpenNavigator = { pickup, dropoff -> openNavigator(pickup, dropoff) },
                    onOpenBatterySettings = { openBatterySettings() }
                )
            }
        }
    }

    private fun askNotificationPermissionOnly() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestLocationAndStartOnline() {
        pendingOnlineStart = true
        locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun startOnlineService() {
        val intent = Intent(this, OnlineDriverService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
    }

    private fun openNavigator(pickup: String, dropoff: String) {
        val destination = dropoff.ifBlank { pickup }.ifBlank { "Rodrigues Açaí e Cia" }
        val navIntent = Intent(Intent.ACTION_VIEW, "google.navigation:q=${Uri.encode(destination)}".toUri()).apply {
            setPackage("com.google.android.apps.maps")
        }
        runCatching { startActivity(navIntent) }.onFailure {
            startActivity(Intent(Intent.ACTION_VIEW, "geo:0,0?q=${Uri.encode(destination)}".toUri()))
        }
    }

    private fun openBatterySettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
