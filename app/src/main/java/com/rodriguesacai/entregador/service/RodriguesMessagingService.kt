package com.rodriguesacai.entregador.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rodriguesacai.entregador.MainActivity
import com.rodriguesacai.entregador.R

class RodriguesMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"] ?: message.data["tipo"] ?: "NORMAL"
        val title = message.notification?.title ?: message.data["title"] ?: message.data["titulo"] ?: if (type == "CORRIDA") "Nova corrida" else "Rodrigues Entregador"
        val body = message.notification?.body ?: message.data["body"] ?: message.data["corpo"] ?: "Você recebeu uma atualização operacional."
        showNotification(type, title, body)
    }

    private fun showNotification(type: String, title: String, body: String) {
        val channelId = if (type.uppercase() in setOf("CORRIDA", "OFERTA", "URGENTE")) "corridas_urgentes" else "avisos_operacionais"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = if (channelId == "corridas_urgentes") "Corridas urgentes" else "Avisos operacionais"
            val importance = if (channelId == "corridas_urgentes") NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
            manager.createNotificationChannel(NotificationChannel(channelId, name, importance))
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("open", if (channelId == "corridas_urgentes") "corrida" else "notificacoes")
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            4810,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(sound)
            .setVibrate(longArrayOf(0, 350, 120, 350, 120, 500))
            .setContentIntent(pendingIntent)
            .setPriority(if (channelId == "corridas_urgentes") NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)

        if (channelId == "corridas_urgentes") {
            builder.setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingIntent, true)
        }
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
