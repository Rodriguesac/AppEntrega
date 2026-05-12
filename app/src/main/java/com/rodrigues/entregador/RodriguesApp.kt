package com.rodrigues.entregador

import android.app.Application
import com.rodrigues.entregador.service.NotificationHelper

class RodriguesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
