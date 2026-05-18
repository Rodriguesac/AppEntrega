package com.rodriguesacai.entregador.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class DriverForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        startForeground(52, NotificationHelper.serviceNotification(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
