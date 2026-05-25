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
import kotlin.math.absoluteValue
import androidx.core.app.NotificationCompat
import com.rodriguesacai.entregador.MainActivity
import com.rodriguesacai.entregador.R
import com.rodriguesacai.entregador.UrgentRideActivity

object NotificationHelper {
    const val CHANNEL_ONLINE = "driver_online"
    const val CHANNEL_URGENT = "urgent_ride_v600"
    const val CHANNEL_NOTICE = "driver_app_notices"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java)

        val online = NotificationChannel(
            CHANNEL_ONLINE,
            "Entregador online",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Serviço ativo enquanto o entregador está disponível"
        }

        val urgent = NotificationChannel(
            CHANNEL_URGENT,
            "Nova corrida urgente",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerta urgente de nova rota"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 700)
            setSound(
                Uri.parse("android.resource://${context.packageName}/${R.raw.alerta}"),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        val notices = NotificationChannel(
            CHANNEL_NOTICE,
            "Avisos da operação",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos enviados pelo gestor para o app do entregador"
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            enableVibration(true)
        }
        manager.createNotificationChannel(online)
        manager.createNotificationChannel(urgent)
        manager.createNotificationChannel(notices)
    }

    fun onlineNotification(context: Context): Notification {
        val pending = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CHANNEL_ONLINE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Rodrigues Entregador")
            .setContentText("Online e aguardando novas corridas.")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pending)
            .build()
    }

    fun serviceNotification(context: Context): Notification = onlineNotification(context)

    fun showUrgent(
        context: Context,
        rideId: String = "sem-id",
        value: String = "",
        distance: String = "",
        duration: String = "",
        pickup: String = "",
        dropoff: String = ""
    ) {
        urgentRideNotification(
            context = context,
            rideId = rideId,
            value = value,
            distance = distance,
            duration = duration,
            pickup = pickup,
            dropoff = dropoff
        )
    }

    fun appNoticeNotification(
        context: Context,
        noticeId: String,
        title: String,
        message: String,
        category: String = "Operação"
    ) {
        val pending = PendingIntent.getActivity(
            context,
            (noticeId.hashCode().absoluteValue % 50000) + 2000,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_NOTICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title.ifBlank { "Aviso da operação" })
            .setContentText(message.ifBlank { category })
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.ifBlank { title }))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((noticeId.hashCode().absoluteValue % 50000) + 2000, notification)
    }

    fun urgentRideNotification(
        context: Context,
        rideId: String,
        value: String,
        distance: String,
        duration: String,
        pickup: String,
        dropoff: String,
        paymentMethod: String = "",
        paymentStatus: String = "",
        amountToCollect: String = "",
        changeFor: String = "",
        requiresMachine: String = ""
    ) {
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alerta}")
        AppAlertPlayer.playNewRide(context)

        val fullScreenIntent = Intent(context, UrgentRideActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("rideId", rideId)
            putExtra("value", value)
            putExtra("distance", distance)
            putExtra("duration", duration)
            putExtra("pickup", pickup)
            putExtra("dropoff", dropoff)
            putExtra("paymentMethod", paymentMethod)
            putExtra("paymentStatus", paymentStatus)
            putExtra("amountToCollect", amountToCollect)
            putExtra("changeFor", changeFor)
            putExtra("requiresMachine", requiresMachine)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            2,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_URGENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Nova corrida urgente disponível")
            .setContentText(listOf(value, distance, duration).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Toque para abrir a oferta" })
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 700))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
        runCatching { context.startActivity(fullScreenIntent) }
    }
}
