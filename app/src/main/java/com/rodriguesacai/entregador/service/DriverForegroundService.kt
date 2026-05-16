package com.rodriguesacai.entregador.service
import android.app.Service
import android.content.Intent
import android.os.IBinder
class DriverForegroundService : Service() {
    override fun onCreate() { super.onCreate(); NotificationHelper.createChannels(this); startForeground(1001, NotificationHelper.serviceNotification(this)) }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { startForeground(1001, NotificationHelper.serviceNotification(this)); return START_STICKY }
    override fun onBind(intent: Intent?): IBinder? = null
}
