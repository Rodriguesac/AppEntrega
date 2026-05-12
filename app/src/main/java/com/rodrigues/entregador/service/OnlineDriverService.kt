package com.rodrigues.entregador.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class OnlineDriverService : Service() {
    override fun onCreate() {
        super.onCreate()
        startForeground(10, NotificationHelper.onlineNotification(this))
        // Próximo passo: iniciar localização em background e salvar status no Firebase.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
