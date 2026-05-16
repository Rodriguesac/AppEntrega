package com.rodriguesacai.entregador
import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.rodriguesacai.entregador.service.DriverForegroundService
import com.rodriguesacai.entregador.ui.RodriguesDriverApp
class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); requestBasePermissions(); startService(Intent(this, DriverForegroundService::class.java)); setContent { RodriguesDriverApp() } }
    private fun requestBasePermissions() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        permissionLauncher.launch(permissions.toTypedArray())
    }
}
