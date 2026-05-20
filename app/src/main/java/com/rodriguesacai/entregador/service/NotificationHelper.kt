package com.rodriguesacai.entregador.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rodriguesacai.entregador.R

object NotificationHelper {
    const val CHANNEL_URGENT = "corrida_urgente"
    const val CHANNEL_LOCATION = "rastreamento_entregador"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            val urgent = NotificationChannel(CHANNEL_URGENT, "Corrida urgente", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alertas de corrida em tela cheia"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                enableVibration(true)
            }
            val location = NotificationChannel(CHANNEL_LOCATION, "Rastreamento em corrida", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Localização ativa durante corrida"
            }
            nm.createNotificationChannel(urgent)
            nm.createNotificationChannel(location)
        }
    }

    fun trackingNotification(context: Context) = NotificationCompat.Builder(context, CHANNEL_LOCATION)
        .setSmallIcon(R.drawable.ic_stat_delivery)
        .setContentTitle("Rodrigues Entregador")
        .setContentText("Rastreamento ativo durante a corrida")
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
}
