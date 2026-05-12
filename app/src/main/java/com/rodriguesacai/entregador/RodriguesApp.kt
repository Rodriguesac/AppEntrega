package com.rodriguesacai.entregador

import android.app.Application
import com.rodriguesacai.entregador.service.NotificationHelper

class RodriguesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
