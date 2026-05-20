package com.rodriguesacai.entregador

import android.app.Application
import com.google.firebase.FirebaseApp

class RodriguesDriverApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try { FirebaseApp.initializeApp(this) } catch (_: Exception) { }
        NotificationCenter.createChannels(this)
    }
}
