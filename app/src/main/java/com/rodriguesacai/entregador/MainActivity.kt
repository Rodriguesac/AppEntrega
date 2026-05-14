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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import com.rodriguesacai.entregador.data.DriverRepository
import com.rodriguesacai.entregador.service.OnlineDriverService
import com.rodriguesacai.entregador.ui.DriverHomeScreen

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askBasicPermissions()
        setContent {
            MaterialTheme(
                typography = Typography().run {
                    copy(
                        displayLarge = displayLarge.copy(fontFamily = FontFamily.SansSerif),
                        displayMedium = displayMedium.copy(fontFamily = FontFamily.SansSerif),
                        displaySmall = displaySmall.copy(fontFamily = FontFamily.SansSerif),
                        headlineLarge = headlineLarge.copy(fontFamily = FontFamily.SansSerif),
                        headlineMedium = headlineMedium.copy(fontFamily = FontFamily.SansSerif),
                        headlineSmall = headlineSmall.copy(fontFamily = FontFamily.SansSerif),
                        titleLarge = titleLarge.copy(fontFamily = FontFamily.SansSerif),
                        titleMedium = titleMedium.copy(fontFamily = FontFamily.SansSerif),
                        titleSmall = titleSmall.copy(fontFamily = FontFamily.SansSerif),
                        bodyLarge = bodyLarge.copy(fontFamily = FontFamily.SansSerif),
                        bodyMedium = bodyMedium.copy(fontFamily = FontFamily.SansSerif),
                        bodySmall = bodySmall.copy(fontFamily = FontFamily.SansSerif),
                        labelLarge = labelLarge.copy(fontFamily = FontFamily.SansSerif),
                        labelMedium = labelMedium.copy(fontFamily = FontFamily.SansSerif),
                        labelSmall = labelSmall.copy(fontFamily = FontFamily.SansSerif)
                    )
                }
            ) {
                DriverHomeScreen(
                    onGoOnline = {
                        DriverRepository.setOnline(this, true)
                        startOnlineService()
                    },
                    onGoOffline = {
                        DriverRepository.setOnline(this, false)
                        stopService(Intent(this, OnlineDriverService::class.java))
                    },
                    onOpenNavigator = { openNavigator() },
                    onOpenBatterySettings = { openBatterySettings() },
                    onSimulateRide = { openSimulatedRide() },
                    driverId = DriverRepository.driverId(this)
                )
            }
        }
    }

    private fun askBasicPermissions() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startOnlineService() {
        val intent = Intent(this, OnlineDriverService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
    }

    private fun openNavigator() {
        val uri = "google.navigation:q=Rodrigues+Açaí+e+Cia".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        runCatching { startActivity(intent) }.onFailure {
            startActivity(Intent(Intent.ACTION_VIEW, "geo:0,0?q=Rodrigues+Açaí+e+Cia".toUri()))
        }
    }

    private fun openBatterySettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun openSimulatedRide() {
        startActivity(Intent(this, UrgentRideActivity::class.java).apply {
            putExtra("rideId", "SIMULACAO-001")
            putExtra("value", "R$ 12,50")
            putExtra("distance", "3,2 km")
            putExtra("pickup", "Rodrigues Açaí e Cia")
            putExtra("dropoff", "Cliente próximo ao Centro")
        })
    }
}
