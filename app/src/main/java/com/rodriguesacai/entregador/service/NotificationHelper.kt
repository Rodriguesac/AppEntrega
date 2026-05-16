package com.rodriguesacai.entregador.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rodriguesacai.entregador.MainActivity
import com.rodriguesacai.entregador.R
import com.rodriguesacai.entregador.UrgentOfferActivity

object NotificationHelper {
    const val CHANNEL_URGENT = "corridas_urgentes"
    const val CHANNEL_SERVICE = "servico_entregador"
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val sound = Uri.parse("android.resource://${context.packageName}/${R.raw.alerta}")
            val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            val urgent = NotificationChannel(CHANNEL_URGENT, "Corridas urgentes", NotificationManager.IMPORTANCE_HIGH).apply { description = "Nova corrida para aceitar"; enableVibration(true); setSound(sound, attrs); lockscreenVisibility = Notification.VISIBILITY_PUBLIC }
            val service = NotificationChannel(CHANNEL_SERVICE, "Serviço do entregador", NotificationManager.IMPORTANCE_LOW).apply { description = "Mantém o app disponível para corridas" }
            manager.createNotificationChannel(urgent); manager.createNotificationChannel(service)
        }
    }
    fun serviceNotification(context: Context): Notification {
        val intent = PendingIntent.getActivity(context, 100, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, CHANNEL_SERVICE).setSmallIcon(R.drawable.ic_stat_delivery).setContentTitle("Rodrigues Entregador").setContentText("Pronto para receber corridas.").setOngoing(true).setContentIntent(intent).build()
    }
    fun urgentRideNotification(context: Context, title: String, body: String): Notification {
        val fullscreenIntent = Intent(context, UrgentOfferActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val fullPendingIntent = PendingIntent.getActivity(context, 200, fullscreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, CHANNEL_URGENT).setSmallIcon(R.drawable.ic_stat_delivery).setContentTitle(title).setContentText(body).setPriority(NotificationCompat.PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_CALL).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setFullScreenIntent(fullPendingIntent, true).setAutoCancel(false).build()
    }
}
