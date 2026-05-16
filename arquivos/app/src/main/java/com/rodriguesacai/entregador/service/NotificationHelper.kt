package com.rodriguesacai.entregador.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rodriguesacai.entregador.R
import com.rodriguesacai.entregador.UrgentOfferActivity

object NotificationHelper {
    const val CHANNEL_URGENT = "corridas_urgentes"
    const val CHANNEL_SERVICE = "servico_entregador"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)

        val urgent = NotificationChannel(
            CHANNEL_URGENT,
            "Corridas urgentes",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas de novas corridas"
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val service = NotificationChannel(
            CHANNEL_SERVICE,
            "Serviço do entregador",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mantém o entregador disponível no radar"
        }

        manager.createNotificationChannel(urgent)
        manager.createNotificationChannel(service)
    }

    fun serviceNotification(context: Context): Notification {
        ensureChannels(context)
        return NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_stat_delivery)
            .setContentTitle("Rodrigues Entregador")
            .setContentText("Radar de corridas ativo")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun urgentNotification(context: Context, title: String, body: String): Notification {
        ensureChannels(context)
        val intent = Intent(context, UrgentOfferActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_URGENT)
            .setSmallIcon(R.drawable.ic_stat_delivery)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()
    }

    fun showUrgent(context: Context, title: String, body: String) {
        runCatching {
            NotificationManagerCompat.from(context).notify(520, urgentNotification(context, title, body))
        }
    }
}
