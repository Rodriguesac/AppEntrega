package com.rodriguesacai.entregador

import android.app.Application
import com.google.firebase.FirebaseApp
import com.rodriguesacai.entregador.service.NotificationHelper

class RodriguesEntregadorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        NotificationHelper.createChannels(this)
    }
}
