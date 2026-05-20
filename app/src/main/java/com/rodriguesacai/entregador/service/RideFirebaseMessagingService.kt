package com.rodriguesacai.entregador.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rodriguesacai.entregador.R
import com.rodriguesacai.entregador.UrgentRideActivity
import com.rodriguesacai.entregador.data.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RideFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        NotificationHelper.createChannels(this)

        val rideId = message.data["corridaId"] ?: message.data["rideId"] ?: ""
        val title = message.notification?.title ?: message.data["titulo"] ?: "Nova corrida"
        val body = message.notification?.body ?: message.data["mensagem"] ?: "Toque para responder agora"

        val intent = Intent(this, UrgentRideActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("corridaId", rideId)
            putExtra("titulo", title)
            putExtra("mensagem", body)
        }
        val pending = PendingIntent.getActivity(
            this,
            rideId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_URGENT)
            .setSmallIcon(R.drawable.ic_stat_delivery)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pending, true)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(this).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
        startActivity(intent)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { FirebaseRepository(this@RideFirebaseMessagingService).saveFcmToken(token) }
        }
    }
}
