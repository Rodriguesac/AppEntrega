package com.rodriguesacai.entregador

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationCenter {
    const val CHANNEL_URGENT = "corridas_urgentes"
    const val CHANNEL_GENERAL = "operacao"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val urgent = NotificationChannel(CHANNEL_URGENT, "Corridas urgentes", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alertas de novas corridas"
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), attrs)
            }
            val general = NotificationChannel(CHANNEL_GENERAL, "Operação", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Avisos e atualizações da operação"
            }
            manager.createNotificationChannel(urgent)
            manager.createNotificationChannel(general)
        }
    }

    fun showUrgentRide(context: Context, title: String, body: String, rideId: String?) {
        createChannels(context)
        val fullScreenIntent = Intent(context, UrgentRideActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("rideId", rideId.orEmpty())
            putExtra("title", title)
            putExtra("body", body)
        }
        val pendingFull = PendingIntent.getActivity(context, 7701, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pendingOpen = PendingIntent.getActivity(context, 7702, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_URGENT)
            .setSmallIcon(R.drawable.ic_stat_delivery)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 900))
            .setContentIntent(pendingOpen)
            .setFullScreenIntent(pendingFull, true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(9901, notification)
    }
}
